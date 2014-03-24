package wota.ai.ameisenbaer.behaviors;

import wota.ai.ameisenbaer.Action;
import wota.ai.ameisenbaer.Behavior;
import wota.ai.ameisenbaer.GameState;

import wota.gameobjects.Ant;

import java.util.List;

public class AttackWeakestEnemyBehavior implements Behavior {
	public Action tick(GameState state) {
		List<Ant> enemies = GameState.getAntsInDistance(state.visibleEnemies, state.self.getPosition(), state.parameters.ATTACK_RANGE);
		Ant weakest = GameState.getWeakestAnt(enemies);
		return new Action(weakest);
	}
}
