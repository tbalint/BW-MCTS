package bwmcts.sparcraft.players;

import java.util.HashMap;
import java.util.List;

import javabot.JNIBWAPI;

import bwmcts.sparcraft.Constants;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.PlayerProperties;
import bwmcts.sparcraft.Position;
import bwmcts.sparcraft.Unit;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.UnitActionTypes;
import bwmcts.sparcraft.WeaponProperties;

public class Player_Defense extends Player {
	
	private int _id=0;
	
	public Player_Defense(int playerID) {
		_id=playerID;
		setID(playerID);
	}

	public void getMoves(GameState  state, HashMap<Integer,List<UnitAction>> moves, List<UnitAction>  moveVec)
	{
		moveVec.clear();
		for (Integer u : moves.keySet())
		{
			int furthestMoveIndex					= 0;
			int furthestMoveDist					= 0;
			Unit ourUnit							= (state.getUnit(_id, u));
			Unit closestUnit						= (ourUnit.canHeal() ? state.getClosestOurUnit(_id, u) : state.getClosestEnemyUnit(_id, u));

			for (int m = 0; m < moves.get(u).size(); ++m)
			{
				UnitAction move						= moves.get(u).get(m);
					
				if (move.type() == UnitActionTypes.MOVE)
				{
					Position ourDest = new Position(ourUnit.pos().getX() + Constants.Move_Dir[move._moveIndex][0], 
													 ourUnit.pos().getY() + Constants.Move_Dir[move._moveIndex][1]);
					int dist = (closestUnit.getDistanceSqToPosition(ourDest, state.getTime()));

					if (dist > furthestMoveDist)
					{
						furthestMoveDist = dist;
						furthestMoveIndex = m;
					}
				}
			}

			moveVec.add(moves.get(u).get(furthestMoveIndex));
		}
	}
}
