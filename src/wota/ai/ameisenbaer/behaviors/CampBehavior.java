package wota.ai.ameisenbaer.behaviors;

import wota.ai.ameisenbaer.Action;
import wota.ai.ameisenbaer.Behavior;
import wota.ai.ameisenbaer.GameState;

import wota.gameobjects.Hill;

public class CampBehavior implements Behavior {
	private final int maxFriends;

	public CampBehavior(int maxFriends) {
		this.maxFriends = maxFriends;
	}

	public Action tick(GameState state) {
		// If there are too many of us, go away
		if (GameState.getAntsWithCaste(state.visibleFriends, state.self.caste).size() > maxFriends)
			return null;

		// If there is a sugar source visible, camp it
		if (!state.visibleSugar.isEmpty())
			return Action.moveToward(state.visibleSugar.iterator().next().getPosition(), state);

		// If there is an hill visible, camp it
		if (!state.visibleHills.isEmpty())
			return Action.moveToward(state.visibleHills.iterator().next().getPosition(), state);

		return null;
	}
}
