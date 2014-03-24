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

	private static final int MAX_AGE = 10000;

	// Urgh. Ugly hack
	public static interface InViewInterface {
		public boolean isInView(Snapshot target);
	}

	private static class Seen<U extends Snapshot> {
		public final U snapshot;
		public int tick;

		public Seen(U snapshot, int tick) {
			this.snapshot = snapshot;
			this.tick = tick;
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

		private void add(T snapshot, int tick) {
			Iterator<Seen<T>> it = findEntry(snapshot);
			if (it != null)
				it.remove();

			// Add new one
			snapshots.add(new Seen<T>(snapshot, tick));
		}

		private void removeOld(int tick) {
			Iterator<Seen<T>> it = snapshots.iterator();
			while (it.hasNext()) {
				Seen<T> t = it.next();
				// FIXME: This hack is used so that we can
				// broadcast "this source is depleted"-messages.
				// That should be handled more intelligently.
				if (t.tick != 0 && t.tick < tick)
					it.remove();
			}
		}

		private void handleVisible(InViewInterface inView, List<T> visible) {
			Iterator<Seen<T>> it = snapshots.iterator();
			while (it.hasNext()) {
				Seen<T> t = it.next();
				if (inView.isInView(t.snapshot) && !visible.contains(t.snapshot)) {
					// We should be seeing this Snapshot,
					// but we don't. Thus, it must be gone.
					t.tick = 0;
				}
			}
		}
	}

	public GameMap(InViewInterface inView) {
		this.inView = inView;
	}

	public Action tick(List<Message> messages, List<Sugar> visibleSugar) {
		tick++;

		for (Message message : messages)
			if (message != null && message.contentSugar != null) {
				if (message.content == Integer.MIN_VALUE)
					// Mark as DEAD
					sugarSeen.add(message.contentSugar, 0);
				else if (!sugarSeen.isKnown(message.contentSugar))
					sugarSeen.add(message.contentSugar, tick - message.content);
			}

		sugarSeen.removeOld(tick - MAX_AGE);
		if (inView != null)
			sugarSeen.handleVisible(inView, visibleSugar);
		for (Sugar sugar : visibleSugar)
			sugarSeen.add(sugar, tick);

		if (sugarSeen.snapshots.isEmpty())
			return null;

		sugarSeenMessageIndex++;
		if (sugarSeenMessageIndex >= sugarSeen.snapshots.size())
			sugarSeenMessageIndex = 0;
		Seen<Sugar> sugar = sugarSeen.snapshots.get(sugarSeenMessageIndex);
		int t = sugar.tick;
		if (t != 0)
			t = tick - t;
		else
			t = Integer.MIN_VALUE;
		return new Action(t, sugar.snapshot);
	}

	public Sugar getClosestSugarExcept(Vector position, Sugar except) {
		Sugar closest = null;
		double distance = Double.MAX_VALUE;
		for (Seen<Sugar> sugar : sugarSeen) {
			double dist = Vector.subtract(sugar.snapshot.getPosition(), position).length();
			if (dist < distance && sugar.tick != 0 && !sugar.snapshot.hasSameOriginal(except)) {
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
			if (s.tick != 0 && s.snapshot.hasSameOriginal(sugar))
				return true;
		}

		return false;
	}
}
