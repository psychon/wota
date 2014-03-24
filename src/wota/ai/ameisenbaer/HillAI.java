package wota.ai.ameisenbaer;

import wota.gamemaster.AIInformation;
import wota.gameobjects.Caste;
import wota.gameobjects.Message;
import wota.gameobjects.Sugar;

import java.util.ArrayList;
import java.util.Collections;

@AIInformation(creator = "Uli", name = "The Enemy of Ants")
public class HillAI extends wota.gameobjects.HillAI {
	private final GameMap map = new GameMap(null);
	private int tick = 0;

	@Override
	public void tick() throws Exception {
		double myfood = self.food;
		while (parameters.ANT_COST < myfood) {
			myfood -= parameters.ANT_COST;
			tick++;

			switch (tick % 10) {
				case 8:
				case 9:
					createAnt(Caste.Soldier, AntAI.class);
					break;
				default:
					createAnt(Caste.Gatherer, AntAI.class);
					break;
			}
		}

		Action mapAction = map.tick(new ArrayList<Message>(audibleAntMessages), Collections.<Sugar>emptyList());
		if (mapAction != null && mapAction.messageSnapshot != null)
			talk(mapAction.messageContent, mapAction.messageSnapshot);
	}
}
