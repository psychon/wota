package wota.ai.ameisenbaer.behaviors;

import wota.ai.ameisenbaer.Action;
import wota.ai.ameisenbaer.Behavior;
import wota.ai.ameisenbaer.GameState;

import wota.gameobjects.Sugar;

public class GatherSugarBehavior implements Behavior {
	private Sugar except = null;
	private Sugar target = null;

	private void pickTarget(GameState state) {
		if (!state.gameMap.checkExists(except) || state.self.sugarCarry != 0)
			except = null;
		if (!state.gameMap.checkExists(target))
			target = null;

		// If we have a target, check if it's too crowded and pick
		// another one if needed.
		if (target != null && state.gameMap.checkExists(target)) {
			if (except != null)
				// We already skipped one, let's not get into a
				// loop by also skipping another one
				return;

			// Check if our target is visible
			boolean visible = false;
			for (Sugar sugar : state.visibleSugar)
				if (sugar.hasSameOriginal(target)) {
					visible = true;
					break;
				}

			// Don't re-pick if the target isn't overly crowded
			if (!visible || state.visibleFriends.size() <= 5)
				return;

			// Too much competition for this source
			except = target;
		}

		target = state.gameMap.getClosestSugarExcept(state.self.getPosition(), except);
	}

	public Action tick(GameState state) {
		pickTarget(state);

		if (target == null)
			return null;

		return new Action(target, Action.positionToDirection(target.getPosition(), state));
	}
}
