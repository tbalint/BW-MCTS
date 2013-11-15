package bwmcts.mcts.guct;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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
import bwmcts.sparcraft.players.Player_NoOverKillAttackValue;

public class GUCTCD {

	private double K = 1.6f;
	private int maxChildren = 20;
	private int maxPlayerIndex = 0;
	private int minPlayerIndex = 1;
	private boolean debug;
	private int simulationSteps;
	private Player baseScript;
	
	
	public GUCTCD() {
		
	}

	public GUCTCD(double k, int maxChildren, int minPlayerIndex,
			int maxPlayerIndex, int simulationSteps, boolean debug) {
		super();
		this.K = k;
		this.maxChildren = maxChildren;
		this.minPlayerIndex = minPlayerIndex;
		this.maxPlayerIndex = maxPlayerIndex;
		this.debug = debug;
		this.simulationSteps = simulationSteps;
		this.baseScript = new Player_NoOverKillAttackValue(maxPlayerIndex);
	}

	public List<UnitAction> search(GameState state, long timeBudget){
		
		if (maxPlayerIndex == 0 && state.whoCanMove() == Players.Player_Two){
			return new ArrayList<UnitAction>(); 
		} else if (maxPlayerIndex == 1 && state.whoCanMove() == Players.Player_One){
			return new ArrayList<UnitAction>(); 
		}
		
		Date start = new Date();
		
		GuctNode root = new GuctNode(null, NodeType.ROOT, new ArrayList<UnitState>(), maxPlayerIndex);
		//root.setVisits(1);
		
		int t = 0;
		while(new Date().getTime() <= start.getTime() + timeBudget){
			
			traverse(root, state.clone());
			t++;
			/*
			String out = root.print(0);
			writeToFile(out, "tree.xml");
			*/
		}
		
		//GuctNode best = mostVisitedChildOf(root);
		GuctNode best = mostWinningChildOf(root);
		
		if (debug){
			System.out.println("Traversals " + (t++));
			/*
			String out = root.print(0);
			writeToFile(out, "tree.xml");
			*/
		}
		
		if (best == null){
			return new ArrayList<UnitAction>();
		}
		
		List<UnitAction> actions = statesToActions(best.getMove(), state.clone());
		
		return actions;
		
	}

