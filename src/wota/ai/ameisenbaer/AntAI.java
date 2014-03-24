package wota.ai.ameisenbaer;

import wota.gamemaster.AIInformation;
import wota.gameobjects.Ant;
import wota.gameobjects.Message;
import wota.gameobjects.Snapshot;
import wota.utility.SeededRandomizer;

import wota.ai.ameisenbaer.behaviors.*;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class AntAI extends wota.gameobjects.AntAI {
	private final List<Behavior> behaviors;
	private final GameMap map;

	public AntAI() {
		behaviors = Arrays.<Behavior>asList(
				// If we attack while carrying, the sugar is dropped
				new OnlyIfNotCarryingBehavior(new AttackWeakestEnemyBehavior()),
				// Get the sugar home, if we have any
				new OnlyIfCarryingBehavior(new ReturnHomeBehavior()),
				new GatherSugarBehavior(),
				new MoveInDirectionBehavior(SeededRandomizer.getInt(360))
				);
		// Reverse the list since so that earlier behaviors in the above
		// list take precedence over later ones.
		Collections.reverse(behaviors);

		final AntAI self = this;
		map = new GameMap(new GameMap.InViewInterface() {
			@Override
			public boolean isInView(Snapshot target) {
				return self.isInView(target);
			}
		});
	}

	@Override
	public void tick() throws Exception {
		List<Ant> friends = Collections.unmodifiableList(visibleFriends());
		List<Ant> enemies = Collections.unmodifiableList(visibleEnemies());
		List<Message> messages = new ArrayList<Message>(audibleAntMessages);
		messages.add(audibleHillMessage);
		messages = Collections.unmodifiableList(messages);

		Action mapAction = map.tick(messages, visibleSugar);
		if (mapAction != null && mapAction.messageSnapshot != null)
			talk(mapAction.messageContent, mapAction.messageSnapshot);

		GameState state = new GameState(visibleSugar, visibleAnts, enemies, friends, visibleHills, messages, parameters, self, map);

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
