/**
 * 
 */
package wota.ai.firstopponent;

import wota.gamemaster.AIInformation;
import wota.gameobjects.Caste;

/**
 *  Give your information about this HillAI here.
 */
@AIInformation(creator = "Wota Team", name = "A first opponent.")
public class HillAI extends wota.gameobjects.HillAI {
	@Override
	public void tick() throws Exception {
		int antsToProduce = (int) (self.food / parameters.ANT_COST);
		for (int i=0; i<antsToProduce; i++) {
			createAnt(Caste.Gatherer, GathererAI.class);
		}
	}
}
