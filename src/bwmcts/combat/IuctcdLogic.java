package bwmcts.combat;


import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;

import bwmcts.mcts.iuct.IUCTCD;
import bwmcts.mcts.uctcd.UCTCD;
import bwmcts.mcts.uctcd.UCTCDsingle;

import bwmcts.sparcraft.AnimationFrameData;
import bwmcts.sparcraft.Game;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.PlayerProperties;
import bwmcts.sparcraft.Players;
import bwmcts.sparcraft.SparcraftUI;
import bwmcts.sparcraft.Unit;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.UnitActionTypes;
import bwmcts.sparcraft.UnitProperties;
import bwmcts.sparcraft.WeaponProperties;
import bwmcts.sparcraft.players.Player;
import javabot.JNIBWAPI;

public class IuctcdLogic extends Player implements ICombatLogic {

	private IUCTCD guctcd;
	private SparcraftUI ui;
	public IuctcdLogic(JNIBWAPI bwapi, IUCTCD guctcd){
		
		this.guctcd = guctcd;
		
		bwapi.loadTypeData();
		AnimationFrameData.Init();
		PlayerProperties.Init();
		WeaponProperties.Init(bwapi);
		UnitProperties.Init(bwapi);

	}
	
	@Override
	public void act(JNIBWAPI bwapi, int time) {
		GameState state = new GameState(bwapi);

		try{

		List<UnitAction> move = guctcd.search(state, time);
		
		executeActions(bwapi,state,move);
		} catch(Exception e){
			//e.printStackTrace();
		}
	}
	
	private void executeActions(JNIBWAPI bwapi, GameState state, List<UnitAction> moves) {
		if (moves!=null && !moves.isEmpty()){
			for (UnitAction move : moves){
				
				Unit ourUnit		= state.getUnit(move._player, move._unit);
		    	int player		= ourUnit.player();
		    	int enemyPlayer  = state.getEnemy(player);
		    	System.out.println(bwapi.getUnit(ourUnit.getId()).getGroundWeaponCooldown()+" cooldown; "+  move.toString());
		    	if (move._moveType == UnitActionTypes.ATTACK && bwapi.getUnit(ourUnit.getId()).getGroundWeaponCooldown()==0)
		    	{
		    		Unit enemyUnit=state.getUnit(enemyPlayer,move._moveIndex);

		    		bwapi.attack(ourUnit.getId(), enemyUnit.getId());

		
		    	}
		    	else if (move._moveType == UnitActionTypes.MOVE && bwapi.getUnit(ourUnit.getId()).getGroundWeaponCooldown()==0)
		    	{    		
		    		bwapi.move(ourUnit.getId(), move.pos().getX(), move.pos().getY());
		    	}
		    	else if (move._moveType == UnitActionTypes.HEAL)
		    	{
		    		Unit ourOtherUnit=state.getUnit(player,move._moveIndex);

		    		bwapi.rightClick(ourUnit.getId(), ourOtherUnit.getId());	
		    	}
		    	else if (move._moveType == UnitActionTypes.RELOAD)
		    	{
		    	}
		    	else if (move._moveType == UnitActionTypes.PASS)
		    	{
		    	}
			}
		}
	}

	public void getMoves(GameState state, HashMap<Integer,List<UnitAction>> moves, List<UnitAction>  moveVec)
	{
		
		GameState clone = state.clone();
		moveVec.clear();
		
		for(UnitAction action : guctcd.search(clone, 40))
			moveVec.add(action.clone());
	
	}

}
