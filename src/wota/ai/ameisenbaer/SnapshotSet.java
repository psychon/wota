package wota.ai.ameisenbaer;

import wota.gameobjects.Snapshot;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class SnapshotSet<T extends Snapshot> extends AbstractSet<T> {
	private Collection<T> data = new LinkedList<T>();

	public SnapshotSet() {
	}

	public SnapshotSet(Collection<? extends T> c) {
		for (T t : c)
			add(t);
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public Iterator<T> iterator() {
		return data.iterator();
	}

	@Override
	public boolean add(T t) {
		if (contains(t))
			return false;
		data.add(t);
		return true;
	}

	@Override
	public boolean contains(Object arg) {
		if (!(arg instanceof Snapshot))
			return false;

		Snapshot s = (Snapshot) arg;
		for (T t : this) {
			if (t.hasSameOriginal(s))
				return true;
		}

		return false;
	}
}
