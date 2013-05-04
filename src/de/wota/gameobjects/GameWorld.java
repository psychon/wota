package de.wota.gameobjects;

import java.util.Random;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

import de.wota.Vector;
import de.wota.Action;
import de.wota.AntOrder;
import de.wota.Player;
import de.wota.ai.AntAI;

/**
 * Enthält alle Elemente der Spielwelt.
 * 
 * @author pascal
 */
public class GameWorld {

	public final List<Player> players = new LinkedList<Player>();
	private LinkedList<SugarObject> sugarObjects;

	public void tick() {
		
		// create Ants for all AntObjects and sets them in the AntAI
		// (the latter happens in AntObject.createAnt() )
		// also create Sugar for SugarObjects
		for (Player player : players) {
			for (AntObject antObject : player.antObjects) {
				antObject.createAnt();
			}
		}
		for (SugarObject sugarObject : sugarObjects) {
			sugarObject.createSugar();
		}
		
		// call tick for all AntObjects
		for (Player player : players) {
			for (AntObject antObject : player.antObjects) {
				LinkedList<Ant> visibleAnts = new LinkedList<Ant>();
				LinkedList<Sugar> visibleSugar = new LinkedList<Sugar>();

				// TODO pass visibleAnts and visibleSugar
				antObject.tick(visibleAnts, visibleSugar);
			}
		}

		// execute all actions
		for (Player player : players) {
			for (AntObject antObject : player.antObjects) {
				executeAction(antObject);
			}
			// order does matter since the hill creates new ants!
			executeAction(player.hillObject); 
		}
		
		// let Ants die!
		for (Player player : players) {
			//LinkedList<AntObject> antObjectsToDie = new LinkedList<AntObject>();

			for (Iterator<AntObject> antObjectIter = player.antObjects.iterator(); antObjectIter.hasNext();) {
				if (antObjectIter.next().isDying()) {
					antObjectIter.remove();
				}
			}
		}

		
	}

	private static void executeAction(HillObject hill) {
		// can only produce units
		List<AntOrder> antOrders = hill.getAntOrders();
		for (AntOrder antOrder : antOrders) {
			AntObject antObject = 
					new AntObject(
						hill.getPosition(),
						antOrder.getCaste(),
						antOrder.getAntAIClass()
					);
			hill.getPlayer().antObjects.add(antObject);
		}
	}
	
	/** führt die Aktion für das AntObject aus */
	private static void executeAction(AntObject actor) {
		Action action = actor.getAction();
		
		// TODO remove this test ----------------------
		Random random = new Random();
		actor.takesDamage(3*random.nextDouble());
		// --------------------------------------------
		
		// Attack
		// TODO add collateral damage
		Ant targetAnt = action.getAttackTarget();
		if (targetAnt != null) {
			// TODO check if target is in range.
			AntObject target = targetAnt.antObject;
			target.takesDamage(actor.getAttack());
		}
		
		// Movement
		actor.move(Vector.fromPolar(action.getMovementDistance(), action.getMovementDirection()));
		
		// Messages
		// TODO Messages
	}
}
