package bwmcts.combat;


import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;

import bwmcts.clustering.UPGMA;
import bwmcts.mcts.guct.GUCTCD;
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

public class GuctcdLogic extends Player implements ICombatLogic {

	private GUCTCD guctcd;
	private SparcraftUI ui;
	public GuctcdLogic(JNIBWAPI bwapi, GUCTCD guctcd){
		
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

			UPGMA upgmaPlayerA = new UPGMA(state.getAllUnit()[0], 1, 1);
			UPGMA upgmaPlayerB = new UPGMA(state.getAllUnit()[1], 1, 1);
			List<UnitAction> move = guctcd.search(state, upgmaPlayerA, upgmaPlayerB, time);
		
			executeActions(bwapi,state,move);
		} catch(Exception e){
			e.printStackTrace();
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
		
		moveVec.clear();
		
		UPGMA upgmaPlayerA = new UPGMA(state.getAllUnit()[ID()], 1, 1);
		UPGMA upgmaPlayerB = new UPGMA(state.getAllUnit()[state.getEnemy(ID())], 1, 1);
		
		for(UnitAction action : guctcd.search(state, upgmaPlayerA, upgmaPlayerB, 40))
			moveVec.add(action.clone());
	
	}

}
