/**
* This file is based on and translated from the open source project: Sparcraft
* https://code.google.com/p/sparcraft/
* author of the source: David Churchill
**/
package bwmcts.uct.iuct;

import java.util.List;

import bwmcts.uct.NodeType;
import bwmcts.uct.UnitState;
import bwmcts.uct.UctNode;

public class IuctNode extends UctNode {

	private List<UnitState> abstractMove;
	
	public IuctNode(IuctNode parent, NodeType type, List<UnitState> abstractMove, int movingPlayerIndex, String label) {
		super(parent, type, null, movingPlayerIndex, label);
		this.abstractMove = abstractMove;
	}

	@Override
	public String moveString(){
		String moves = "";
		for(UnitState a : abstractMove){
			moves += a.type + ";";
		}
		return moves;
	}
	
	public List<UnitState> getAbstractMove() {
		return abstractMove;
	}

	public void setAbstractMove(List<UnitState> abstractMove) {
		this.abstractMove = abstractMove;
	}

}
