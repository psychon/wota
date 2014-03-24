package wota.ai.ameisenbaer.behaviors;

import wota.ai.ameisenbaer.Action;
import wota.ai.ameisenbaer.Behavior;
import wota.ai.ameisenbaer.GameState;

public class MoveInDirectionBehavior implements Behavior {
	private final int direction;

	public MoveInDirectionBehavior(int direction) {
		this.direction = direction;
	}

	public Action tick(GameState state) {
		return Action.moveInDirection(direction, state);
	}
}
