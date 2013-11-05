package bwmcts.mcts.uctcd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import bwmcts.sparcraft.Game;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Players;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.players.Player;
import bwmcts.sparcraft.players.Player_AttackClosest;

public class UCTCD {

	private double K = 1.6f;
	private int maxChildren = 20;
	private int maxPlayerIndex = 0;
	private int minPlayerIndex = 1;
	
	public static void main(String[] args) {
		
		UCTCD uctcd = new UCTCD();
		GameState state = new GameState();
		uctcd.search(state, 40L);
		
	}
	
	public UCTCD() {
		
	}

	public UCTCD(double k, int maxChildren, int minPlayerIndex,
			int maxPlayerIndex) {
		super();
		K = k;
		this.maxChildren = maxChildren;
		this.minPlayerIndex = minPlayerIndex;
		this.maxPlayerIndex = maxPlayerIndex;
	}

	public List<UnitAction> search(GameState state, long timeBudget){
		
		Date start = new Date();
		
		UctNode root = new UctNode(null, NodeType.ROOT, new ArrayList<UnitAction>(), maxPlayerIndex);
		root.setVisits(1);
		
		int t = 0;
		while(new Date().getTime() <= start.getTime() + timeBudget){
			
			traverse(root, state.clone());
			System.out.println("Traversal " + (t++));
			
		}
		
		UctNode best = mostVisitedChildOf(root);
		
		String out = root.print(0);

		writeToFile(out, "tree.xml");
		
		return best.getMove();
		
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

	private float traverse(UctNode node, GameState state) {
		
		float score = 0;
		if (node.getVisits() == 0){
			updateState(node, state, true);
			score = evaluate(state);
		} else {
			updateState(node, state, true);
			if (state.isTerminal()){
				score = evaluate(state);
			} else {
				if (node.getChildren().isEmpty())
					generateChildren(node, state);
				score = traverse(selectNode(node), state);
			}
		}
		node.setVisits(node.getVisits() + 1);
		node.setTotalScore(node.getTotalScore() + score);
		return score;
	}

	private float evaluate(GameState state) {
		
		// get the players
	    Player p1 = new Player_AttackClosest(Players.Player_One.ordinal());
	    Player p2 = new Player_AttackClosest(Players.Player_Two.ordinal());

	    // enter a maximum move limit for the game to go on for
	    int moveLimit = 1000;

	    // contruct the game
	    Game g=new Game(state, p1, p2, moveLimit,false);

	    // play the game
	    g.play();

	    // you can access the resulting game state after g has been played via getState
	    GameState finalState = g.getState();
	    System.out.println(finalState.playerDead(0) + "  "+finalState.playerDead(1));
	    // you can now evaluate the state however you wish. let's use an LTD2 evaluation from the point of view of player one
	    int score = finalState.eval(Players.Player_One.ordinal(), 0,0,0);
		
		return score;
	}

	private void generateChildren(UctNode node, GameState state) {
		
		// Figure out who is next to move
		int playerToMove = getPlayerToMove(node, state);
		
		HashMap<Integer, List<UnitAction>> map = new HashMap<Integer, List<UnitAction>>();
		
		// Generate possible moves?
		try {
			state.generateMoves(map, node.movingPlayerIndex);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int children = 0;
		while(children < maxChildren ){
			
			List<UnitAction> move = getNextMove(playerToMove, state, map); // Possible moves?
			if (move == null)
				break;
			
			UctNode child = new UctNode(node, getChildNodeType(node, state), move, enemy(playerToMove));
			node.children.add(child);
			children++;
		}
		
	}

	private int getPlayerToMove(UctNode node, GameState state) {
		
		if (state.bothCanMove()){
		
			if (node.getType() == NodeType.ROOT)
				
				return maxPlayerIndex;
			
			else if (node.getType() == NodeType.FIRST)
				
				return minPlayerIndex;
			
			return maxPlayerIndex;
			
		}
		
		if (state.whoCanMove() == Players.Player_One)
			return 0;
		else if (state.whoCanMove() == Players.Player_Two)
			return 1;
		
		return -1;
		
	}

	private int enemy(int player) {
		
		if (player == minPlayerIndex)
			return maxPlayerIndex;
		else if (player == maxPlayerIndex)
			return minPlayerIndex;
		
		return -1;
	}

	private NodeType getChildNodeType(UctNode parent, GameState prevState) {
		
		if(!prevState.bothCanMove())
			return NodeType.SOLO;
		
		if (parent.getType() == NodeType.ROOT)
			return NodeType.FIRST;
		
		if (parent.getType() == NodeType.SOLO)
			return NodeType.FIRST;
		
		if (parent.getType() == NodeType.SECOND)
			return NodeType.FIRST;
		
		if (parent.getType() == NodeType.FIRST)
			return NodeType.SECOND;
		
		return null;
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
				
				if (node.getParent() != null){
					try {
						state.makeMoves(node.getParent().getMove());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			try {
				state.makeMoves(node.getMove());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			state.finishedMoving();
			
		}
		
	}

	private UctNode selectNode(UctNode parent) {
		
		float bestScore = Float.MAX_VALUE;
		boolean maxPlayer = parent.getChildren().get(0).getMovingPlayerIndex() == maxPlayerIndex;
		if (maxPlayer)
			bestScore = Float.MIN_VALUE;
			
		UctNode bestNode = null;
		for(UctNode child : parent.getChildren()){
			
			if (child.getVisits() > 0){
				
	            float winRate = child.getTotalScore() / child.getVisits();
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

	private UctNode mostVisitedChildOf(UctNode parent) {
		int mostVisits = -1;
		UctNode best = null;
		for(UctNode node : parent.getChildren()){
			if (node.getVisits()>mostVisits)
				best = node;
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
	
	/*
	private float evaluate(GameState state) {
		float value = 0;
		for(Unit unit : state.getUnits(maxPlayerIndex)){
			UnitType type = unit.getType();
			int hp = unit.getHitPoints();
			int damage = maxDamageRate(type);
			value += Math.sqrt(hp) * damage;
		}
		for(Unit unit : state.getUnits(minPlayerIndex)){
			UnitType type = unit.getType();
			int hp = unit.getHitPoints();
			int damage = maxDamageRate(type);
			value -= Math.sqrt(hp) * damage;
		}
		return value;
	}

	private int maxDamageRate(UnitType type) {
		int airDamage = 0;
		int groundDamage = 0;
		if (type.isCanAttackAir()){
			int weapon = type.getAirWeaponID();
			int damage = 
		}
		return type.get
	}
	*/
	
	
}
