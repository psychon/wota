package de.wota.gameobjects;

import java.lang.Math;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.wota.gamemaster.AILoader;
import de.wota.gamemaster.AbstractLogger;
import de.wota.gameobjects.LeftoverParameters;

import de.wota.utility.Vector;

/**
 * Enthält alle Elemente der Spielwelt.
 * 
 * @author pascal
 */
public class GameWorld {
	private final List<Player> players = new LinkedList<Player>();
	private final LinkedList<SugarObject> sugarObjects = new LinkedList<SugarObject>();
	
	private List<AbstractLogger> registeredLoggers = new LinkedList<AbstractLogger>();

	private SpacePartitioning spacePartitioning;
	
	private final Parameters parameters;
	
	public GameWorld(Parameters parameters) {
		this.parameters = parameters;
		spacePartitioning = new SpacePartitioning(maximumSight(), parameters);
	}
	
	private static double maximumSight() {
		double maximum = 0.0;
		for (Caste caste : Caste.values()) {
			if (caste.SIGHT_RANGE > maximum) {
				maximum = caste.SIGHT_RANGE;
			}
			if (caste.HEARING_RANGE > maximum) {
				maximum = caste.HEARING_RANGE;
			}
		}
		return maximum;
	}
	
	public void addSugarObject(SugarObject sugarObject) {
		sugarObjects.add(sugarObject);
		spacePartitioning.addSugarObject(sugarObject);
	}
	
	/** Do not modify the list! Use addSugarObject instead */
	public List<SugarObject> getSugarObjects() {
		return sugarObjects;
	}
		
	public void addPlayer(Player player) {
		notifyLoggers(AbstractLogger.LogEventType.PLAYER_REGISTERED);

		players.add(player);
	}

	private static int nextPlayerId = 0; // TODO can this somehow go into Player?
	
	public class Player {
		public final List<AntObject> antObjects = new LinkedList<AntObject>();
		public final HillObject hillObject;
		public final QueenObject queenObject;

		public final String name;

		private final int id;
		
		public boolean hasLost = false;

		public int getId() {
			return id;
		}

		// TODO make this private and change addPlayer
		public Player(Vector position, Class<? extends QueenAI> queenAIClass) {
			hillObject = new HillObject(position, this, parameters);
			queenObject = new QueenObject(position, queenAIClass, this, parameters);
			
			if (LeftoverParameters.QUEEN_IS_VISIBLE) {
				addAntObject(queenObject);
			}
			else {
				antObjects.add(queenObject);
			}
				
		
			name = AILoader.getAIName(queenAIClass);

			// TODO fail early w.r.t. to ants, too, by creating one to test ant
			// creation
			id = nextPlayerId;
			nextPlayerId++;
		}
		
		public void addAntObject(AntObject antObject) {
			antObjects.add(antObject);
			spacePartitioning.addAntObject(antObject);
		}
	}
	
