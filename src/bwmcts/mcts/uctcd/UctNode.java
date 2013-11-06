package bwmcts.mcts.uctcd;

import java.util.ArrayList;
import java.util.List;

import bwmcts.sparcraft.UnitAction;

public class UctNode {

	int visits;
	float totalScore;
	float uctValue;
	
	List<UnitAction> move;
	int movingPlayerIndex;
	NodeType type;
	
	List<UctNode> children;
	UctNode parent;
	
	public UctNode(UctNode parent, NodeType type, List<UnitAction> move, int movingPlayerIndex) {
		
		this.visits = 0;
		this.totalScore = 0;
		this.uctValue = 0;
		this.movingPlayerIndex = movingPlayerIndex;
		this.parent = parent;
		this.children = new ArrayList<UctNode>();
		this.type = type;
		this.move = move;
		
	}
	
	public UctNode mostVisitedChild(){
		UctNode mostVisited = null;
		for(UctNode child : children){
			if (mostVisited == null || child.getVisits() > mostVisited.getVisits())
				mostVisited = child;
		}
		return mostVisited;
	}
	
	public List<UnitAction> getMove() {
		return move;
	}

	public void setMove(List<UnitAction> move) {
		this.move = move;
	}

	public List<UctNode> getChildren() {
		return children;
	}

	public void setChildren(List<UctNode> children) {
		this.children = children;
	}

	public UctNode getParent() {
		return parent;
	}

	public void setParent(UctNode parent) {
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
		for(UnitAction a : move){
			moves += a.moveString() + "(" + a.pos().getX() + "," + a.pos().getY() + ");";
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
		for(UctNode child : children){
			
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
}
