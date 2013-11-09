package bwmcts.combat;


import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;

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

public class UctcdLogic extends Player implements ICombatLogic {

	private UCTCD uctcd;
	private SparcraftUI ui;
	public UctcdLogic(JNIBWAPI bwapi, UCTCD uctcd){
		
		this.uctcd = uctcd;
		
		bwapi.loadTypeData();
		AnimationFrameData.Init();
		PlayerProperties.Init();
		WeaponProperties.Init(bwapi);
		UnitProperties.Init(bwapi);
		/*ui = new SparcraftUI(null);
        
        // Setup of the frame containing the game
        JFrame f = new JFrame();
        f.setSize(1000,700);
        f.setTitle("Sparcraft in JAVA");
        f.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(ui);    
        f.setVisible(true);*/

	}
	
	@Override
	public void act(JNIBWAPI bwapi, int time) {
		GameState state = new GameState(bwapi);
		/*ui.setGameState(state);
		ui.repaint();*/
		try{
		//state.print();
		
		List<UnitAction> move = uctcd.search(state, time);
		
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
		    	//System.out.println(bwapi.getUnit(ourUnit.getId()).getGroundWeaponCooldown()+" cooldown;     "+ ourUnit.attackCooldown()+ "; "+  move.toString());
		    	if (move._moveType == UnitActionTypes.ATTACK)
		    	{
		    		Unit enemyUnit=state.getUnit(enemyPlayer,move._moveIndex);
		            //Unit & enemyUnit(getUnitByID(enemyPlayer ,move._moveIndex));
		    			
		    		// attack the unit
		    		//ourUnit.attack(move, enemyUnit, state._currentTime);

		    		bwapi.attack(ourUnit.getId(), enemyUnit.getId());

		    		// enemy unit takes damage if it is alive
		    		/*if (enemyUnit.isAlive())
		    		{				
		    			enemyUnit.takeAttack(ourUnit);

		    			// check to see if enemy unit died
		    			if (!enemyUnit.isAlive())
		    			{
		    				// if it died, remove it
		    				state._numUnits[enemyPlayer]--;
		    			}
		    		}*/			
		    	}
		    	else if (move._moveType == UnitActionTypes.MOVE)
		    	{
		    		//_numMovements[player]++;

		    		//ourUnit.move(move, _currentTime);
		    		
		    		bwapi.move(ourUnit.getId(), move.pos().getX(), move.pos().getY());
		    		//bwapi.move(ourUnit.getId(), move.pos().getX(), move.pos().getY());
		    	}
		    	else if (move._moveType == UnitActionTypes.HEAL)
		    	{
		    		Unit ourOtherUnit=state.getUnit(player,move._moveIndex);
		    			
		    		// attack the unit
		    		//ourUnit.heal(move, ourOtherUnit, _currentTime);
		    		bwapi.rightClick(ourUnit.getId(), ourOtherUnit.getId());	
		    		/*if (ourOtherUnit.isAlive())
		    		{
		    			ourOtherUnit.takeHeal(ourUnit);
		    		}*/
		    	}
		    	else if (move._moveType == UnitActionTypes.RELOAD)
		    	{
		    		//ourUnit.waitUntilAttack(move, _currentTime);
		    	}
		    	else if (move._moveType == UnitActionTypes.PASS)
		    	{
		    		//ourUnit.pass(move, _currentTime);
		    	}
			}
		}
	}

	public void getMoves(GameState state, HashMap<Integer,List<UnitAction>> moves, List<UnitAction>  moveVec)
	{
		
		GameState clone = state.clone();
		moveVec.clear();
		
		for(UnitAction action : uctcd.search(clone, 40))
			moveVec.add(action.clone());
	
	}

}
