/**
* This file is based on and translated from the open source project: Sparcraft
* https://code.google.com/p/sparcraft/
* author of the source: David Churchill
**/
package bwmcts.mcts.uctcd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import bwmcts.mcts.NodeType;
import bwmcts.sparcraft.EvaluationMethods;
import bwmcts.sparcraft.Game;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Players;
import bwmcts.sparcraft.StateEvalScore;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.UnitActionTypes;
import bwmcts.sparcraft.players.Player;
import bwmcts.sparcraft.players.Player_Kite;
import bwmcts.sparcraft.players.Player_KiteDPS;
import bwmcts.sparcraft.players.Player_NoOverKillAttackValue;
import bwmcts.sparcraft.players.Player_Nothing;
import bwmcts.sparcraft.players.Player_Random;

public class UCTCD {

	private double K = 1.6f;
	private int maxChildren = 20;
	private int maxPlayerIndex = 0;
	private int minPlayerIndex = 1;
	private boolean debug;
	private int simulationSteps;
	
	private int NOK = 0;
	private int KITE = 0;
	private int RANDOM = 0;
	
	private boolean winRate;

	public UCTCD(double k, int maxChildren, int minPlayerIndex,
			int maxPlayerIndex, int simulationSteps, boolean debug, boolean winRate) {
		super();
		this.K = k;
		this.maxChildren = maxChildren;
		this.minPlayerIndex = minPlayerIndex;
		this.maxPlayerIndex = maxPlayerIndex;
		this.debug = debug;
		this.simulationSteps = simulationSteps;
		this.winRate = winRate;
	}

	public List<UnitAction> search(GameState state, long timeBudget){
		
		if (maxPlayerIndex == 0 && state.whoCanMove() == Players.Player_Two){
			System.out.println("Exit without computing");
			return new ArrayList<UnitAction>(); 
		} else if (maxPlayerIndex == 1 && state.whoCanMove() == Players.Player_One){
			System.out.println("Exit without computing");
			return new ArrayList<UnitAction>();
			
		}
		
		Date start = new Date();
		
		UctNode root = new UctNode(null, NodeType.ROOT, new ArrayList<UnitAction>(), maxPlayerIndex, "ROOT");
		root.setVisits(1);
		
		if (state.getTime()==0){
			System.out.println("UCT : NOK-AV=" + NOK + " KITE=" + KITE + " RANDOM=" + RANDOM);
			NOK=0;
			KITE=0;
			RANDOM=0;
		}
		
		int t = 0;
		while(new Date().getTime() <= start.getTime() + timeBudget){
			
			traverse(root, state.clone());
			t++;
			
		}
		
		//System.out.println(t + " iterarions.");
		UctNode best = mostVisitedChildOf(root);
		//UctNode best = withLabel(root, "NOK-AV");
		//UctNode best = bestValueChildOf(root);
		//System.out.println("Best value " + best.getTotalScore() / best.getVisits() + ".");
		//System.out.println(best.getLabel());
		
		if (best == null){
			System.out.println("NULL MOVE!");
			return new ArrayList<UnitAction>();
		}
		
		if (best.getLabel().equals("NOK-AV")){
			NOK++;
		} else if (best.getLabel().equals("KITE")){
			KITE++;
		}  else if (best.getLabel().equals("RANDOM")){
			RANDOM++;
		}
		
		if (debug){
			System.out.println(state._currentTime +  "\t" + (t++));
			
			String out = root.print(0);
			writeToFile(out, "tree.xml");
		}
		
		
		return best.getMove();
		
	}
	
	private float sigmoid(float x)
	{
	    return (float) (1 / (1 + Math.exp(-x)));
	}

