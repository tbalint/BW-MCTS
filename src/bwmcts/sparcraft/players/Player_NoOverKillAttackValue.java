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
		float actionHighestDPS	= 0;
		int closestMoveIndex = 0;
		int closestMoveDist	= Integer.MAX_VALUE;
		UnitAction move;
		Unit ourUnit;
		int dist=0;
		float dpsHPValue=0;
		Unit closestUnit ;
		UnitAction theMove;
		int m=0;
		List<UnitAction> movesForU;
		for (Integer u : moves.keySet()){
			closestUnit= null;
			foundUnitAction=false;
			actionMoveIndex=0;
			actionHighestDPS=0;
			closestMoveIndex =0;
			closestMoveDist=Integer.MAX_VALUE;
			ourUnit = state.getUnit(_id, u);
			//Unit ourUnit = state.getUnitByID(u);
			
			if (ourUnit == null || moves.get(u) == null){
				//state.print();
				System.out.println(ourUnit + " " + _id + " " + u);
			}
			movesForU=moves.get(u);
			for (m=0; m<movesForU.size(); m++)
			{
				//long g=System.nanoTime();
				move	= movesForU.get(m);
				//timeOnHpCopying+=System.nanoTime()-g;	
				if ((move.type() == UnitActionTypes.ATTACK) && (hpRemaining[move.index()] > 0))
				{
					
					dpsHPValue 	= (state.getUnit(enemy, move.index()).dpf() / hpRemaining[move.index()]);

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
				else if (move.type() == UnitActionTypes.HEAL){
					
					dpsHPValue =	(state.getUnit(move.player(), move.index()).dpf() / hpRemaining[move.index()]);

					if (dpsHPValue > actionHighestDPS)
					{
						actionHighestDPS = dpsHPValue;
						actionMoveIndex = m;
						foundUnitAction = true;
					}
				}
				
				if (!foundUnitAction){
					if (closestUnit==null){
						
						closestUnit= ourUnit.canHeal() ? state.getClosestOurUnit(_id, u) : state.getClosestEnemyUnit(ourUnit.currentPosition(state._currentTime),enemy,Integer.MAX_VALUE,0,0);
						
					}
					
					
					if (move.type() == UnitActionTypes.RELOAD)
					{
						if (ourUnit.canAttackTarget(closestUnit, state._currentTime))
						{
							closestMoveIndex = m;
							break;
						}
					}
					else if (move.type() == UnitActionTypes.MOVE)
					{
						
						dist = closestUnit.getDistanceSqToPosition(ourUnit.pos().getX() + Constants.Move_DirX[move.index()], 
											ourUnit.pos().getY() + Constants.Move_DirY[move.index()], state.getTime());
	
						if (dist < closestMoveDist)
						{
							closestMoveDist = dist;
							closestMoveIndex = m;
						}
					}
				}
			}

			
			if (foundUnitAction){
				theMove = movesForU.get(actionMoveIndex);
				if (theMove.type() == UnitActionTypes.ATTACK)
				{
					hpRemaining[theMove.index()] -= state.getUnit(_id, theMove.unit()).damage();
				}
					
				moveVec.add(movesForU.get(actionMoveIndex));
			} else {
				moveVec.add(movesForU.get(closestMoveIndex));
			}
			
		}
	}
}
