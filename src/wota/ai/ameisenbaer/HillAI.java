package wota.ai.ameisenbaer;

import wota.gamemaster.AIInformation;
import wota.gameobjects.Ant;
import wota.gameobjects.Caste;
import wota.gameobjects.Hill;
import wota.gameobjects.Message;
import wota.gameobjects.Sugar;
import wota.utility.Vector;

import java.util.ArrayList;
import java.util.Collections;

@AIInformation(creator = "Uli", name = "The Enemy of Ants")
public class HillAI extends wota.gameobjects.HillAI {
	private final GameMap map = new GameMap();
	private int tick = 0;

	protected boolean haveVisibleEnemy() {
		for (Ant ant : visibleAnts) {
			if (ant.playerID != self.playerID) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void tick() throws Exception {
		double myfood = self.food;
		while (parameters.ANT_COST <= myfood) {
			myfood -= parameters.ANT_COST;
			tick++;

			if (tick % 20 > 15 || (haveVisibleEnemy() && tick % 2 == 0))
				createAnt(Caste.Soldier, AntAI.class);
			else
				createAnt(Caste.Gatherer, AntAI.class);
		}

		GameState state = new GameState(Collections.<Sugar>emptyList(), Collections.<Ant>emptyList(), Collections.<Ant>emptyList(),
				Collections.<Ant>emptyList(), Collections.<Hill>emptyList(), new ArrayList<Message>(audibleAntMessages),
				parameters, self, map, new Vector(0, 0));
		Action mapAction = map.tick(state);
		if (mapAction != null && mapAction.messageSnapshot != null)
			talk(mapAction.messageContent, mapAction.messageSnapshot);
	}
}
