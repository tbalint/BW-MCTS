/**
* This file is based on and translated from the open source project: Sparcraft
* https://code.google.com/p/sparcraft/
* author of the source: David Churchill
**/
package bwmcts.uct;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import bwmcts.uct.NodeType;
import bwmcts.sparcraft.EvaluationMethods;
import bwmcts.sparcraft.Game;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Players;
import bwmcts.sparcraft.StateEvalScore;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.players.Player;
import bwmcts.sparcraft.players.Player_NoOverKillAttackValue;

public abstract class UCT {
	
	protected UctConfig config;
	protected UctStats stats;

	public UCT(UctConfig config, UctStats stats) {
		super();
		this.config = config;
		this.stats = stats;
	}

	public abstract List<UnitAction> search(GameState state, long timeBudget);
		
	protected float evaluate(GameState state) {
		
		// get the players
	    Player p1 = new Player_NoOverKillAttackValue(Players.Player_One.ordinal());
	    Player p2 = new Player_NoOverKillAttackValue(Players.Player_Two.ordinal());

	    // contruct the game
	    Game g=new Game(state, p1, p2, config.getSimulationSteps(), false);
	    
	    // play the game
	    g.play();
	    
	    // you can access the resulting game state after g has been played via getState
	    GameState finalState = g.getState();
	    // you can now evaluate the state however you wish. let's use an LTD2 evaluation from the point of view of player one
	    StateEvalScore score = finalState.eval(config.getMaxPlayerIndex(), EvaluationMethods.LTD2);

		return score._val;
	}

	protected int getPlayerToMove(UctNode node, GameState state) {
		
		if (state.whoCanMove() == Players.Player_Both){
		
			if (node.getType() == NodeType.ROOT)
				
				return config.getMaxPlayerIndex();
			
			if (node.getType() == NodeType.FIRST)
				
				return GameState.getEnemy(node.getMovingPlayerIndex());
			
			return node.getMovingPlayerIndex();
			
		}
		
		if (state.whoCanMove() == Players.Player_One)
			return 0;
		else if (state.whoCanMove() == Players.Player_Two)
			return 1;
		
		return -1;
		
	}
	
	protected NodeType getChildNodeType(UctNode parent, GameState prevState) {
		
		if(!prevState.bothCanMove()){
			
			return NodeType.SOLO;
			
		} else { 
			
			if (parent.getType() == NodeType.ROOT)
		
				return NodeType.FIRST;
			
			if (parent.getType() == NodeType.SOLO)
				
				return NodeType.FIRST;
			
			if (parent.getType() == NodeType.SECOND)
				
				return NodeType.FIRST;
			
			if (parent.getType() == NodeType.FIRST)
				
				return NodeType.SECOND;
		}
			
		return NodeType.DEFAULT;
	}


	protected void updateState(UctNode node, GameState state, boolean leaf) {
		
		if (node.getType() == NodeType.ROOT){
			return;
		}
		
		if (node.getType() != NodeType.FIRST || leaf){
			
			if (node.getType() == NodeType.SECOND){
				
				try {
					state.makeMoves(node.getParent().getMove());
				} catch (Exception e) {e.printStackTrace();}
			}
			
			try {
				state.makeMoves(node.getMove());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			state.finishedMoving();
			
		}
		
	}

	protected UctNode selectNode(UctNode parent) {
		
		float bestScore = Float.MAX_VALUE;
		boolean maxPlayer = parent.getMovingPlayerIndex() == config.getMaxPlayerIndex();
		if (maxPlayer)
			bestScore = -Float.MAX_VALUE;
			
		UctNode bestNode = null;
		for(UctNode child : parent.getChildren()){
			
			if (child.getVisits() > 0){
				
	            float score = child.getTotalScore() / child.getVisits();
	            if (config.isLTD2())
	            	score = sigmoid(score);
	            float uctVal = (float) (config.getK() * Math.sqrt(Math.log(parent.getVisits()) / child.getVisits()));
	            float currentVal = maxPlayer ? (score + uctVal) : (score - uctVal);
	            
	            child.setUctValue(currentVal);
				
			} else {
				
				return child;
				
			}
			
			if (maxPlayer && child.getUctValue() > bestScore){
				bestScore = child.getUctValue();
				bestNode = child;
			}
			if (!maxPlayer && child.getUctValue() < bestScore){
				bestScore = child.getUctValue();
				bestNode = child;
			}
			
		}
		
		return bestNode;
	}
	
	protected UctNode mostVisitedChildOf(UctNode parent) {
		int mostVisits = -1;
		UctNode best = null;
		for(UctNode node : parent.getChildren()){
			if (node.getVisits()>mostVisits){
				best = node;
				mostVisits = node.getVisits();
			}
		}
		return best;
	}
	
	protected UctNode bestValueChildOf(UctNode parent) {
		float bestValue = config.getMaxPlayerIndex() == 0 ? -100000f : 100000f;
		UctNode best = null;
		for(UctNode node : parent.getChildren()){
			if (node.getTotalScore()/node.getVisits()>bestValue && node.getVisits() > 0){
				best = node;
				bestValue = node.getTotalScore()/node.getVisits();
			}
		}
		return best;
	}
	
	protected void writeToFile(String out, String filename) {
		
		// Write to file
		System.out.println("Ready to write file.");
        FileWriter fw = null;
		try {
			File old = new File(filename);
			if (old.exists()){
				old.delete();
				System.out.println(filename + " deleted");
			}
			File file = new File(filename);
			fw = new FileWriter(file);
			fw.write(out);
			fw.close();
			System.out.println(filename + " saved.");
		} catch (FileNotFoundException e1) {
			System.out.println("Error saving " + filename + ". " + e1);
		} catch (IOException e2) {
			System.out.println("Error saving " + filename + ". " + e2);
		}
	}
	
	protected float sigmoid(float x){
	    return (float) (1 / (1 + Math.exp(-x)));
	}
	
	
	public String toString(){
		return "UCT - "+this.config.toString();
	}
	
}