/**
* This file is an extension to code based on and translated from the open source project: Sparcraft
* https://code.google.com/p/sparcraft/
* author of the source: David Churchill
**/
package bwmcts.mcts.iuct;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import bwmcts.mcts.NodeType;
import bwmcts.mcts.UnitState;
import bwmcts.mcts.UnitStateTypes;
import bwmcts.sparcraft.EvaluationMethods;
import bwmcts.sparcraft.Game;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Players;
import bwmcts.sparcraft.StateEvalScore;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.players.Player;
import bwmcts.sparcraft.players.Player_AttackClosest;
import bwmcts.sparcraft.players.Player_Defense;
import bwmcts.sparcraft.players.Player_Kite;
import bwmcts.sparcraft.players.Player_KiteDPS;
import bwmcts.sparcraft.players.Player_NoOverKillAttackValue;

public class IUCTCD {

	private double K = 1.6f;
	private int maxChildren = 20;
	private int maxPlayerIndex = 0;
	private int minPlayerIndex = 1;
	private boolean debug;
	private int simulationSteps;
	
	private int NOK = 0;
	private int KITE = 0;
	private int RANDOM = 0;
	
	public IUCTCD() {
		
	}

	public IUCTCD(double k, int maxChildren, int minPlayerIndex,
			int maxPlayerIndex, int simulationSteps, boolean debug) {
		super();
		this.K = k;
		this.maxChildren = maxChildren;
		this.minPlayerIndex = minPlayerIndex;
		this.maxPlayerIndex = maxPlayerIndex;
		this.debug = debug;
		this.simulationSteps = simulationSteps;
	}

	public List<UnitAction> search(GameState state, long timeBudget){
		
		if (maxPlayerIndex == 0 && state.whoCanMove() == Players.Player_Two){
			return new ArrayList<UnitAction>(); 
		} else if (maxPlayerIndex == 1 && state.whoCanMove() == Players.Player_One){
			return new ArrayList<UnitAction>(); 
		}
		
		Date start = new Date();
		
		IuctNode root = new IuctNode(null, NodeType.ROOT, new ArrayList<UnitState>(), maxPlayerIndex, "ROOT");
		root.setVisits(1);
		
		if (state.getTime()==0){
			System.out.println("IUCT : NOK-AV=" + NOK + " KITE=" + KITE + " RANDOM=" + RANDOM);
			NOK=0;
			KITE=0;
			RANDOM=0;
		}
		
		int t = 0;
		while(new Date().getTime() <= start.getTime() + timeBudget){
			
			traverse(root, state.clone());
			t++;
			/*
			String out = root.print(0);
			writeToFile(out, "tree.xml");
			*/
		}
		
		IuctNode best = mostVisitedChildOf(root);
		//IuctNode best = mostWinningChildOf(root);
		
		if (debug){
			System.out.println(state._currentTime +  "\t" + (t++));
			String out = root.print(0);
			writeToFile(out, "tree.xml");
		}
		
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
		
		/*
		System.out.print("i:");
		for(UnitState s : best.getMove())
			System.out.print(s.type);
		System.out.print("\n");
		*/
		List<UnitAction> actions = statesToActions(best.getMove(), state.clone());
		//System.out.println(state._currentTime +  "\t" + (t++));
		return actions;
		
	}

	private float traverse(IuctNode node, GameState state) {
		
		float score = 0;
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
		//if (score > 0)
		//	node.setTotalScore(node.getTotalScore() + 1);
	    //else if (score == 0)
	    //	node.setTotalScore(node.getTotalScore() + 0.5f);
		node.setTotalScore(node.getTotalScore() + score);
		
		return score;
	}
	
