package wota.ai.ameisenbaer.behaviors;

import wota.ai.ameisenbaer.Action;
import wota.ai.ameisenbaer.Behavior;
import wota.ai.ameisenbaer.GameState;

public class ReturnHomeBehavior implements Behavior {
	public Action tick(GameState state) {
		return Action.moveHomeAction();
	}
}
