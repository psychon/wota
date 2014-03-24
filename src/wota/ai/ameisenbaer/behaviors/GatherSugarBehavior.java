package wota.ai.ameisenbaer.behaviors;

import wota.ai.ameisenbaer.Action;
import wota.ai.ameisenbaer.Behavior;
import wota.ai.ameisenbaer.GameState;

import wota.gameobjects.Sugar;

public class GatherSugarBehavior implements Behavior {
	private Sugar except = null;
	private Sugar target = null;

	private void pickTarget(GameState state) {
		if (!state.gameMap.checkExists(except))
			except = null;

		if (target != null && state.gameMap.checkExists(target)) {
			if (except != null)
				// We already skipped one, let's not get into a
				// loop by also skipping another one
				return;

			boolean visible = false;
			for (Sugar sugar : state.visibleSugar)
				if (sugar.hasSameOriginal(target)) {
					visible = true;
					break;
				}
			if (!visible || state.visibleFriends.size() <= 5)
				// Stay with the target we already have
				return;

			// Too many competing for this source
			except = target;
		}

		target = state.gameMap.getClosestSugarExcept(state.self.getPosition(), except);
	}

	public Action tick(GameState state) {
		if (state.self.sugarCarry != 0) {
			except = null;
		}

		pickTarget(state);

		if (target == null)
			return null;

		return new Action(target, Action.positionToDirection(target.getPosition(), state));
	}
}
