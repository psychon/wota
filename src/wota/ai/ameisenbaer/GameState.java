package wota.ai.ameisenbaer;

import wota.gameobjects.Ant;
import wota.gameobjects.Caste;
import wota.gameobjects.Hill;
import wota.gameobjects.Message;
import wota.gameobjects.Parameters;
import wota.gameobjects.Snapshot;
import wota.gameobjects.Sugar;
import wota.utility.Vector;

import java.util.Collection;

public class GameState {
	private static final double CLOSE_DISTANCE_LIMIT = 1e-4;

	public final Collection<Sugar> visibleSugar;
	public final Collection<Ant> visibleAnts;
	public final Collection<Ant> visibleEnemies;
	public final Collection<Ant> visibleFriends;
	public final Collection<Hill> visibleHills;
	public final Collection<Message> audibleMessages;
	public final Parameters parameters;
	public final Ant self;
	public final Hill selfHill;
	public final GameMap gameMap;
	public final Vector vectorToHome;

	public GameState(Collection<Sugar> sugar, Collection<Ant> ants, Collection<Ant> enemies, Collection<Ant> friends,
			Collection<Hill> hills, Collection<Message> messages, Parameters parameters, Ant selfAnt, GameMap map,
			Vector vectorToHome) {
		this(sugar, ants, enemies, friends, hills, messages, parameters, selfAnt, null, map, vectorToHome);
	}

	public GameState(Collection<Sugar> sugar, Collection<Ant> ants, Collection<Ant> enemies, Collection<Ant> friends,
			Collection<Hill> hills, Collection<Message> messages, Parameters parameters, Hill selfHill, GameMap map,
			Vector vectorToHome) {
		this(sugar, ants, enemies, friends, hills, messages, parameters, null, selfHill, map, vectorToHome);
	}

	public GameState(Collection<Sugar> sugar, Collection<Ant> ants, Collection<Ant> enemies, Collection<Ant> friends,
			Collection<Hill> hills, Collection<Message> messages, Parameters parameters, Ant selfAnt, Hill selfHill,
			GameMap map, Vector vectorToHome) {
		visibleSugar = sugar;
		visibleAnts = ants;
		visibleEnemies = enemies;
		visibleFriends = friends;
		visibleHills = hills;
		audibleMessages = messages;
		this.parameters = parameters;
		this.self = selfAnt;
		this.selfHill = selfHill;
		this.gameMap = map;
		this.vectorToHome = vectorToHome;
	}

	public static Ant getCarryingAnt(Collection<Ant> ants) {
		for (Ant ant : ants)
			if (ant.sugarCarry > 0) {
				return ant;
			}
		return null;
	}

	public static Ant getWeakestAnt(Collection<Ant> ants) {
		Ant weakest = null;
		double health = Double.MAX_VALUE;
		for (Ant ant : ants)
			if (ant.health < health) {
				health = ant.health;
				weakest = ant;
			}
		return weakest;
	}

	public static Ant getClosestAnt(Collection<Ant> ants, Vector position) {
		Ant result = null;
		double distance = Double.MAX_VALUE;
		for (Ant ant : ants) {
			double dist = Vector.subtract(ant.getPosition(), position).length();
			if (dist <= distance) {
				result = ant;
				distance = dist;
				// If too many ants are standing on the same
				// spot, this code could use too much time
				if (distance < CLOSE_DISTANCE_LIMIT)
					break;
			}
		}
		return result;
	}

	public static Ant getHealthiestAnt(Collection<Ant> ants) {
		Ant result = null;
		double health = 0;
		for (Ant ant : ants) {
			if (ant.health > health) {
				result = ant;
				health = ant.health;
			}
		}
		return result;
	}

	public static Collection<Ant> getAntsInDistance(Collection<Ant> ants, Vector position, double distance) {
		Collection<Ant> result = new SnapshotSet<Ant>();
		for (Ant ant : ants)
			if (Vector.subtract(ant.getPosition(), position).length() <= distance)
				result.add(ant);
		return result;
	}

	public static Collection<Ant> getAntsWithCaste(Collection<Ant> ants, Caste caste) {
		Collection<Ant> result = new SnapshotSet<Ant>();
		for (Ant ant : ants)
			if (ant.caste.equals(caste))
				result.add(ant);
		return result;
	}

	public boolean isInView(Snapshot snapshot) {
		Snapshot s = self != null ? self : selfHill;
		double SIGHT_RANGE = self != null ? self.caste.SIGHT_RANGE : Caste.Hill.SIGHT_RANGE;
		return parameters.distance(snapshot.getPosition(), s.getPosition()) < SIGHT_RANGE;
	}
}
