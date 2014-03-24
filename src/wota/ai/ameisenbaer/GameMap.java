package wota.ai.ameisenbaer;

import wota.gameobjects.Ant;
import wota.gameobjects.Message;
import wota.gameobjects.Snapshot;
import wota.gameobjects.Sugar;
import wota.utility.Vector;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GameMap {
	private int tick = 0;
	private SnapshotList<Sugar> sugarSeen = new SnapshotList<>();
	private int sugarSeenMessageIndex = 0;

	private final InViewInterface inView;

	// The assumption is that all ants quickly learn about depleted food
	// sources and thus don't have to remember them for as long
	private static final int MAX_ALIVE_AGE = 10000;
	private static final int MAX_DEAD_AGE = 500;

	// Urgh. Ugly hack
	public static interface InViewInterface {
		public boolean isInView(Snapshot target);
	}

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
	}

	private static class SnapshotList<T extends Snapshot> implements Iterable<Seen<T>> {
		public final List<Seen<T>> snapshots = new LinkedList<>();

		@Override
		public Iterator<Seen<T>> iterator() {
			return snapshots.iterator();
		}

		private Iterator<Seen<T>> findEntry(T snapshot) {
			// Remove old entry, if one exists
			Iterator<Seen<T>> it = snapshots.iterator();
			while (it.hasNext()) {
				Seen<T> t = it.next();
				if (!snapshot.hasSameOriginal(t.snapshot))
					continue;

				return it;
			}
			return null;
		}

		private boolean isKnown(T snapshot) {
			return findEntry(snapshot) != null;
		}

		private void add(Seen<T> seen) {
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

		private void handleVisible(InViewInterface inView, List<T> visible) {
			Iterator<Seen<T>> it = snapshots.iterator();
			while (it.hasNext()) {
				Seen<T> t = it.next();
				// If it should be in view, but isn't, it must be dead
				if (inView.isInView(t.snapshot) && !visible.contains(t.snapshot)) {
					t.alive = false;
				}
			}
		}
	}

	public GameMap(InViewInterface inView) {
		this.inView = inView;
	}

	public Action tick(List<Message> messages, List<Sugar> visibleSugar) {
		for (Message message : messages)
			if (message != null && message.contentSugar != null) {
				int content = message.content;
				Sugar sugar = message.contentSugar;
				// The sign specifies if the Sugar is alive or dead
				if (message.content <= 0)
					sugarSeen.add(new Seen<Sugar>(sugar, tick + content, false));
				else if (!sugarSeen.isKnown(message.contentSugar))
					sugarSeen.add(new Seen<Sugar>(sugar, tick - content));
			}

		sugarSeen.removeOld(tick - MAX_ALIVE_AGE, tick - MAX_DEAD_AGE);
		if (inView != null) {
			sugarSeen.handleVisible(inView, visibleSugar);
		}
		for (Sugar sugar : visibleSugar)
			sugarSeen.add(new Seen<Sugar>(sugar, tick));

		tick++;

		// Pick a message to broadcast
		if (sugarSeen.snapshots.isEmpty())
			return null;

		sugarSeenMessageIndex++;
		if (sugarSeenMessageIndex >= sugarSeen.snapshots.size())
			sugarSeenMessageIndex = 0;
		Seen<Sugar> sugar = sugarSeen.snapshots.get(sugarSeenMessageIndex);
		int t = tick - sugar.tick;
		if (!sugar.alive)
			t *= -1;
		return new Action(t, sugar.snapshot);
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
