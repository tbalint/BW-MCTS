package bwmcts.sparcraft.players;

import java.util.HashMap;
import java.util.List;

import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.MoveArray;
import bwmcts.sparcraft.UnitAction;

public abstract class Player {
 //Sparcraft players
	
	public int _id=0;
	
	public Player(){
		
	}
	
	public Player(int id){
		_id=id;
	}
	public void getMoves(GameState state, HashMap<Integer,List<UnitAction>> moves, List<UnitAction> moveVec){
		
	}
	public int ID(){return _id;}
	public void setID(int id){_id=id;}
}
