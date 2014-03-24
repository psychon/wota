package wota.ai.ameisenbaer;

import wota.gameobjects.Ant;
import wota.gameobjects.Snapshot;
import wota.gameobjects.Sugar;
import wota.utility.Vector;

import java.util.List;

public class Action {
	public final Ant attackTarget;
	public final Sugar sugarTarget;

	public final boolean moveDirectionSet;
	public final double moveDirection;
	public final double moveLength;
	public final boolean moveHome;

	public final int messageContent;
	public final Snapshot messageSnapshot;

	public Action(Ant attackTarget) {
		this(attackTarget, null, null);
	}

	public Action(Sugar sugarTarget) {
		this(null, sugarTarget, null);
	}

	public Action(Sugar sugarTarget, Vector moveDirection) {
		this(null, sugarTarget, moveDirection);
	}

	public Action(Vector moveDirection) {
		this(null, null, moveDirection);
	}

	public Action(int content, Snapshot snapshot) {
		this(null, null, content, snapshot);
	}

	public Action(Ant attackTarget, Sugar sugarTarget, Vector moveDirection) {
		this(attackTarget, sugarTarget, moveDirection, false, 0, null);
	}

	private Action(Ant attackTarget, Sugar sugarTarget, int content, Snapshot snapshot) {
		this(attackTarget, sugarTarget, null, false, content, snapshot);
	}

	private Action(Ant attackTarget, Sugar sugarTarget, Vector moveDirection, boolean moveHome, int content, Snapshot snapshot) {
		this.attackTarget = attackTarget;
		this.sugarTarget = sugarTarget;
		this.moveHome = moveHome;
		this.messageContent = content;
		this.messageSnapshot = snapshot;
		if (moveDirection != null) {
			this.moveDirectionSet = true;
			this.moveDirection = moveDirection.angle();
			this.moveLength = moveDirection.length();
		} else {
			this.moveDirectionSet = false;
			this.moveDirection = -1;
			this.moveLength = 0;
		}
	}

	static public Action moveHomeAction() {
		return new Action(null, null, null, true, 0, null);
	}

	static public Action moveToward(Vector vector, GameState state) {
		return new Action(positionToDirection(vector, state));
	}

	static public Action moveInDirection(double direction, GameState state) {
		return new Action(Vector.fromPolar(state.parameters.SIZE_X/2, direction));
	}

	static public Action moveToAndAttack(Ant target, GameState state) {
		return new Action(target, null, positionToDirection(target.getPosition(), state));
	}

	static public Vector positionToDirection(Vector vector, GameState state) {
		return state.parameters.shortestDifferenceOnTorus(vector, state.self.getPosition());
	}
}
