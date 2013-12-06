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

import org.omg.CORBA._IDLTypeStub;

import bwmcts.clustering.UPGMA;
import bwmcts.mcts.UnitState;
import bwmcts.mcts.UnitStateTypes;
import bwmcts.sparcraft.EvaluationMethods;
import bwmcts.sparcraft.Game;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Players;
import bwmcts.sparcraft.StateEvalScore;
import bwmcts.sparcraft.Unit;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.players.Player;
import bwmcts.sparcraft.players.Player_AttackClosest;
import bwmcts.sparcraft.players.Player_Defense;
import bwmcts.sparcraft.players.Player_Kite;
import bwmcts.sparcraft.players.Player_KiteDPS;
import bwmcts.sparcraft.players.Player_NoOverKillAttackValue;

public class GUCTCD {

	private double K = 1.6f;
	private int maxChildren = 20;
	private int maxPlayerIndex = 0;
	private int minPlayerIndex = 1;
	private boolean debug;
	private int simulationSteps;
	private Player baseScript;
	private HashMap<Integer, List<Unit>> clustersA;
	private HashMap<Integer, List<Unit>> clustersB;
	private double hpMulitplier;
	private int clusters;

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

	public List<UnitAction> search(GameState state, UPGMA upgmaPlayerA, UPGMA upgmaPlayerB, long timeBudget){
		
		//System.out.println("Search called");
		
		if (maxPlayerIndex == 0 && state.whoCanMove() == Players.Player_Two){
			return new ArrayList<UnitAction>(); 
		} else if (maxPlayerIndex == 1 && state.whoCanMove() == Players.Player_One){
			return new ArrayList<UnitAction>(); 
		}
		
		Date start = new Date();
		
		// Get clusters
		clustersA = upgmaPlayerA.getClusters(6);
		clustersB = upgmaPlayerB.getClusters(6);
		
		GuctNode root = new GuctNode(null, NodeType.ROOT, new ArrayList<UnitState>(), maxPlayerIndex);
		
		int t = 0;
		while(new Date().getTime() <= start.getTime() + timeBudget){
			
			traverse(root, state.clone());
			t++;
			
			//String out = root.print(0);
			//writeToFile(out, "tree.xml");
			
		}
		
		//GuctNode best = mostVisitedChildOf(root);
		GuctNode best = mostWinningChildOf(root);
			
		if (debug){
			//System.out.println("Traversals " + (t++));
			//String out = root.print(0);
			//writeToFile(out, "tree.xml");
		}
		
		if (best == null){
			return new ArrayList<UnitAction>();
		}
		
		System.out.print("guctcd:");
		for(UnitState s : best.getMove())
			System.out.print(s.type);
		System.out.print("\n");
		
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
		
		// Which cluster?
		HashMap<Integer, List<Unit>> clusters = clustersB;
		if (playerToMove == maxPlayerIndex)
			clusters = clustersA;
		
		if (node.getChildren().isEmpty())
			move.addAll(getAllMove(UnitStateTypes.KITE, clusters));
		else if (node.getChildren().size() == 1)
			move.addAll(getAllMove(UnitStateTypes.ATTACK, clusters));
		else 
			move = getRandomMove(playerToMove, clusters); // Possible moves?
			
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

	private List<UnitState> getAllMove(UnitStateTypes type, HashMap<Integer, List<Unit>> clusters) {

		List<UnitState> states = new ArrayList<UnitState>();
		
		for(Integer c : clusters.keySet()){
			
			UnitState state = new UnitState(type, c, clusters.get(c).get(0).player());
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

	private List<UnitState> getRandomMove(int playerToMove, HashMap<Integer, List<Unit>> clusters) {
		
		List<UnitState> states = new ArrayList<UnitState>();
		
		for(Integer c : clusters.keySet()){
			
			// Random state
			UnitStateTypes type = UnitStateTypes.ATTACK;
			if (Math.random() >= 0.5f)
				type = UnitStateTypes.KITE;
			
			UnitState state = new UnitState(type, c, clusters.get(c).get(0).player());
			states.add(state);
			
		}
		
		return states;
		
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
		
		if (move == null || move.isEmpty() || move.get(0) == null)
			return new ArrayList<UnitAction>();
		
		int player = move.get(0).player;
		
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
			
			// Which cluster?
			HashMap<Integer, List<Unit>> clusters = clustersB;
			if (player == maxPlayerIndex)
				clusters = clustersA;
			
			// Add units in cluster
			for(Unit u : clusters.get(unitState.unit)){
				
				if (u.isAlive() && (u.canAttackNow() || u.canMoveNow())){
				
					if (unitState.type == UnitStateTypes.ATTACK && u.isAlive())
						attackingUnits.add(u.getId());
					else if (unitState.type == UnitStateTypes.KITE && u.isAlive())
						kitingUnits.add(u.getId());
					
				}
				
			}
			
		}
		
		List<UnitAction> allActions = new ArrayList<UnitAction>();
		HashMap<Integer, List<UnitAction>> attackingMap = new HashMap<Integer, List<UnitAction>>();
		HashMap<Integer, List<UnitAction>> kitingMap = new HashMap<Integer, List<UnitAction>>();
		
		// Loop through the map
		for(Integer i : map.keySet()){
			int u = map.get(i).get(0)._unit;
			int unitId = state.getUnit(player, u).getId();
			if (attackingUnits.contains(unitId))
				attackingMap.put(i, map.get(i)); 
			if (kitingUnits.contains(unitId))
				kitingMap.put(i, map.get(i));
		}
		
		// Add attack actions
		List<UnitAction> attackActions = new ArrayList<UnitAction>();
		attack.getMoves(state, attackingMap, attackActions);
		allActions.addAll(attackActions);
		
		// Add defend actions
		List<UnitAction> defendActions = new ArrayList<UnitAction>();
		kite.getMoves(state, kitingMap, defendActions);
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

	public HashMap<Integer, List<Unit>> getClustersA() {
		return clustersA;
	}

	public void setClustersA(HashMap<Integer, List<Unit>> clustersA) {
		this.clustersA = clustersA;
	}

	public HashMap<Integer, List<Unit>> getClustersB() {
		return clustersB;
	}

	public void setClustersB(HashMap<Integer, List<Unit>> clustersB) {
		this.clustersB = clustersB;
	}

	public double getHpMulitplier() {
		return hpMulitplier;
	}

	public void setHpMulitplier(double hpMulitplier) {
		this.hpMulitplier = hpMulitplier;
	}

	public int getClusters() {
		return clusters;
	}

	public void setClusters(int clusters) {
		this.clusters = clusters;
	}
	
	
	
}