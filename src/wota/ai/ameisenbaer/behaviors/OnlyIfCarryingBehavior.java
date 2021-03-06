package wota.ai.ameisenbaer.behaviors;

import wota.ai.ameisenbaer.Action;
import wota.ai.ameisenbaer.Behavior;
import wota.ai.ameisenbaer.GameState;

public class OnlyIfCarryingBehavior implements Behavior {
	private final Behavior otherBehavior;

	public OnlyIfCarryingBehavior(Behavior otherBehavior) {
		this.otherBehavior = otherBehavior;
	}

	public Action tick(GameState state) {
		if (state.self.sugarCarry == 0)
			return null;
		return otherBehavior.tick(state);
	}
}
