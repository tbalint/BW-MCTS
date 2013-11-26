package bwmcts.combat;


import java.awt.Color;
import java.util.ArrayList;
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
import bwmcts.sparcraft.Position;
import bwmcts.sparcraft.SparcraftUI;
import bwmcts.sparcraft.Unit;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.UnitActionTypes;
import bwmcts.sparcraft.UnitProperties;
import bwmcts.sparcraft.WeaponProperties;
import bwmcts.sparcraft.players.Player;
import javabot.JNIBWAPI;
import javabot.util.BWColor;

public class UctcdLogic extends Player implements ICombatLogic {

	private UCTCD uctcd;
	private GUCTCD guctcd;
	private IUCTCD iuctcd;
	private SparcraftUI ui;
	private HashMap<Integer,UnitAction> actions=new HashMap<Integer,UnitAction>();
	
	public UctcdLogic(JNIBWAPI bwapi, UCTCD uctcd){
		
		this.uctcd = uctcd;
		
		bwapi.loadTypeData();
		AnimationFrameData.Init();
		PlayerProperties.Init();
		WeaponProperties.Init(bwapi);
		UnitProperties.Init(bwapi);

	}
	
	public UctcdLogic(JNIBWAPI bwapi, GUCTCD uctcd){
		
		this.guctcd = uctcd;
		
		bwapi.loadTypeData();
		AnimationFrameData.Init();
		PlayerProperties.Init();
		WeaponProperties.Init(bwapi);
		UnitProperties.Init(bwapi);

	}
	
	public UctcdLogic(JNIBWAPI bwapi, IUCTCD uctcd){
		
		this.iuctcd = uctcd;
		
		bwapi.loadTypeData();
		AnimationFrameData.Init();
		PlayerProperties.Init();
		WeaponProperties.Init(bwapi);
		UnitProperties.Init(bwapi);

	}

