package wota.ai.ameisenbaer;

import wota.gameobjects.Ant;
import wota.gameobjects.Hill;
import wota.gameobjects.Message;
import wota.gameobjects.Parameters;
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
	public final GameMap gameMap;

	public GameState(List<Sugar> sugar, List<Ant> ants, List<Ant> enemies, List<Ant> friends,
			List<Hill> hills, List<Message> messages, Parameters parameters, Ant self, GameMap map) {
		visibleSugar = sugar;
		visibleAnts = ants;
		visibleEnemies = enemies;
		visibleFriends = friends;
		visibleHills = hills;
		audibleMessages = messages;
		this.parameters = parameters;
		this.self = self;
		this.gameMap = map;
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

	public static List<Ant> getAntsInDistance(List<Ant> ants, Vector position, double distance) {
		List<Ant> result = new LinkedList<>();
		for (Ant ant : ants)
			if (Vector.subtract(ant.getPosition(), position).length() <= distance)
				result.add(ant);
		return result;
	}
}
