package de.wota.gameobjects;

import java.util.List;

import de.wota.AntOrder;
import de.wota.ai.QueenAI;
import de.wota.utility.Vector;

public class QueenObject extends AntObject {
	public final QueenAI queenAI;
	
	public QueenObject(Vector position, Class<? extends QueenAI> queenAIClass, GameWorld.Player player) {
		super(position, Caste.Queen, queenAIClass, player);
		this.queenAI = (QueenAI) ai;
	}
	
	public List<AntOrder> getAntOrders() {
		return queenAI.popAntOrders();
	}
	
	public boolean isDead() {
		return (health <= 0);
	}
}
