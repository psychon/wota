package wota.ai.ameisenbaer.behaviors;

import wota.ai.ameisenbaer.Action;
import wota.ai.ameisenbaer.Behavior;
import wota.ai.ameisenbaer.GameState;

import wota.gameobjects.Hill;

public class CampInEnemyHillBehavior implements Behavior {
	public Action tick(GameState state) {
		if (state.visibleFriends.size() > 10)
			return null;

		for (Hill hill : state.visibleHills) {
			if (hill.playerID != state.self.playerID)
				return Action.moveToward(hill.getPosition(), state);
		}

		return null;
	}
}
