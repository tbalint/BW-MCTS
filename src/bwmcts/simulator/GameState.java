package bwmcts.simulator;

import java.util.ArrayList;
import java.util.List;

import javabot.JNIBWAPI;

/**
 * Game state representation which has to implement the imitate method which translates the 
 * actual Brood War game state to the abstract representation of the Game State implementation.
 * 
 * @author Niels
 *
 */
public abstract class GameState {

	int gameTime;
	List<List<Unit>> unitSets;
	Map map;
	
	public GameState(int gameTime, Map map) {
		super();
		this.gameTime = gameTime;
		this.map = map;
		
		this.unitSets = new ArrayList<List<Unit>>();
		for(int i = 0; i < 10; i++)
			this.unitSets.add(new ArrayList<Unit>());
	}
	
	/**
	 * Imitates the actual Brood War game state.
	 * @param bwapi
	 * 		The BWAPI representing the actual game state.
	 */
	public abstract void imitate(JNIBWAPI bwapi);

	public int getGameTime() {
		return gameTime;
	}

	public void setGameTime(int gameTime) {
		this.gameTime = gameTime;
	}
	
	public Map getMap() {
		return map;
	}

	public void setMap(Map map) {
		this.map = map;
	}

	/**
	 * @param playerIndex
	 * 		The player index of the owner
	 * @return
	 * 		The units owned by the owner
	 */
	public List<Unit> getUnits(int playerIndex) {
		return unitSets.get(playerIndex);
	}

	/**
	 * 
	 * @param playerIndex
	 * 		The player index of the owner
	 * @param units
	 * 		The units of the owner
	 */
	public void setUnits(int playerIndex, List<Unit> units) {
		this.unitSets.set(playerIndex, units);
	}
	
}