	private float sigmoid(float x)
	{
	    return (float) (1 / (1 + Math.exp(-x)));
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

	private void generateChildren(IuctNode node, GameState state) {
		
		// Figure out who is next to move
		int playerToMove = getPlayerToMove(node, state);
		
		HashMap<Integer, List<UnitAction>> map = new HashMap<Integer, List<UnitAction>>();
		try {
			state.generateMoves(map, playerToMove);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		NodeType childType = getChildNodeType(node, state);
		
		List<UnitState> moveAttack = new ArrayList<UnitState>();
		moveAttack.addAll(getAllMove(UnitStateTypes.ATTACK, map));
		IuctNode childAttack = new IuctNode(node, childType, moveAttack, playerToMove, "NOK-AV");
		node.getChildren().add(childAttack);
		
		List<UnitState> moveKite = new ArrayList<UnitState>();
		moveKite.addAll(getAllMove(UnitStateTypes.KITE, map));
		IuctNode childKite = new IuctNode(node, childType, moveKite, playerToMove, "KITE");
		node.getChildren().add(childKite);
		
		int e = 2;
		while(node.getChildren().size() < maxChildren && e < maxChildren){
			List<UnitState> moveRandom = getRandomMove(playerToMove, map);
			e++;
			if (uniqueMove(moveRandom, node)){
				IuctNode child = new IuctNode(node, childType, moveRandom, playerToMove, "RANDOM");
				node.getChildren().add(child);
			}
		}
		
	}

	private List<UnitState> getAllMove(UnitStateTypes type, HashMap<Integer, List<UnitAction>> map) {

		List<UnitState> states = new ArrayList<UnitState>();
		
		for(Integer i : map.keySet()){
			
			List<UnitAction> actions = map.get(i);
			if (actions.isEmpty())
				continue;
			
			UnitState state = new UnitState(type, actions.get(0)._unit, actions.get(0)._player);
			states.add(state);
			
		}
		
		return states;
	}
	
	private List<UnitState> getRandomMove(int playerToMove, HashMap<Integer, List<UnitAction>> map) {
		
		ArrayList<UnitState> move = new ArrayList<UnitState>();
		
		for(Integer i : map.keySet()){
			
			// Skip empty actions
			List<UnitAction> actions = map.get(i);
			if (actions.isEmpty())
				continue;
			
			// Random state
			UnitStateTypes type = UnitStateTypes.ATTACK;
			if (Math.random() >= 0.5f)
				type = UnitStateTypes.KITE;
			
			UnitState unitState = new UnitState(type, i, playerToMove);
			
			// Add random possible action
			move.add(unitState);
			
		}
		
		return move;
		
	}

	private int getPlayerToMove(IuctNode node, GameState state) {
		
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
	
	private boolean uniqueMove(List<UnitState> move, IuctNode node) {

		if(node.getChildren().isEmpty())
			return true;
		
		for (IuctNode child : node.getChildren()){
			boolean identical = true;
			if (child.getMove().size() != move.size()){
				identical = false;
			} else {
				for(int i = 0; i < move.size(); i++){
					if (!child.getMove().get(i).equals(move.get(i))){
						identical = false;
						break;
					}
				}
			}
			if (identical){
				return false;
			}
		}
		
		return true;
		
	}

	private NodeType getChildNodeType(IuctNode parent, GameState prevState) {
		
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

	private void updateState(IuctNode node, GameState state, boolean leaf) {
		
		if (node.getType() != NodeType.FIRST || leaf){
			
			if (node.getType() == NodeType.SECOND){
				
				try {
					List<UnitState> move = node.getParent().getMove();
					List<UnitAction> actions = statesToActions(move, state.clone());
					state.makeMoves(actions);
				} catch (Exception e) {e.printStackTrace();}
			}
			
			try {
				List<UnitState> move = node.getMove();
				List<UnitAction> actions = statesToActions(move, state.clone());
				state.makeMoves(actions);
			} catch (Exception e) {e.printStackTrace();}
			
			state.finishedMoving();
			
		}
		
	}

	private List<UnitAction> statesToActions(List<UnitState> move, GameState state) {
		
		int player = 0;
		
		if (move == null || move.isEmpty() || move.get(0) == null)
			return new ArrayList<UnitAction>();
		else
			player = move.get(0).player;
		
		Player attack = new Player_NoOverKillAttackValue(player);
		Player kite = new Player_Kite(player);
		
		HashMap<Integer, List<UnitAction>> map = new HashMap<Integer, List<UnitAction>>();
		
		try {
			state.generateMoves(map, player);
		} catch (Exception e) {e.printStackTrace();}
		
		List<Integer> attackingUnits = new ArrayList<Integer>();
		List<Integer> kitingUnits = new ArrayList<Integer>();
		
		// Divide units into two groups
		for(UnitState unitState : move){
			
			if (unitState.type == UnitStateTypes.ATTACK)
				attackingUnits.add(unitState.unit);
			else if (unitState.type == UnitStateTypes.KITE)
				kitingUnits.add(unitState.unit);
			
		}
		
		List<UnitAction> allActions = new ArrayList<UnitAction>();
		HashMap<Integer, List<UnitAction>> attackingMap = new HashMap<Integer, List<UnitAction>>();
		HashMap<Integer, List<UnitAction>> kitingMap = new HashMap<Integer, List<UnitAction>>();
		
		for(Integer i : attackingUnits)
			if (map.get(i) != null)
				attackingMap.put(i, map.get(i));
			
		
		for(Integer i : kitingUnits)
			if (map.get(i) != null)
				kitingMap.put(i, map.get(i));
		
		// Add attack actions
		List<UnitAction> attackActions = new ArrayList<UnitAction>();
		attack.getMoves(state, attackingMap, attackActions);
		allActions.addAll(attackActions);
		
		// Add kite actions
		List<UnitAction> kiteActions = new ArrayList<UnitAction>();
		kite.getMoves(state, kitingMap, kiteActions);
		allActions.addAll(kiteActions);
		
		return allActions;
	}

	private IuctNode selectNode(IuctNode parent) {
		
		float bestScore = Float.MAX_VALUE;
		//.getChildren().get(0)
		boolean maxPlayer = parent.getMovingPlayerIndex() == maxPlayerIndex;
		if (maxPlayer)
			bestScore = -Float.MAX_VALUE;
			
		IuctNode bestNode = null;
		for(IuctNode child : parent.getChildren()){
			
			if (child.getVisits() > 0){
				
	            float score = child.getTotalScore() / child.getVisits();
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
	
	private IuctNode mostWinningChildOf(IuctNode parent) {
		int bestScore = Integer.MIN_VALUE;
		IuctNode best = null;
		for(IuctNode node : parent.getChildren()){
			if (node.getTotalScore()/node.getVisits() > bestScore){
				best = node;
				bestScore = (int) (node.getTotalScore()/node.getVisits());
			}
		}
		return best;
	}


	private IuctNode mostVisitedChildOf(IuctNode parent) {
		int mostVisits = -1;
		IuctNode best = null;
		for(IuctNode node : parent.getChildren()){
			if (node.getVisits()>mostVisits){
				best = node;
				mostVisits = node.getVisits();
			}
		}
		return best;
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