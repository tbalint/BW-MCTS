package bwmcts.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwmcts.Util;
import bwmcts.simulator.Position;
import javabot.JNIBWAPI;
import javabot.model.Unit;
import javabot.util.BWColor;

public class AttackClosestLogic implements ICombatLogic {

	Map<Integer, Integer> attackingUnits = new HashMap<Integer, Integer>();
	
	@Override
	public void act(JNIBWAPI bwapi, int time) {
		
		System.out.println("--------------- " + bwapi.getMyUnits().size() + " units.");
	
		List<Unit> myUnits = bwapi.getMyUnits();
		List<Unit> enemyUnits = bwapi.getEnemyUnits();
		
		for(Unit unit : myUnits){
			
			System.out.println(bwapi.getUnitType(unit.getTypeID()).getName());
			
			bwapi.drawCircle(unit.getX(), unit.getY(), 6, BWColor.YELLOW, false, false);
			
			Position position = new Position(unit.getX(), unit.getY());
			
			// Find closest units enemy
			int closestDistance = Integer.MAX_VALUE;
			Unit closestEnemy = null;
			for(Unit enemy : enemyUnits){
				
				Position enemyPosition = new Position(enemy.getX(), enemy.getY());
				
				int distance = Util.distance(position, enemyPosition);
				System.out.println("d = " + distance);
				
				if (distance < closestDistance){
					closestEnemy = enemy;
					closestDistance = distance;
				}
				
			}
			
			if (closestEnemy == null){
				System.out.println("No enemy found");
				continue;
			}
			
			Unit lastTarget = null;
			if (attackingUnits.containsKey(unit.getID()))
				lastTarget = bwapi.getUnit(attackingUnits.get(unit.getID()));
				
			if (lastTarget == null || closestEnemy.getID() != lastTarget.getID()){
				bwapi.rightClick(unit.getID(), closestEnemy.getID());
				attackingUnits.put(unit.getID(), closestEnemy.getID());
			}
			
			Unit newTarget = bwapi.getUnit(attackingUnits.get(unit.getID()));
			bwapi.drawLine(unit.getX(), unit.getY(), newTarget.getX(), newTarget.getY(), BWColor.RED, false);
			
		}
	}
}
