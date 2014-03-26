package wota.ai.ameisenbaer.behaviors;

import wota.ai.ameisenbaer.Action;
import wota.ai.ameisenbaer.Behavior;
import wota.ai.ameisenbaer.GameState;

import wota.gameobjects.Ant;
import wota.utility.Vector;

public class AttackOrFleeBehavior implements Behavior {
	static private final int FLEE_TICKS = 10;
	static private final int ATTACK_TICKS = 30;

	private final double attackFactor;
	private final double fleeFactor;

	private int fleeTicks;
	private Vector fleeDirection;

	private int attackTicks;
	private Ant lastAttackTarget;

	public AttackOrFleeBehavior(double attackFactor, double fleeFactor) {
		this.attackFactor = attackFactor;
		this.fleeFactor = fleeFactor;
	}

	private Action attack(GameState state) {
		// dont endlessly follow an equally fast target
		Ant target = GameState.getCarryingAnt(state.visibleEnemies);
		if (target == null)
			target = GameState.getWeakestAnt(state.visibleEnemies);
		if (target == null) {
			lastAttackTarget = null;
			return null;
		}

		// If we are following the same target for too many ticks and
		// still aren't in attack range, give up
		if (lastAttackTarget != null && lastAttackTarget.hasSameOriginal(target)) {
			if (Vector.subtract(state.self.getPosition(), target.getPosition()).length() >= state.parameters.ATTACK_RANGE &&
					(attackTicks == 0 || --attackTicks == 0))
				return null;
		} else {
			lastAttackTarget = target;
			attackTicks = ATTACK_TICKS;
		}
		return Action.moveToAndAttack(target, state);
	}

	private Action flee(GameState state) {
		Ant attacker = GameState.getClosestAnt(state.visibleEnemies, state.self.getPosition());
		if (attacker == null)
			return null;

		fleeTicks = FLEE_TICKS;
		fleeDirection = Vector.subtract(state.self.getPosition(), attacker.getPosition());
		return new Action(fleeDirection);
	}

	private boolean shouldNotFlee(GameState state, double enemyAttack) {
		// The idea is: Don't flee if we are carrying sugar...
		if (state.self.sugarCarry == 0 || enemyAttack == 0)
			return false;

		double roundsSurvive = state.self.health / enemyAttack;
		double roundsToGo = state.vectorToHome.length() / state.self.caste.SPEED_WHILE_CARRYING_SUGAR;

		// ...and we can get the sugar home before we die
		return roundsToGo < roundsSurvive;
	}

	public Action tick(GameState state) {
		if (fleeTicks > 0) {
			fleeTicks--;
			return new Action(fleeDirection);
		}

		double friendHealth = state.self.health;
		double friendAttack = state.self.caste.ATTACK;
		for (Ant ant : state.visibleFriends) {
			friendHealth += ant.health;
			if (ant.sugarCarry == 0)
				friendAttack += ant.caste.ATTACK;
		}

		double enemyHealth = 0;
		double enemyAttack = 0;
		for (Ant ant : state.visibleEnemies) {
			enemyHealth += ant.health;
			if (ant.sugarCarry == 0)
				enemyAttack += ant.caste.ATTACK;
		}

		if (friendHealth * attackFactor > enemyHealth && friendAttack * attackFactor > enemyAttack)
			return attack(state);

		if (shouldNotFlee(state, enemyAttack))
			return null;

		if (friendHealth * fleeFactor < enemyHealth || friendAttack * fleeFactor < enemyAttack)
			return flee(state);

		return null;
	}
}
