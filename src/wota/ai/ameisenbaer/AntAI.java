package wota.ai.ameisenbaer;

import wota.gamemaster.AIInformation;
import wota.gameobjects.Ant;
import wota.gameobjects.Message;
import wota.gameobjects.Snapshot;
import wota.gameobjects.Sugar;
import wota.gameobjects.Hill;
import wota.utility.SeededRandomizer;

import wota.ai.ameisenbaer.behaviors.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AntAI extends wota.gameobjects.AntAI {
	private List<Behavior> behaviors = null;
	private final GameMap map = new GameMap();

	private void pickBehavior() {
		double direction = 360 * self.id / (parameters.STARTING_FOOD / parameters.ANT_COST);
		switch (self.caste) {
			default:
			case Gatherer:
				behaviors = Arrays.<Behavior>asList(
						// If we attack while carrying, the sugar is dropped
						new OnlyIfNotCarryingBehavior(new AttackWeakestEnemyBehavior()),
						// Attack if we are ten times as strong,
						// flee if they are stronger
						new OnlyIfNotCarryingBehavior(new AttackOrFleeBehavior(0.1, 1)),
						// When carrying, always flee
						new OnlyIfCarryingBehavior(new AttackOrFleeBehavior(0, 0.4)),
						// Get the sugar home, if we have any
						new OnlyIfCarryingBehavior(new ReturnHomeBehavior()),
						// Avoid splash damage
						new OnlyIfNotCarryingBehavior(new AvoidCrowd(direction)),
						// Get us some sugar to bring home
						new GatherSugarBehavior(),
						// As fall back, go into a random direction
						new MoveInDirectionBehavior(direction)
						);
				break;
			case Scout:
				behaviors = Arrays.<Behavior>asList(
						// Never attack, always flee
						new AttackOrFleeBehavior(0, 0.9),
						// Avoid splash damage
						new AvoidCrowd(direction),
						new MoveInDirectionBehavior(direction)
						);
				break;
			case Soldier:
				behaviors = Arrays.<Behavior>asList(
						new AttackWeakestEnemyBehavior(),
						// Attack if we are stronger,
						// flee if they are twice as strong
						new AttackOrFleeBehavior(1, 0.5),
						// Avoid splash damage
						new AvoidCrowd(direction),
						new FollowHealthierBehavior(),
						new CampBehavior(4),
						new MoveInDirectionBehavior(direction)
						);
				break;
		}
		// Reverse the list since so that earlier behaviors in the above
		// list take precedence over later ones.
		Collections.reverse(behaviors);
	}

	@Override
	public void tick() throws Exception {
		// Grml, this can't be done in constructor :-(
		if (behaviors == null)
			pickBehavior();

		Collection<Sugar> sugar = new SnapshotSet<Sugar>(visibleSugar);
		Collection<Ant> ants = new SnapshotSet<Ant>(visibleAnts);
		Collection<Ant> enemies = new SnapshotSet<Ant>(visibleEnemies());
		Collection<Ant> friends = new SnapshotSet<Ant>(visibleFriends());
		Collection<Hill> hills = new SnapshotSet<Hill>(visibleHills);
		Collection<Message> messages = new ArrayList<Message>(audibleAntMessages);
		messages.add(audibleHillMessage);
		messages = Collections.unmodifiableCollection(messages);

		GameState state = new GameState(sugar, ants, enemies, friends, hills, messages, parameters,
				self, map, vectorToHome());

		Action mapAction = map.tick(state);
		if (mapAction != null && mapAction.messageSnapshot != null)
			talk(mapAction.messageContent, mapAction.messageSnapshot);

		// Execute all behaviors
		for (Behavior behavior : behaviors) {
			Action action = behavior.tick(state);
			if (action == null)
				continue;
			if (action.sugarTarget != null)
				pickUpSugar(action.sugarTarget);
			if (action.attackTarget != null)
				attack(action.attackTarget);
			if (action.moveDirectionSet)
				moveInDirection(action.moveDirection, action.moveLength);
			if (action.moveHome)
				moveHome();
			if (action.messageSnapshot != null)
				talk(action.messageContent, action.messageSnapshot);
		}
	}

}
