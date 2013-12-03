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

public class Player_NoOverKillAttackValue extends Player {
	
	private int _id=0;
	private int enemy;
	private int[] hpRemaining = new int[Constants.Max_Units];
	
	public Player_NoOverKillAttackValue(int playerID) {
		_id=playerID;
		setID(playerID);
		enemy=GameState.getEnemy(_id);
	}

	public long timeOnHpCopying=0;
	public void getMoves(GameState  state, HashMap<Integer,List<UnitAction>> moves, List<UnitAction>  moveVec)
	{
		
		moveVec.clear();

		
		
		for(int u = 0; u < state.numUnits(enemy); u++){
			
			hpRemaining[u] = state.getUnit(enemy,u).currentHP();
			
		}
		
		
		//for (int u = 0; u<moves.size(); ++u){
		boolean foundUnitAction = false;
		int actionMoveIndex	= 0;
		double actionHighestDPS	= 0;
		int closestMoveIndex = 0;
		int closestMoveDist	= Integer.MAX_VALUE;
		
		for (Integer u : moves.keySet()){
			
			foundUnitAction=false;
			actionMoveIndex=0;
			actionHighestDPS=0;
			closestMoveIndex =0;
			closestMoveDist=Integer.MAX_VALUE;
			Unit ourUnit = state.getUnit(_id, u);
			//Unit ourUnit = state.getUnitByID(u);
			
			if (ourUnit == null || moves.get(u) == null){
				state.print();
				System.out.println(ourUnit + " " + _id + " " + u);
			}
			long g=System.nanoTime();
			Unit closestUnit			= ourUnit.canHeal() ? state.getClosestOurUnit(_id, u) : state.getClosestEnemyUnit(_id,u);
			timeOnHpCopying+=System.nanoTime()-g;
			for (int m=0; m<moves.get(u).size(); m++)
			{
				UnitAction move	= moves.get(u).get(m);
					
				if ((move.type() == UnitActionTypes.ATTACK) && (hpRemaining[move.index()] > 0))
				{
					
					double dpsHPValue 	= (state.getUnit(enemy, move.index()).dpf() / hpRemaining[move.index()]);

					if (dpsHPValue > actionHighestDPS)
					{
						actionHighestDPS = dpsHPValue;
						actionMoveIndex = m;
						foundUnitAction = true;
					}

	                /*if (move.index() >= state.numUnits(enemy))
	                {
	                    int e = enemy;
	                    int pl = _id;
	                    System.out.println("wtf");
	                }*/
				}
				else if (move.type() == UnitActionTypes.HEAL)
				{
					
					double dpsHPValue =	(state.getUnit(move.player(), move.index()).dpf() / hpRemaining[move.index()]);

					if (dpsHPValue > actionHighestDPS)
					{
						actionHighestDPS = dpsHPValue;
						actionMoveIndex = m;
						foundUnitAction = true;
					}
				}
				else if (move.type() == UnitActionTypes.RELOAD)
				{
					if (ourUnit.canAttackTarget(closestUnit, state.getTime()))
					{
						closestMoveIndex = m;
						break;
					}
				}
				else if (move.type() == UnitActionTypes.MOVE)
				{
					
					int dist = closestUnit.getDistanceSqToPosition(new Position(ourUnit.pos().getX() + Constants.Move_Dir[move.index()][0], 
										ourUnit.pos().getY() + Constants.Move_Dir[move.index()][1]), state.getTime());

					if (dist < closestMoveDist)
					{
						closestMoveDist = dist;
						closestMoveIndex = m;
					}
				}
			}

			
			if (foundUnitAction){
				UnitAction theMove = moves.get(u).get(actionMoveIndex);
				if (theMove.type() == UnitActionTypes.ATTACK)
				{
					hpRemaining[theMove.index()] -= state.getUnit(_id, theMove.unit()).damage();
				}
					
				moveVec.add(moves.get(u).get(actionMoveIndex));
			} else {
				moveVec.add(moves.get(u).get(closestMoveIndex));
			}
			
		}
	}
}
