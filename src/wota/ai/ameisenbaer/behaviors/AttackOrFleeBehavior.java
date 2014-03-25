package wota.ai.ameisenbaer.behaviors;

import wota.ai.ameisenbaer.Action;
import wota.ai.ameisenbaer.Behavior;
import wota.ai.ameisenbaer.GameState;

import wota.gameobjects.Ant;
import wota.utility.Vector;

public class AttackOrFleeBehavior implements Behavior {
	private final double attackFactor;
	private final double fleeFactor;

	public AttackOrFleeBehavior(double attackFactor, double fleeFactor) {
		this.attackFactor = attackFactor;
		this.fleeFactor = fleeFactor;
	}

	private Action attack(GameState state) {
		Ant target = GameState.getCarryingAnt(state.visibleEnemies);
		if (target == null)
			target = GameState.getWeakestAnt(state.visibleEnemies);
		if (target == null)
			return null;
		return Action.moveToAndAttack(target, state);
	}

	private Action flee(GameState state) {
		Ant attacker = GameState.getClosestAnt(state.visibleEnemies, state.self.getPosition());
		if (attacker == null)
			return null;
		return new Action(Vector.subtract(state.self.getPosition(), attacker.getPosition()));
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
