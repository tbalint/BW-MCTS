/**
* This file is an extension to code based on and translated from the open source project: Sparcraft
* https://code.google.com/p/sparcraft/
* author of the source: David Churchill
**/
package bwmcts.mcts.guct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bwmcts.mcts.UnitState;
import bwmcts.sparcraft.UnitAction;

public class GuctNode {

	private int visits;
	private float totalScore;
	private float uctValue;
	
	private List<UnitState> move;
	private int movingPlayerIndex;
	private NodeType type;
	private boolean fullyExpanded;
	private int expansions;
	
	private List<GuctNode> children;
	private GuctNode parent;
	private HashMap<Integer, List<UnitAction>> possibleMoves;
	
	public GuctNode(GuctNode parent, NodeType type, List<UnitState> move, int movingPlayerIndex) {
		
		this.visits = 0;
		this.totalScore = 0;
		this.uctValue = 0;
		this.movingPlayerIndex = movingPlayerIndex;
		this.parent = parent;
		this.children = new ArrayList<GuctNode>();
		this.type = type;
		this.move = move;
		this.fullyExpanded = false;
		this.expansions = 0;
		
	}
	
	public GuctNode mostVisitedChild(){
		GuctNode mostVisited = null;
		for(GuctNode child : children){
			if (mostVisited == null || child.getVisits() > mostVisited.getVisits())
				mostVisited = child;
		}
		return mostVisited;
	}
	
	public List<UnitState> getMove() {
		return move;
	}

	public void setMove(List<UnitState> move) {
		this.move = move;
	}

	public List<GuctNode> getChildren() {
		return children;
	}

	public void setChildren(List<GuctNode> children) {
		this.children = children;
	}

	public GuctNode getParent() {
		return parent;
	}

	public void setParent(GuctNode parent) {
		this.parent = parent;
	}

	public int getVisits() {
		return visits;
	}

	public void setVisits(int visits) {
		this.visits = visits;
	}

	public float getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(float totalScore) {
		this.totalScore = totalScore;
	}

	public NodeType getType() {
		return type;
	}

	public void setType(NodeType type) {
		this.type = type;
	}

	public float getUctValue() {
		return uctValue;
	}

	public void setUctValue(float uctValue) {
		this.uctValue = uctValue;
	}

	public int getMovingPlayerIndex() {
		return movingPlayerIndex;
	}

	public void setMovingPlayerIndex(int movingPlayerIndex) {
		this.movingPlayerIndex = movingPlayerIndex;
	}

	public String print(int level) {
		
		String moves = "";
		for(UnitState a : move){
			moves += a.type;
		}
		
		String out = "";
		for(int n = 0; n < level; n++){
			out += "\t";
		}
		out += "<node type="+type+ " playerToMove=" + movingPlayerIndex + " moves=" + moves + " totalScore=" + totalScore + " visited=" + visits;
		
		if (children.isEmpty()){
			out += "/>\n";
		} else {
			out += ">\n";
		}
		
		int next = level+1;
		for(GuctNode child : children){
			
			out += child.print(next);
			
		}
		
		if (!children.isEmpty()){
			for(int n = 0; n < level; n++){
				out += "\t";
			}
			out += "</node>\n";
		}
		
		return out;
		
	}

	public HashMap<Integer, List<UnitAction>> getPossibleMoves() {
		return possibleMoves;
	}

	public void setPossibleMoves(HashMap<Integer, List<UnitAction>> possibleMoves) {
		this.possibleMoves = possibleMoves;
	}

	public boolean isFullyExpanded() {
		return fullyExpanded;
	}

	public void setFullyExpanded(boolean expanded) {
		this.fullyExpanded = expanded;
	}

	public int getExpansions() {
		return expansions;
	}

	public void setExpansions(int expansions) {
		this.expansions = expansions;
	}
	
	
	
}