	public void tick() {		
		notifyLoggers(AbstractLogger.LogEventType.TICK);
		
		// can be removed as soon as SpacePartitioning is well tested!
		if (LeftoverParameters.DEBUG) {
			System.out.println("SpacePartitioning: " + spacePartitioning.totalNumberOfAntObjects());
			System.out.println("Total number: " + totalNumberOfAntObjects());
		}

		// create Ants for all AntObjects and the QueenObject and sets them in
		// the AntAI (the latter happens in AntObject.createAnt() )
		// also create Sugar for SugarObjects
		for (Player player : players) {
			for (AntObject antObject : player.antObjects) {
				antObject.createAnt();
			}
			player.hillObject.createHill();
		}

		for (SugarObject sugarObject : sugarObjects) {
			sugarObject.createSugar();
		}
		
		// The MessageObjects don't need a "createMessage", because one can
		// construct the Message instance when the
		// MessageObject instance is constructed.

		// call tick for all AntObjects
		for (Player player : players) {			
			for (AntObject antObject : player.antObjects) {
				List<Ant> visibleAnts = new LinkedList<Ant>();
				List<Sugar> visibleSugar = new LinkedList<Sugar>();
				List<Hill> visibleHills = new LinkedList<Hill>();
				List<Message> audibleMessages = new LinkedList<Message>();

				for (AntObject visibleAntObject : 
					spacePartitioning.antObjectsInsideCircle(antObject.getCaste().SIGHT_RANGE, antObject.getPosition())) {
					if (visibleAntObject != antObject) {
						visibleAnts.add(visibleAntObject.getAnt());
					}
				}

				for (SugarObject visibleSugarObject : 
					spacePartitioning.sugarObjectsInsideCircle(antObject.getCaste().SIGHT_RANGE, antObject.getPosition())) {
					visibleSugar.add(visibleSugarObject.getSugar());
				}
				
				for (HillObject visibleHillObject :
					spacePartitioning.hillObjectsInsideCircle(antObject.getCaste().SIGHT_RANGE, antObject.getPosition())) {
					visibleHills.add(visibleHillObject.getHill());
				}				
				
				for (MessageObject audibleMessageObject : 
					spacePartitioning.messageObjectsInsideCircle(antObject.getCaste().HEARING_RANGE, antObject.getPosition())) {
					if (audibleMessageObject.getSender().playerID == antObject.player.id) {
						audibleMessages.add(audibleMessageObject.getMessage());
					}
				}
				antObject.tick(visibleAnts, visibleSugar, visibleHills, audibleMessages);
			}
		}
		// Only do this now that we used last ticks message objects.
		spacePartitioning.discardMessageObjects();
		
		// execute all actions, ants get created
		for (Player player : players) {
			for (AntObject antObject : player.antObjects) {
				executeActionExceptMovement(antObject);
			} 
		}
		
		for (Player player : players) {
			for (AntObject antObject : player.antObjects) {
				executeMovement(antObject);
			}

			// order does matter since the queen creates new ants!
			executeAntOrders(player.queenObject);
		}
		
		// Needs to go before removing dead ants, because they need to be in 
		// the correct cell to be removed.
		spacePartitioning.update(); 
				

		// Let ants die!
		for (Player player : players) {
			for (Iterator<AntObject> antObjectIter = player.antObjects.iterator();
					antObjectIter.hasNext();) {
				AntObject maybeDead = antObjectIter.next();
				if (maybeDead.isDead()) {
					// hat neue Aktionen erzeugt.
					//executeLastWill(maybeDead);
					antObjectIter.remove();
					spacePartitioning.removeAntObject(maybeDead);
				}
			}
		}
		
		// remove empty sugar sources and decrease sugarObject.ticksToWait
		for (Iterator<SugarObject> sugarObjectIter = sugarObjects.iterator();
				sugarObjectIter.hasNext();) {
			SugarObject sugarObject = sugarObjectIter.next();
			
			sugarObject.tick();
			
			// remove if empty 
			if (sugarObject.getAmount() <= 0) {
				sugarObjectIter.remove();
				spacePartitioning.removeSugarObject(sugarObject);
			}
		}
		
	}

	private void executeAntOrders(QueenObject queenObject) {
		List<AntOrder> antOrders = queenObject.getAntOrders();
		Iterator<AntOrder> iterator = antOrders.iterator();
		final Player player = queenObject.player;
		
		while (iterator.hasNext()) {
			AntOrder antOrder = iterator.next();
		
			if (parameters.ANT_COST <= player.hillObject.getStoredFood()) {
				player.hillObject.changeStoredFoodBy(-parameters.ANT_COST);
				
				AntObject antObject = new AntObject(
						queenObject.player.hillObject.getPosition(),
						antOrder.getCaste(), antOrder.getAntAIClass(),
						queenObject.player, parameters);
				queenObject.player.addAntObject(antObject);
			}
		}
	}

