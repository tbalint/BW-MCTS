package bwmcts.simulator;

import java.util.ArrayList;
import java.util.List;

import bwmcts.simulator.actions.Action;

import javabot.JNIBWAPI;

/**
 * Game state representation which has to implement the imitate method which translates the 
 * actual Brood War game state to the abstract representation of the Game State implementation.
 * 
 * @author Niels
 *
 */
public class GameState {

	int gameTime;
	List<List<Unit>> unitSets;
	List<Move> nextMoves;
	boolean terminal;
	
	public GameState(int gameTime) {
		super();
		this.gameTime = gameTime;
		
		this.unitSets = new ArrayList<List<Unit>>();
		for(int i = 0; i < 10; i++)
			this.unitSets.add(new ArrayList<Unit>());
		
		this.nextMoves = new ArrayList<Move>();
		this.terminal = false;
	}
	
	/**
	 * Imitates the actual Brood War game state.
	 * @param bwapi
	 * 		The BWAPI representing the actual game state.
	 */
	public void imitate(JNIBWAPI bwapi){
		
		
		
	}
	
	
	public GameState clone(){
		
		GameState clone = new GameState(gameTime);
		clone.setGameTime(gameTime);
		clone.setNextMoves(new ArrayList<Move>());
		for(Move move : nextMoves){
			clone.getNextMoves().add(new Move());
		}
		clone.setTerminal(false);
		for(int i = 0; i < unitSets.size(); i++)
			for(Unit unit : unitSets.get(i))
				clone.getUnits(i).add((Unit) unit.clone());
		
		return clone;
	}

	public List<List<Unit>> getUnitSets() {
		return unitSets;
	}

	public void setUnitSets(List<List<Unit>> unitSets) {
		this.unitSets = unitSets;
	}

	public List<Move> getNextMoves() {
		return nextMoves;
	}

	public void setNextMoves(List<Move> nextMoves) {
		this.nextMoves = nextMoves;
	}

	public void setTerminal(boolean terminal) {
		this.terminal = terminal;
	}

	public int getGameTime() {
		return gameTime;
	}

	public void setGameTime(int gameTime) {
		this.gameTime = gameTime;
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

	public void applyMoves(Move move) {
		
		nextMoves.add(move);
		
	}

	public void makeMove() {
		
		// Simulate game one step using nextMoves
		nextMoves.clear();
		
	}

	public boolean isTerminal() {
		
		return terminal;
	}

	public float evaluate() {
		
		// Run simulation
		return (float) Math.random();
	}

	public boolean bothCanMove() {
		return true;
	}

	public int whoCanMove() {

		if (Math.random() >= 0.5f)
			return 0;
		
		return 1;
	}
	
	
}
