package bwmcts.mcts.uctcd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import bwmcts.simulator.GameState;
import bwmcts.simulator.Move;

public class UCTCD {

	private double K = 1.6f;
	private int maxChildren = 20;
	private int maxPlayerIndex = 0;
	private int minPlayerIndex = 1;
	
	public static void main(String[] args) {
		
		UCTCD uctcd = new UCTCD();
		GameState state = new GameState(0);
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

	public Move search(GameState state, long timeBudget){
		
		Date start = new Date();
		
		UctNode root = new UctNode(null, NodeType.ROOT, new Move(), maxPlayerIndex);
		
		while(new Date().getTime() <= start.getTime() + timeBudget){
			
			traverse(root, state.clone());
			
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
			score = state.evaluate();
		} else {
			updateState(node, state, true);
			if (state.isTerminal()){
				score = state.evaluate();
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

	private void generateChildren(UctNode node, GameState state) {
		
		// Figure out who is next to move
		int playerToMove = getPlayerToMove(node, state);
		
		// Generate possible moves?
		// state.getPossibleMoves();...
		
		int children = 0;
		while(children < maxChildren ){
			
			Move move = getNextMove(playerToMove, state); // Possible moves?
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
		
		return state.whoCanMove();
		
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

	private Move getNextMove(int playerToMove, GameState state) {
		
		return new Move();
		
	}

	private void updateState(UctNode node, GameState state, boolean leaf) {
		
		if (node.getType() != NodeType.FIRST || leaf){
			
			if (node.getType() == NodeType.SECOND){
				
				if (node.getParent() != null)
					state.applyMoves(node.getParent().getMove());
								
			}
			
			state.applyMoves(node.getMove());
			state.makeMove();
			
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