	/** Führt die Aktion für das AntObject aus. 
	 * Beinhaltet Zucker abliefern.
	 *  */
	private void executeActionExceptMovement(AntObject actor) {
		Action action = actor.getAction();

		// Attack
		Ant targetAnt = action.attackTarget;
		actor.isAttacking = false;
		if (targetAnt != null) {
			if (parameters.distance(targetAnt.antObject.getPosition(), actor.getPosition()) 
					<= parameters.ATTACK_RANGE) {
				
				// main damage:
				AntObject target = targetAnt.antObject;
				target.takesDamage(actor.getCaste().ATTACK);
				actor.isAttacking = true;
				
				// collateral damage:
				for (AntObject closeAntObject : spacePartitioning.antObjectsInsideCircle(parameters.ATTACK_RANGE, target.getPosition())) {
					if (closeAntObject != target && closeAntObject.player != actor.player) {
						// TODO: Find a good rate, maybe depending on distance.
						closeAntObject.takesDamage(actor.getCaste().ATTACK*parameters.COLLATERAL_DAMAGE_FACTOR);
					}
				}
			}
		}

		// Drop sugar at the hill and reset ticksToLive if inside the hill.
		// TODO possible optimization: Use space partitioning for dropping sugar at the hill, don't test for all ants.
		if (parameters.distance(actor.player.hillObject.getPosition(), actor.getPosition())
				<= parameters.HILL_RADIUS) {
			actor.player.hillObject.changeStoredFoodBy(actor.getSugarCarry());
			actor.dropSugar();
			
			actor.resetTicksToLive();
		}
		
		// or drop sugar if desired
		if (action.dropItem == true) {
			actor.dropSugar();
		}
		
		// Pick up sugar
		Sugar sugar = action.sugarTarget;
		if (sugar != null) {
			if (parameters.distance(actor.getPosition(),sugar.sugarObject.getPosition())
					<= sugar.sugarObject.getRadius()) {
				actor.pickUpSugar(sugar.sugarObject);
			}
		}
		
		// Messages
		handleMessages(actor, action);
	}
	
	private void executeMovement(AntObject actor) {
		Action action = actor.getAction();

		if (actor.isAttacking) {
			actor.move(action.movement.boundLengthBy(actor.getCaste().SPEED_WHILE_ATTACKING));
		} else if (actor.getSugarCarry() > 0) {
			actor.move(action.movement.boundLengthBy(actor.getCaste().SPEED_WHILE_CARRYING_SUGAR));
		} else {
			actor.move(action.movement.boundLengthBy(actor.getCaste().SPEED));
		}
	}

	/*
	/** wird nur aufgerufen bevor die Ant stirbt -> kein Angriff mehr
	private void executeLastWill(AntObject actor) {
		Action action = actor.getAction();

		// Messages
		handleMessages(actor, action);
	}
	*/
	
	/** tests if victory condition is fulfilled
	 * @return is the victory condition fulfilled or can nobody win anymore? */
	public boolean checkVictoryCondition() {	
		int nPossibleWinners = players.size();
		for (Player player : players) {
			switch (LeftoverParameters.VICTORY_CONDITION) {
			case KILL_QUEEN:
				if (player.queenObject.isDead()) {
					player.hasLost = true;
					nPossibleWinners--;
				}
				else {
					player.hasLost = false;
				}
				break;

			case KILL_ANTS: // all dead or only queen is living
				if ( (player.antObjects.size() == 1 && !player.queenObject.isDead() ) ||
						player.antObjects.size() == 0) {
					player.hasLost = true;
					nPossibleWinners--;
				}
				else {
					player.hasLost = false;
				}
				break;
			}
		}
		
		return (nPossibleWinners <= 1);

	}
	
	/** 
	 * Assumes that checkVictoryCondition returns true.
	 * @return the player who won the game or null for draws. 
	 */
	public Player getWinner() {
		if (!checkVictoryCondition()) {
			System.err.println("getWinner() should only be called if checkVictoryCondition() return true!");
		}
		for (Player player : players) {
			if (player.hasLost == false) {
				return player;
			}
		}
		return null;
	}

	private void handleMessages(AntObject actor, Action action) {
		if (action.messageObject != null) {
			spacePartitioning.addMessageObject(action.messageObject);
			Message message = action.messageObject.getMessage();
			if (LeftoverParameters.DEBUG)
				System.out.println("\"" + message.content + "\" sagt "
						+ message.sender + ".");
		}
	}

	public void registerLogger(AbstractLogger logger) {
		registeredLoggers.add(logger);
	}

	private void notifyLoggers(AbstractLogger.LogEventType event) {
		for (AbstractLogger logger : registeredLoggers)
			logger.log(event);
	}

	public int totalNumberOfAntObjects() {
		int n = 0;
		for (Player player : players) {
			n += player.antObjects.size();
		}
		return n;
	}
	
	public List<Player> getPlayers() {
		return Collections.unmodifiableList(players); // TODO is it possible to ensure this statically?
	}
}
