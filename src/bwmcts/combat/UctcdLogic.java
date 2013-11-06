package bwmcts.combat;


import java.util.HashMap;
import java.util.List;

import bwmcts.mcts.uctcd.UCTCD;

import bwmcts.sparcraft.AnimationFrameData;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.PlayerProperties;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.UnitProperties;
import bwmcts.sparcraft.WeaponProperties;
import bwmcts.sparcraft.players.Player;
import javabot.JNIBWAPI;

public class UctcdLogic extends Player implements ICombatLogic {

	private UCTCD uctcd;
	
	public UctcdLogic(JNIBWAPI bwapi, UCTCD uctcd){
		
		this.uctcd = uctcd;
		
		bwapi.loadTypeData();
		AnimationFrameData.Init();
		PlayerProperties.Init();
		WeaponProperties.Init(bwapi);
		UnitProperties.Init(bwapi);
		
	}
	
	@Override
	public void act(JNIBWAPI bwapi, int time) {
		
		GameState state = new GameState(bwapi);
		List<UnitAction> move = uctcd.search(state, time);
		
		
		
		
	}
	
	public void getMoves(GameState  state, HashMap<Integer,List<UnitAction>> moves, List<UnitAction>  moveVec)
	{
		
		GameState clone = state.clone();
		moveVec.clear();
		for(UnitAction action : uctcd.search(clone, 40))
			moveVec.add(action.clone());
	
	}

}
