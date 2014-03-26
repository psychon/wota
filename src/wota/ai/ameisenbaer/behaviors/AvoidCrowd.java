package wota.ai.ameisenbaer.behaviors;

import wota.ai.ameisenbaer.Action;
import wota.ai.ameisenbaer.Behavior;
import wota.ai.ameisenbaer.GameState;

import wota.gameobjects.Ant;
import wota.utility.Vector;

public class AvoidCrowd implements Behavior {
	static private final int WAIT_TICKS = 5;

	private Vector lastPosition = null;
	private Vector waitPosition = null;
	private int stoodStillTicks;
	private final double fallbackDirection;

	public AvoidCrowd(double fallbackDirection) {
		this.fallbackDirection = fallbackDirection;
	}

	public Action tick(GameState state) {
		// It's fine to stand still if no ant are near
		Ant closest = GameState.getClosestAnt(state.visibleFriends, state.self.getPosition());
		if (closest == null) {
			waitPosition = null;
			lastPosition = null;
			return null;
		}

		// Are we waiting somewhere?
		if (waitPosition != null && stoodStillTicks-- > 0)
			return Action.moveToward(waitPosition, state);

		// We are standing still if we weren't moving
		boolean standingStill = lastPosition != null && lastPosition.isSameVectorAs(state.self.getPosition());
		lastPosition = state.self.getPosition();
		if (!standingStill)
			return null;

		// Get away from our closest ant
		Vector dir = Vector.subtract(state.self.getPosition(), closest.getPosition());
		if (dir.length() != 0)
			dir = dir.scaleTo(state.parameters.ATTACK_RANGE);
		else
			dir = Vector.fromPolar(fallbackDirection, state.parameters.ATTACK_RANGE);

		lastPosition = null;
		waitPosition = Vector.add(state.self.getPosition(), dir);
		stoodStillTicks = WAIT_TICKS;
		return Action.moveToward(waitPosition, state);
	}
}
