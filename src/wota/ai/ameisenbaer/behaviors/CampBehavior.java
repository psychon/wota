package wota.ai.ameisenbaer.behaviors;

import wota.ai.ameisenbaer.Action;
import wota.ai.ameisenbaer.Behavior;
import wota.ai.ameisenbaer.GameState;

import wota.gameobjects.Ant;
import wota.gameobjects.Hill;
import wota.gameobjects.Snapshot;
import wota.utility.Vector;

import java.util.Collection;

public class CampBehavior implements Behavior {
	private final int maxFriends;

	public CampBehavior(int maxFriends) {
		this.maxFriends = maxFriends;
	}

	public Action tick(GameState state) {
		Snapshot target = null;

		// If there is an hill visible, camp it
		if (!state.visibleHills.isEmpty())
			target = state.visibleHills.iterator().next();

		// If there is a sugar source visible, camp it
		if (!state.visibleSugar.isEmpty())
			target = state.visibleSugar.iterator().next();

		if (target == null)
			return null;

		// If there are too many of us, go away
		double ownDistance = Vector.subtract(state.self.getPosition(), target.getPosition()).length();
		Collection<Ant> ants = GameState.getAntsWithCaste(state.visibleFriends, state.self.caste);
		ants = GameState.getAntsInDistance(ants, target.getPosition(), ownDistance + 0.1);
		if (ants.size() > maxFriends)
			return null;

		return Action.moveToward(target.getPosition(), state);
	}
}