	private float traverse(GuctNode node, GameState state) {
		
		float score = 0;
		if (node.getVisits() == 0){
			updateState(node, state, true);
			score = evaluate(state.clone());
		} else {
			updateState(node, state, false);
			if (state.isTerminal()){
				score = evaluate(state.clone());
			} else {
				if (!node.isFullyExpanded())
					generateChildren(node, state);
				score = traverse(selectNode(node), state);
			}
		}
		node.setVisits(node.getVisits() + 1);
		node.setTotalScore(node.getTotalScore() + score);
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

	private void generateChildren(GuctNode node, GameState state) {
		
		// Figure out who is next to move
		int playerToMove = getPlayerToMove(node, state);
		
		List<UnitState> move = new ArrayList<UnitState>();
		
		HashMap<Integer, List<UnitAction>> map;
		if (node.getPossibleMoves() == null){

			map = new HashMap<Integer, List<UnitAction>>();
			try {
				state.generateMoves(map, playerToMove);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			node.setPossibleMoves(map);
			
		}
		
			
		if (node.getChildren().isEmpty())
			move.addAll(getAllMove(UnitStateTypes.ATTACK, node.getPossibleMoves()));
		else if (node.getChildren().size() == 0)
			move.addAll(getAllMove(UnitStateTypes.FLEE, node.getPossibleMoves()));
		else 
			move = getRandomMove(playerToMove, node.getPossibleMoves()); // Possible moves?
			
		if (move == null)
			return;
	
		if (uniqueMove(move, node)){
			GuctNode child = new GuctNode(node, getChildNodeType(node, state), move, playerToMove);
			node.getChildren().add(child);
		}
		
		node.setExpansions(node.getExpansions() + 1);

		if (node.getExpansions() >= maxChildren)
			node.setFullyExpanded(true);
		
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

	private int getPlayerToMove(GuctNode node, GameState state) {
		
		if (state.whoCanMove() == Players.Player_Both){
		
			if (node.getType() == NodeType.ROOT)
				
				return maxPlayerIndex;
			
			if (node.getType() == NodeType.FIRST)
				
				return state.getEnemy(node.getMovingPlayerIndex());
			
			return node.getMovingPlayerIndex();
			
		}
		
		if (state.whoCanMove() == Players.Player_One)
			return 0;
		else if (state.whoCanMove() == Players.Player_Two)
			return 1;
		
		return -1;
		
	}
	
	private boolean uniqueMove(List<UnitState> move, GuctNode node) {

		if(node.getChildren().isEmpty())
			return true;
		
		for (GuctNode child : node.getChildren()){
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

	private NodeType getChildNodeType(GuctNode parent, GameState prevState) {
		
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

	private List<UnitState> getRandomMove(int playerToMove, HashMap<Integer, List<UnitAction>> map) {
		
		ArrayList<UnitState> move = new ArrayList<UnitState>();
		
		for(Integer i : map.keySet()){
			
			// Random state
			UnitStateTypes type = UnitStateTypes.ATTACK;
			if (Math.random() >= 0.5f)
				type = UnitStateTypes.FLEE;
			
			UnitState unitState = new UnitState(type, i, playerToMove);
			
			// Add random possible action
			move.add(unitState);
			
		}
		
		return move;
		
	}

	private void updateState(GuctNode node, GameState state, boolean leaf) {
		
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
		Player flee = new Player_Kite(player);
		
		HashMap<Integer, List<UnitAction>> map = new HashMap<Integer, List<UnitAction>>();
		
		try {
			state.generateMoves(map, player);
		} catch (Exception e) {e.printStackTrace();}
		
		List<Integer> attackingUnits = new ArrayList<Integer>();
		List<Integer> fleeingUnits = new ArrayList<Integer>();
		
		// Divide units into two groups
		for(UnitState unitState : move){
			
			if (unitState.type == UnitStateTypes.ATTACK)
				attackingUnits.add(unitState.unit);
			else if (unitState.type == UnitStateTypes.FLEE)
				fleeingUnits.add(unitState.unit);
			
		}
		
		List<UnitAction> allActions = new ArrayList<UnitAction>();
		HashMap<Integer, List<UnitAction>> attackingMap = new HashMap<Integer, List<UnitAction>>();
		HashMap<Integer, List<UnitAction>> defendingMap = new HashMap<Integer, List<UnitAction>>();
		
		for(Integer i : attackingUnits)
			attackingMap.put(i, map.get(i));
		
		for(Integer i : fleeingUnits)
			defendingMap.put(i, map.get(i));
		
		// Add attack actions
		List<UnitAction> attackActions = new ArrayList<UnitAction>();
		attack.getMoves(state, attackingMap, attackActions);
		allActions.addAll(attackActions);
		
		// Add defend actions
		List<UnitAction> defendActions = new ArrayList<UnitAction>();
		flee.getMoves(state, defendingMap, defendActions);
		allActions.addAll(defendActions);
		
		return allActions;
	}

	private GuctNode selectNode(GuctNode parent) {
		
		float bestScore = Float.MAX_VALUE;
		//.getChildren().get(0)
		boolean maxPlayer = parent.getMovingPlayerIndex() == maxPlayerIndex;
		if (maxPlayer)
			bestScore = -Float.MAX_VALUE;
			
		GuctNode bestNode = null;
		for(GuctNode child : parent.getChildren()){
			
			if (child.getVisits() > 0){
				
	            float winRate = child.getTotalScore() / child.getVisits();
	            winRate = winRate / 500;
	            float uctVal = (float) (K * Math.sqrt(Math.log(parent.getVisits()) / child.getVisits()));
	            float currentVal = maxPlayer ? (winRate + uctVal) : (winRate - uctVal);
	            
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
	
	private GuctNode mostWinningChildOf(GuctNode parent) {
		int bestScore = Integer.MIN_VALUE;
		GuctNode best = null;
		for(GuctNode node : parent.getChildren()){
			if (node.getTotalScore()/node.getVisits() > bestScore){
				best = node;
				bestScore = (int) (node.getTotalScore()/node.getVisits());
			}
		}
		return best;
	}


	private GuctNode mostVisitedChildOf(GuctNode parent) {
		int mostVisits = -1;
		GuctNode best = null;
		for(GuctNode node : parent.getChildren()){
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