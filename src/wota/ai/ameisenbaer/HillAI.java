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

	@Override
	public void tick() throws Exception {
		createAnt(Caste.Gatherer, AntAI.class);

		Action mapAction = map.tick(new ArrayList<Message>(audibleAntMessages), Collections.<Sugar>emptyList());
		if (mapAction != null && mapAction.messageSnapshot != null)
			talk(mapAction.messageContent, mapAction.messageSnapshot);
	}
}