	private float traverse(UctNode node, GameState state) {
		
		float score = 0f;
		if (node.getVisits() == 0){
			updateState(node, state, true);
			score = evaluate(state.clone());
		} else {
			updateState(node, state, false);
			if (state.isTerminal()){
				score = evaluate(state.clone());
			} else {
				if (node.getChildren().isEmpty())
					generateChildren(node, state);
				score = traverse(selectNode(node), state);
			}
		}
		node.setVisits(node.getVisits() + 1);
		
		if (winRate){
			if (score > 0)
				node.setTotalScore(node.getTotalScore() + 1);
			else if (score == 0)
				node.setTotalScore(node.getTotalScore() + 0.5f);
		} else {
			node.setTotalScore(node.getTotalScore() + score);
		}

		return score;
	}

	private float evaluate(GameState state) {
		
		long start = System.nanoTime();
		
		// get the players
	    Player p1 = new Player_NoOverKillAttackValue(Players.Player_One.ordinal());
	    Player p2 = new Player_NoOverKillAttackValue(Players.Player_Two.ordinal());

	    // contruct the game
	    Game g=new Game(state, p1, p2, simulationSteps, false);
	    
	    // play the game
	    g.play();
	    
	    // you can access the resulting game state after g has been played via getState
	    GameState finalState = g.getState();
	    // you can now evaluate the state however you wish. let's use an LTD2 evaluation from the point of view of player one
	    StateEvalScore score = finalState.eval(maxPlayerIndex, EvaluationMethods.LTD2);

	    long end = System.nanoTime();
	    
	    if (debug){
	    	//System.out.println("Sum:   " + (end - start));
	    }
		return score._val;
	}