	@Override	
	public void act(JNIBWAPI bwapi, int time) {
		
		try{
			GameState state = new GameState(bwapi);
			//state.print();
			List<UnitAction> move=new ArrayList<UnitAction>();
			if (uctcd!=null)
				move = uctcd.search(state.clone(), time);
			if (iuctcd!=null){
				move = iuctcd.search(state.clone(), time);
			}
			if (guctcd!=null){
				
				try{
					UPGMA upgmaPlayerA = new UPGMA(state.getAllUnit()[bwapi.getSelf().getID()], 1, 1);
					UPGMA upgmaPlayerB = new UPGMA(state.getAllUnit()[bwapi.getEnemies().get(0).getID()], 1, 1);
					move = guctcd.search(state, upgmaPlayerA, upgmaPlayerB, time);
					HashMap<Integer, List<Unit>> clustersA = guctcd.getClustersA();
					HashMap<Integer, List<Unit>> clustersB = guctcd.getClustersA();
					
					drawClusters(bwapi, clustersA);
					drawClusters(bwapi, clustersB);
					
				} catch(Exception e){
					e.printStackTrace();
				}
				
			}
			
			executeActions(bwapi,state,move);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void drawClusters(JNIBWAPI bwapi, HashMap<Integer, List<Unit>> clusters) {
		
		if (clusters == null)
			return;
		
		for(Integer i : clusters.keySet()){
			
			for (Unit u : clusters.get(i)){
				
				bwapi.drawCircle(u.pos().getX(), u.pos().getY(), 6, getColor(i), false, false);
				
			}
			
		}
		
	}
	

	private int getColor(Integer i) {
		switch (i){
		case 0:
			return BWColor.CYAN;
		case 1:
			return BWColor.GREEN;
		case 2:
			return BWColor.WHITE;
		case 3:
			return BWColor.PURPLE;
		case 4:
			return BWColor.ORANGE;
		case 5:
			return BWColor.YELLOW;
		case 6:
			return BWColor.TEAL;
		case 7:
			return BWColor.RED;
		case 8:
			return BWColor.BLUE;
		default:
			return BWColor.BLACK;
	}
	}


	private HashMap<Integer,UnitAction> firstAttack=new HashMap<Integer,UnitAction>();
	
	private void executeActions(JNIBWAPI bwapi, GameState state, List<UnitAction> moves) {
		if (moves!=null && !moves.isEmpty()){
			for (UnitAction move : moves){
				
				Unit ourUnit		= state.getUnit(move._player, move._unit);
		    	int player		= ourUnit.player();
		    	int enemyPlayer  = state.getEnemy(player);
		    	if (firstAttack.get(ourUnit.getId())!=null){
		    		if (bwapi.getUnit(firstAttack.get(ourUnit.getId())._moveIndex) == null){
		    			firstAttack.remove(ourUnit.getId());
		    		}
		    		if (bwapi.getUnit(ourUnit.getId()).isAttackFrame()){
		    			firstAttack.remove(ourUnit.getId());
		    		}
		    		if (bwapi.getUnit(ourUnit.getId()).getLastCommandFrame()+10<bwapi.getFrameCount()){
		    			firstAttack.remove(ourUnit.getId());
		    		}
		    		
		    	}
		    	if (firstAttack.get(ourUnit.getId())!=null){continue;}
		    	/* After assigning the attack for a unit it shouldn't assign any actions to it until the target is dead or we have an attackFrame 
		    	 * 
		    	 * 
		    	 * 
		    	 * 
		    	 * */
		    	
		    	
		    	//System.out.println(bwapi.getUnit(ourUnit.getId()).getGroundWeaponCooldown()+" cooldown "+  move.toString());
	
		    	//bwapi.drawCircle(bwapi.getUnit(ourUnit.getId()).getX(),bwapi.getUnit(ourUnit.getId()).getY(),ourUnit.range(),179,false,false);
		    	if (bwapi.getUnit(ourUnit.getId()).isAttackFrame()){continue;}
		    	
		    	if (move._moveType == UnitActionTypes.ATTACK && bwapi.getUnit(ourUnit.getId()).getGroundWeaponCooldown()==0)
		    	{
		    		Unit enemyUnit=state.getUnit(enemyPlayer,move._moveIndex);
		    		
		    		bwapi.attack(ourUnit.getId(), enemyUnit.getId());
		    		firstAttack.put(ourUnit.getId(), move.clone());
		    		
		    		//if (!bwapi.getUnit(ourUnit.getId()).isAccelerating() && !bwapi.getUnit(ourUnit.getId()).isStartingAttack()){
		    		//	System.out.println("CanAttack: "+ourUnit.canAttackTarget(enemyUnit, bwapi.getFrameCount())+", isMoving: "+bwapi.getUnit(ourUnit.getId()).isMoving()+",isAttacking: "+bwapi.getUnit(ourUnit.getId()).isAttacking());	
		    		//	bwapi.attack(ourUnit.getId(), enemyUnit.getId());
		    		//}
		    	}
		    	else if (move._moveType == UnitActionTypes.MOVE)
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
		} else {
			System.out.println("---------------------NO MOVES----------------------------");
			
		}
	}

	public void getMoves(GameState state, HashMap<Integer,List<UnitAction>> moves, List<UnitAction>  moveVec)
	{
		
		moveVec.clear();
		int time = 40;
		List<UnitAction> move=new ArrayList<UnitAction>();
		if (uctcd!=null)
			move = uctcd.search(state.clone(), time-5);
		if (iuctcd!=null){
			move = iuctcd.search(state.clone(), time-5);
		}
		if (guctcd!=null){
			
			try{
				UPGMA upgmaPlayerA = new UPGMA(state.getAllUnit()[ID()], 1, 1);
				UPGMA upgmaPlayerB = new UPGMA(state.getAllUnit()[state.getEnemy(ID())], 1, 1);
				move = guctcd.search(state, upgmaPlayerA, upgmaPlayerB, time-5);
			} catch (Exception e){
				
			}
		}
		
		for(UnitAction action : move)
			moveVec.add(action.clone());
	
	}
	
	public void drawUnitOneInfo(JNIBWAPI bwapi){
		javabot.model.Unit my=bwapi.getUnit(0);
		
		bwapi.drawText(0, 0, "isMoving: "+my.isMoving(), false);
		bwapi.drawText(0, 20, "isattacking: "+my.isAttacking(), false);
		bwapi.drawText(0, 40, "isattackframe: "+my.isAttackFrame(), false);
		bwapi.drawText(0, 60, "isacc: "+my.isAccelerating(), false);
		bwapi.drawText(0, 80, "isIdle: "+my.isIdle(), false);
		bwapi.drawText(0, 100, "isStartingAttack: "+my.isStartingAttack(), false);
	}

}
