package wota.ai.ameisenbaer.behaviors;

import wota.ai.ameisenbaer.Action;
import wota.ai.ameisenbaer.Behavior;
import wota.ai.ameisenbaer.GameState;

import wota.gameobjects.Sugar;
import wota.utility.Vector;

public class PatrolBehavior implements Behavior {
	private Sugar target = null;

	private void pickTarget(GameState state) {
		if (!state.gameMap.checkExists(target))
			target = null;

		if (target != null && Vector.subtract(state.self.getPosition(), target.getPosition()).length() < state.self.caste.SIGHT_RANGE)
			target = null;

		if (target == null)
			target = state.gameMap.getRandomSugarSource();
	}

	public Action tick(GameState state) {
		pickTarget(state);

		if (target == null)
			return null;

		return Action.moveToward(target.getPosition(), state);
	}
}