	private void generateChildren(UctNode node, GameState state) {
		
		// Figure out who is next to move
		int playerToMove = getPlayerToMove(node, state);
		
		HashMap<Integer, List<UnitAction>> map = new HashMap<Integer, List<UnitAction>>();
		
		try {
			state.generateMoves(map, playerToMove);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Move ordering
		//shuffleMoveOrders(map);
		
		NodeType childType = getChildNodeType(node, state);
		
		// Add script moved
		
		//while(node.getChildren().size() < maxChildren){
		
		List<UnitAction> moveNok = new ArrayList<UnitAction>();
		new Player_NoOverKillAttackValue(playerToMove).getMoves(state, map, moveNok);
		UctNode childNok = new UctNode(node, childType, moveNok, playerToMove, "NOK-AV");
		node.getChildren().add(childNok);
		//}
		
		List<UnitAction> moveKite = new ArrayList<UnitAction>();
		new Player_KiteDPS(playerToMove).getMoves(state, map, moveKite);
		UctNode childKite = new UctNode(node, childType, moveKite, playerToMove, "KITE");
		node.getChildren().add(childKite);
		
		while(node.getChildren().size() < maxChildren){
			List<UnitAction> moveRandom = new ArrayList<UnitAction>();
			moveRandom = getNextMove(playerToMove, state, map); // Possible moves?
			UctNode childRandom = new UctNode(node, childType, moveRandom, playerToMove, "RANDOM");
			node.getChildren().add(childRandom);
		}
		
	}

	private void shuffleMoveOrders(HashMap<Integer, List<UnitAction>> map) {
		
		// Foreach unit
		for(Integer u : map.keySet()){
			
			int moveEnd = -1;
	        int moveBegin = -1;
	        
	        int numMoves = map.get(u).size();
	        
	        // reverse through the list of actions for this unit
	        for(int a = numMoves-1; a >= 0; --a){
				
	        	UnitActionTypes moveType = map.get(u).get(a)._moveType;
	        	
	            // mark the end of the move actions
	            if (moveEnd == -1 && (moveType == UnitActionTypes.MOVE))
	            {
	                moveEnd = a;
	            }
	            // mark the beginning of the MOVE unit actions
	            else if ((moveEnd != -1) && (moveBegin == -1) && (moveType != UnitActionTypes.MOVE))
	            {
	                moveBegin = a;
	            }
	            else if (moveBegin != -1)
	            {
	                break;
	            }
	        	
			}
	        
	     	// if we found the end but didn't find the beginning
	        if (moveEnd != -1 && moveBegin == -1)
	        {
	            // then the move actions begin at the beginning of the array
	            moveBegin = 0;
	        }

	        // shuffle the movement actions for this unit
	        if (moveEnd != -1 && moveBegin != -1 && moveEnd != moveBegin)
	        {
	        	List<UnitAction> moveActions = map.get(u).subList(moveBegin, moveEnd+1);
	        	/*
	        	for(UnitAction action : moveActions)
	        		map.get(u).remove(action);
	        	*/
	        	Collections.shuffle(moveActions);
	        	//map.get(u).addAll(moveActions);
	            //std::random_shuffle(&_moves[u][moveBegin], &_moves[u][moveEnd]);
	            //resetMoveIterator();
	        }
			
		}
		
	}

	private int getPlayerToMove(UctNode node, GameState state) {
		
		if (state.whoCanMove() == Players.Player_Both){
		
			if (node.getType() == NodeType.ROOT)
				
				return maxPlayerIndex;
			
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
	
	private NodeType getChildNodeType(UctNode parent, GameState prevState) {
		
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

	private List<UnitAction> getNextMove(int playerToMove, GameState state, HashMap<Integer, List<UnitAction>> map) {
		
		ArrayList<UnitAction> move = new ArrayList<UnitAction>();
		
		Random r = new Random();
		for(Integer i : map.keySet()){
			
			// Add random possible action
			move.add(map.get(i).get(r.nextInt(map.get(i).size())));
			
		}
		
		return move;
		
	}

	private void updateState(UctNode node, GameState state, boolean leaf) {
		
		if (node.getType() != NodeType.FIRST || leaf){
			
			if (node.getType() == NodeType.SECOND){
				
				try {
					state.makeMoves(node.getParent().getMove());
				} catch (Exception e) {e.printStackTrace();}
			}
			
			try {
				state.makeMoves(node.getMove());
			} catch (Exception e) {e.printStackTrace();}
			
			state.finishedMoving();
			
		}
		
	}

	private UctNode selectNode(UctNode parent) {
		
		float bestScore = Float.MAX_VALUE;
		//.getChildren().get(0)
		boolean maxPlayer = parent.getMovingPlayerIndex() == maxPlayerIndex;
		if (maxPlayer)
			bestScore = -Float.MAX_VALUE;
			
		UctNode bestNode = null;
		for(UctNode child : parent.getChildren()){
			
			if (child.getVisits() > 0){
				
	            float score = child.getTotalScore() / child.getVisits();
	            if (!winRate)
	            	score = sigmoid(score);
	            float uctVal = (float) (K * Math.sqrt(Math.log(parent.getVisits()) / child.getVisits()));
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
	

	private void writeToFile(String out, String filename) {
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


	private UctNode mostVisitedChildOf(UctNode parent) {
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
	
	private UctNode bestValueChildOf(UctNode parent) {
		float bestValue = maxPlayerIndex == 0 ? -100000f : 100000f;
		UctNode best = null;
		for(UctNode node : parent.getChildren()){
			if (node.getTotalScore()/node.getVisits()>bestValue && node.getVisits() > 0){
				best = node;
				bestValue = node.getTotalScore()/node.getVisits();
			}
		}
		return best;
	}
	

	private UctNode withLabel(UctNode parent, String label) {
		for(UctNode node : parent.getChildren()){
			if (node.getLabel().equals(label)){
				return node;
			}
		}
		return null;
	}

	public double getK() {
		return K;
	}

	public void setK(double k) {
		K = k;
	}

	public int getMaxChildren() {
		return maxChildren;
	}

	public void setMaxChildren(int maxChildren) {
		this.maxChildren = maxChildren;
	}

	public int getMinPlayerIndex() {
		return minPlayerIndex;
	}

	public void setMinPlayerIndex(int minPlayerIndex) {
		this.minPlayerIndex = minPlayerIndex;
	}

	public int getMaxPlayerIndex() {
		return maxPlayerIndex;
	}

	public void setMaxPlayerIndex(int maxPlayerIndex) {
		this.maxPlayerIndex = maxPlayerIndex;
	}
	
}