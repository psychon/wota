package wota.ai.ameisenbaer;

import wota.gameobjects.Ant;
import wota.gameobjects.Message;
import wota.gameobjects.Snapshot;
import wota.gameobjects.Sugar;
import wota.utility.Vector;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class GameMap {
	private int ticks = 0;
	private final Set<Sugar> sugarAlive = new SnapshotSet<>();
	private final SortedMap<Integer, Set<Sugar>> sugarDead = new TreeMap<>();
	private Deque<Action> broadcastList = null;

	private static final int MAX_DEAD_AGE = 1000;

	private void markDead(int ticks, Sugar sugar) {
		if (!sugarAlive.remove(sugar))
			// We didn't know about it, so we don't care
			return;

		// Add an entry to the "dead map"
		Set<Sugar> set = sugarDead.get(ticks);
		if (set == null) {
			set = new SnapshotSet<>();
			sugarDead.put(ticks, set);
		}
		set.add(sugar);

		// Fix up the message queue
		if (broadcastList != null) {
			Iterator<Action> it = broadcastList.iterator();
			while (it.hasNext()) {
				Action action = it.next();
				if (!action.messageSnapshot.hasSameOriginal(sugar))
					continue;

				it.remove();

				// Add new message in head position so that it gets broadcasted soon
				broadcastList.addFirst(new Action(ticks, sugar));
				break;
			}
		}
	}

	private void updateState(GameState state) {
		for (Message message : state.audibleMessages)
			if (message != null && message.contentSugar != null) {
				Sugar sugar = message.contentSugar;
				if (message.content == 0)
					sugarAlive.add(sugar);
				else
					markDead(ticks - message.content, sugar);
			}

		// Remove all dead entries older than MAX_DEAD_AGE
		while (!sugarDead.isEmpty() && sugarDead.firstKey() < ticks - MAX_DEAD_AGE)
			sugarDead.remove(sugarDead.firstKey());

		// All snapshots which should be in view but aren't are dead
		for (Sugar sugar : sugarAlive) {
			if (state.isInView(sugar) && !state.visibleSugar.contains(sugar)) {
				markDead(ticks, sugar);
				// Ugly hack to avoid ConcurrentModificationExceptions
				break;
			}
		}

		// All snapshots which are in view are alive
		sugarAlive.addAll(state.visibleSugar);
	}

	private Action getAction() {
		if (broadcastList == null || broadcastList.isEmpty()) {
			broadcastList = new ArrayDeque<>();
			for (Sugar sugar : sugarAlive)
				broadcastList.addLast(new Action(0, sugar));
			for (SortedMap.Entry<Integer, Set<Sugar>> entry : sugarDead.entrySet()) {
				int content = ticks - entry.getKey();
				for (Sugar sugar : entry.getValue())
					broadcastList.addLast(new Action(content, sugar));
			}
		}
		if (broadcastList == null || broadcastList.isEmpty())
			return null;

		return broadcastList.removeFirst();
	}

	public Action tick(GameState state) {
		updateState(state);
		ticks++;
		return getAction();
	}

	public Sugar getClosestSugarExcept(Vector position, Sugar except) {
		Sugar closest = null;
		double distance = Double.MAX_VALUE;
		for (Sugar sugar : sugarAlive) {
			double dist = Vector.subtract(sugar.getPosition(), position).length();
			if (dist < distance && !sugar.hasSameOriginal(except)) {
				closest = sugar;
				distance = dist;
			}
		}

		return closest;
	}

	public Sugar getClosestSugar(Vector position) {
		return getClosestSugarExcept(position, null);
	}

	public boolean checkExists(Sugar sugar) {
		return sugarAlive.contains(sugar);
	}
}
