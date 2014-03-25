package wota.ai.ameisenbaer;

import wota.gameobjects.Ant;
import wota.gameobjects.Caste;
import wota.gameobjects.Hill;
import wota.gameobjects.Message;
import wota.gameobjects.Parameters;
import wota.gameobjects.Snapshot;
import wota.gameobjects.Sugar;
import wota.utility.Vector;

import java.util.List;
import java.util.LinkedList;

public class GameState {
	public final List<Sugar> visibleSugar;
	public final List<Ant> visibleAnts;
	public final List<Ant> visibleEnemies;
	public final List<Ant> visibleFriends;
	public final List<Hill> visibleHills;
	public final List<Message> audibleMessages;
	public final Parameters parameters;
	public final Ant self;
	public final Hill selfHill;
	public final GameMap gameMap;

	public GameState(List<Sugar> sugar, List<Ant> ants, List<Ant> enemies, List<Ant> friends,
			List<Hill> hills, List<Message> messages, Parameters parameters, Ant selfAnt, GameMap map) {
		this(sugar, ants, enemies, friends, hills, messages, parameters, selfAnt, null, map);
	}

	public GameState(List<Sugar> sugar, List<Ant> ants, List<Ant> enemies, List<Ant> friends,
			List<Hill> hills, List<Message> messages, Parameters parameters, Hill selfHill, GameMap map) {
		this(sugar, ants, enemies, friends, hills, messages, parameters, null, selfHill, map);
	}

	public GameState(List<Sugar> sugar, List<Ant> ants, List<Ant> enemies, List<Ant> friends,
			List<Hill> hills, List<Message> messages, Parameters parameters, Ant selfAnt, Hill selfHill, GameMap map) {
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
	}

	public static Ant getCarryingAnt(List<Ant> ants) {
		for (Ant ant : ants)
			if (ant.sugarCarry > 0) {
				return ant;
			}
		return null;
	}

	public static Ant getWeakestAnt(List<Ant> ants) {
		Ant weakest = null;
		double health = Double.MAX_VALUE;
		for (Ant ant : ants)
			if (ant.health < health) {
				health = ant.health;
				weakest = ant;
			}
		return weakest;
	}

	public static Ant getClosestAnt(List<Ant> ants, Vector position) {
		Ant result = null;
		double distance = Double.MAX_VALUE;
		for (Ant ant : ants) {
			double dist = Vector.subtract(ant.getPosition(), position).length();
			if (dist <= distance) {
				result = ant;
				distance = dist;
			}
		}
		return result;
	}

	public static Ant getHealthiestAnt(List<Ant> ants) {
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

	public static List<Ant> getAntsInDistance(List<Ant> ants, Vector position, double distance) {
		List<Ant> result = new LinkedList<>();
		for (Ant ant : ants)
			if (Vector.subtract(ant.getPosition(), position).length() <= distance)
				result.add(ant);
		return result;
	}

	public boolean isInView(Snapshot snapshot) {
		Snapshot s = self != null ? self : selfHill;
		double SIGHT_RANGE = self != null ? self.caste.SIGHT_RANGE : Caste.Hill.SIGHT_RANGE;
		return parameters.distance(snapshot.getPosition(), s.getPosition()) < SIGHT_RANGE;
	}
}
