package bwmcts.sparcraft.players;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import bwmcts.sparcraft.Constants;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Position;
import bwmcts.sparcraft.Unit;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.UnitActionTypes;

public class Player_Random extends Player {
	
	private int _id=0;
	private Random r = new Random();
	
	public Player_Random(int playerID) {
		_id=playerID;
		setID(playerID);
	}

	public void getMoves(GameState  state, HashMap<Integer,List<UnitAction>> moves, List<UnitAction>  moveVec)
	{
		
	    moveVec.clear();
		for (int u=0; u< moves.size(); u++)
		{
			
			int a = (int) Math.floor((r.nextDouble() * (double)moves.get(u).size()));
			//int a = 0;
			moveVec.add(moves.get(u).get(a));
			
		}
	}
}
