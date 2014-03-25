package wota.ai.ameisenbaer;

import wota.gameobjects.Ant;
import wota.gameobjects.Message;
import wota.gameobjects.Snapshot;
import wota.gameobjects.Sugar;
import wota.utility.Vector;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class GameMap {
	private int ticks = 0;
	private SnapshotList<Sugar> sugarSeen = new SnapshotList<>();
	private Queue<Seen<? extends Snapshot>> broadcastList = null;

	// The assumption is that all ants quickly learn about depleted food
	// sources and thus don't have to remember them for as long
	private static final int MAX_ALIVE_AGE = 10000;
	private static final int MAX_DEAD_AGE = 500;

	private static class Seen<U extends Snapshot> {
		public final U snapshot;
		public final int tick;
		public boolean alive;

		public Seen(U snapshot, int tick) {
			this(snapshot, tick, true);
		}

		public Seen(U snapshot, int tick, boolean alive) {
			this.snapshot = snapshot;
			this.tick = tick;
			this.alive = alive;
		}

		public boolean hasSameOriginal(U other) {
			return snapshot.hasSameOriginal(other);
		}

		public Action getAction(int ticks) {
			int t = ticks - this.tick;
			if (!this.alive)
				t *= -1;
			return new Action(t, this.snapshot);
		}

		public void updateFromMessage(int content) {
			if (content <= 0)
				alive = false;
		}

		static Seen<Sugar> sugarSeenFromMessage(int ticks, int content, Sugar snapshot) {
			if (content <= 0)
				return new Seen<Sugar>(snapshot, ticks + content, false);
			else
				return new Seen<Sugar>(snapshot, ticks - content);
		}
	}

	private static class SnapshotList<T extends Snapshot> implements Iterable<Seen<T>> {
		public final List<Seen<T>> snapshots = new ArrayList<>();

		@Override
		public Iterator<Seen<T>> iterator() {
			return snapshots.iterator();
		}

		private Iterator<Seen<T>> findEntry(T snapshot) {
			Iterator<Seen<T>> it = snapshots.iterator();
			while (it.hasNext()) {
				Seen<T> t = it.next();
				if (!snapshot.hasSameOriginal(t.snapshot))
					continue;

				return it;
			}
			return null;
		}

		private Seen<T> getEntry(T snapshot) {
			Iterator<Seen<T>> it = snapshots.iterator();
			while (it.hasNext()) {
				Seen<T> t = it.next();
				if (!snapshot.hasSameOriginal(t.snapshot))
					continue;

				return t;
			}
			return null;
		}

		private void add(Seen<T> seen) {
			// Remove old entry, if one exists
			Iterator<Seen<T>> it = findEntry(seen.snapshot);
			if (it != null)
				it.remove();

			// Add new one
			snapshots.add(seen);
		}

		private void removeOld(int aliveTick, int deadTick) {
			Iterator<Seen<T>> it = snapshots.iterator();
			while (it.hasNext()) {
				Seen<T> t = it.next();
				if ((t.alive && t.tick < aliveTick) || (!t.alive && t.tick < deadTick))
					it.remove();
			}
		}

		private void handleVisible(GameState state, List<T> visible) {
			Iterator<Seen<T>> it = snapshots.iterator();
			while (it.hasNext()) {
				Seen<T> t = it.next();
				// If it should be in view, but isn't, it must be dead
				if (state.isInView(t.snapshot) && !visible.contains(t.snapshot)) {
					t.alive = false;
				}
			}
		}
	}

	private void updateState(GameState state) {
		for (Message message : state.audibleMessages)
			if (message != null && message.contentSugar != null) {
				Seen<Sugar> seen = sugarSeen.getEntry(message.contentSugar);
				if (seen != null)
					seen.updateFromMessage(message.content);
				else
					sugarSeen.add(Seen.sugarSeenFromMessage(ticks, message.content, message.contentSugar));
			}

		sugarSeen.removeOld(ticks - MAX_ALIVE_AGE, ticks - MAX_DEAD_AGE);
		sugarSeen.handleVisible(state, state.visibleSugar);
		for (Sugar sugar : state.visibleSugar)
			sugarSeen.add(new Seen<Sugar>(sugar, ticks));
	}

	private Action getAction() {
		if (broadcastList == null || broadcastList.isEmpty()) {
			broadcastList = new ArrayDeque<Seen<? extends Snapshot>>(sugarSeen.snapshots);
		}
		if (broadcastList == null || broadcastList.isEmpty())
			return null;

		return broadcastList.poll().getAction(ticks);
	}

	public Action tick(GameState state) {
		updateState(state);
		ticks++;
		return getAction();
	}

	public Sugar getClosestSugarExcept(Vector position, Sugar except) {
		Sugar closest = null;
		double distance = Double.MAX_VALUE;
		for (Seen<Sugar> sugar : sugarSeen) {
			double dist = Vector.subtract(sugar.snapshot.getPosition(), position).length();
			if (dist < distance && sugar.alive && !sugar.snapshot.hasSameOriginal(except)) {
				closest = sugar.snapshot;
				distance = dist;
			}
		}

		return closest;
	}

	public Sugar getClosestSugar(Vector position) {
		return getClosestSugarExcept(position, null);
	}

	public boolean checkExists(Sugar sugar) {
		for (Seen<Sugar> s : sugarSeen) {
			if (s.alive && s.snapshot.hasSameOriginal(sugar))
				return true;
		}

		return false;
	}
}
