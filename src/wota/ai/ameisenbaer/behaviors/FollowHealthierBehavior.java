package wota.ai.ameisenbaer.behaviors;

import wota.ai.ameisenbaer.Action;
import wota.ai.ameisenbaer.Behavior;
import wota.ai.ameisenbaer.GameState;

import wota.gameobjects.Ant;
import wota.utility.Vector;

public class FollowHealthierBehavior implements Behavior {
	public Action tick(GameState state) {
		Ant target = GameState.getHealthiestAnt(state.visibleFriends);
		if (target == null || target.health <= state.self.health)
			return null;
		return Action.moveToward(target.getPosition(), state);
	}
}
