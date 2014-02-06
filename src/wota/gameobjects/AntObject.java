package wota.gameobjects;

import java.util.List;

import wota.gameobjects.Parameters;
import wota.utility.SeededRandomizer;
import wota.utility.Vector;


/**
 * Interne Darstellung von Ants. Enthält alle Informationen.
 * Im Gegensatz dazu enthält Ant nur die Informationen, welche die KI sehen darf.
 */
public class AntObject extends GameObject{
	
	private static int idCounter = 0;
	private Ant ant;
	protected final AntAI antAi;
	public final int id;
	protected double health;
	private double speed;
	private double lastMovementDirection = 0;
	
	/** amount of sugar carried now */
	private int sugarCarry = 0;
	
	/** Angriffspunkte */
	private Action action;
	private final Caste caste;
	public final GameWorld.Player player;
	private boolean isAttacking = false;
	private AntObject attackTarget = null;
	private int ticksUntilDecay = Integer.MAX_VALUE; // counts down the ticks after Ant has died until its corpse will be removed
	
	public AntObject(Vector position, Caste caste, Class<? extends AntAI> antAIClass, GameWorld.Player player, Parameters parameters) {
		super(position, parameters);
		
		this.player = player;
		this.id = getNewID();
		AntAI antAI = null;
		try {
			antAI = antAIClass.newInstance(); 
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not create AntAI -> exit");
			System.exit(1);
		}
		antAI.setParameters(parameters);
		this.caste = caste;
		// set parameters
		health = caste.INITIAL_HEALTH;
		speed = caste.SPEED;
		
		this.antAi = antAI;
		this.antAi.setAntObject(this);
	}

	public AntAI getAI() {
		return antAi;
	}
	
	public Ant getAnt() {
		return ant;
	}
	
	public Caste getCaste() {
		return caste;
	}

	public double getHealth() {
		return health;
	}
	
	public AntObject getAttackTarget() {
		if(isAttacking) {
			return attackTarget;
		}
		else
			return null;
	}
	
	void setAttackTarget(AntObject target) {
		if (target == null) {
			isAttacking = false;
			attackTarget = null;
		}
		else {
			isAttacking = true;
			attackTarget = target;
		}
	}

	public double getSpeed() {
		return speed;
	}
	
	public Action getAction() {
		return action;
	}
	
	public int getSugarCarry() {
		return sugarCarry;
	}
	
	public double getLastMovementDirection() {
		return lastMovementDirection;
	}
	
	public void createAnt() {
		ant = new Ant(this);
		this.antAi.setAnt(ant);
	}
	
	public void takesDamage(double attack) {
		double takenDamage;
		if (isCarrying()) {
			takenDamage = parameters.VULNERABILITY_WHILE_CARRYING * attack;
		}
		else {
			takenDamage = attack;
		}
			
		health = health - takenDamage;
	}
	
	public void pickUpSugar(SugarObject sugarObject) {
		int oldAmountOfSugarCarried = sugarCarry;
		sugarCarry = Math.min(caste.MAX_SUGAR_CARRY, sugarCarry + sugarObject.getAmount());
		sugarObject.decreaseSugar(sugarCarry - oldAmountOfSugarCarried);		 
	}
	
	/** sets amount of carried sugar to 0 */
	public void dropSugar() {
		sugarCarry = 0;
	}
	
	/** Checks if AntObject is already dead. In this case it will decay after CORPSE_DECAY_TIME */
	public boolean isDead() {
		return health <= 0;
	}
	
	/** calls ai.tick(), handles exceptions and saves the action */
	public void tick(List<Ant> visibleAnts, List<Ant> visibleCorpses, List<Sugar> visibleSugar, 
			List<Hill> visibleHills, List<AntMessage> incomingAntMessages, HillMessage incomingHillMessage) {
		if (!isDead()) {
			antAi.visibleAnts = visibleAnts;
			antAi.visibleCorpses = visibleCorpses;
			antAi.visibleSugar = visibleSugar;
			antAi.visibleHills = visibleHills;
			antAi.audibleAntMessages = incomingAntMessages;
			antAi.audibleHillMessage = incomingHillMessage;
			antAi.setPosition(getPosition());
			
			try {
				antAi.tick();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			action = antAi.popAction();
		}
		else { // is dead
			ticksUntilDecay--;
		}
	}
	
	/** if true, AntObject can be entirely removed */
	public boolean isDecayed() {
		return ticksUntilDecay <= 0;
	}

	/** true if AntObject is carrying sugar */
	public boolean isCarrying() {
		return sugarCarry > 0;
	}

	@Override
	public void move(Vector moveVector) {
		Vector realMovement = moveVector;
		if (moveVector.length() != 0) {
			double angleError = parameters.ANGLE_ERROR_PER_DISTANCE * moveVector.length() 
					* 2 * (SeededRandomizer.getDouble() - 0.5); 
			realMovement = Vector.fromPolar(moveVector.length(), moveVector.angle() + angleError);
		}
		lastMovementDirection = realMovement.angle();
		super.move(realMovement);
	}
	
	/** get new id for antObject */
	private static int getNewID() {
		idCounter++;
		return idCounter - 1;
	}
	
	/** gets called when AntObject is dying */
	public void die() {
		ticksUntilDecay = parameters.CORPSE_DECAY_TIME;
		action = new Action();
	}

	/** returns true iff ant is dead and did not start to decay  */
	public boolean diedLastTick() {
		return isDead() && ticksUntilDecay == Integer.MAX_VALUE;
	}

}
