/**
 * 
 */
package wota.ai.bienemaja;

import wota.gamemaster.AIInformation;
import wota.gameobjects.Caste;
import wota.utility.SeededRandomizer;

/**
 *  Give your information about this HillAI here.
 */
@AIInformation(creator = "Simon", name = "Bienenkoenigin")
public class HillAI extends MyHillAI {

	/*
	 * your Hill is not able to move but can
	 * communicate and create new ants. 
	 * 
	 * You can create new ants with				createAnt(caste, antAIClass)		
	 * e.g. if you want a gatherer and the AI
	 * you want use is called SuperGathererAI	createAnt(Caste.Gatherer, SuperGathererAI.class)
	 * 
	 */
	@Override
	public void tick() throws Exception {
		
		/* 
		 * try to create an Ant using the TemplateAI in every tick
		 * if you don't have enough food to create the ant your call
		 * will be ignored
		 */
		dowhatcanbedone();
		if(time==1) createAnt(Caste.Scout, Huepfer.class);
		
		for(int i=0; i<self.food/parameters.ANT_COST;i++){
			//createAnt(Caste.Soldier, Thekla.class);
			if(SeededRandomizer.getDouble()<1){//0.4*acceptance(time)+0.3){
				createAnt(Caste.Gatherer, BieneMaja.class);
			}else{
				createAnt(Caste.Soldier, Thekla.class);
			}
		
		say(0);
	}

}
	
	
}