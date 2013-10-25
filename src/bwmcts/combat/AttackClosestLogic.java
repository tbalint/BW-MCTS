package bwmcts.combat;

import bwmcts.Util;
import bwmcts.simulator.Position;
import javabot.JNIBWAPI;
import javabot.model.Unit;

public class AttackClosestLogic implements ICombatLogic {

	@Override
	public void act(JNIBWAPI bwapi, int time) {
		
		for(Unit unit : bwapi.getMyUnits()){
			
			Position position = new Position(unit.getX(), unit.getY());
			
			// Find closest units enemy
			int closestDistance = Integer.MAX_VALUE;
			Unit closestEnemy = null;
			for(Unit enemy : bwapi.getEnemyUnits()){
				
				Position enemyPosition = new Position(enemy.getX(), enemy.getY());
				
				int distance = Util.distance(position, enemyPosition);
				
				if (enemy.isLifted() || bwapi.getUnitType(enemy.getID()).isFlyer()){
					if (distance < closestDistance && bwapi.getUnitType(unit.getID()).isCanAttackAir()){
						closestEnemy = enemy;
						closestDistance = distance;
					}
				} else {
					if (distance < closestDistance && bwapi.getUnitType(unit.getID()).isCanAttackGround()){
						closestEnemy = enemy;
						closestDistance = distance;
					}
				}
				
			}
			
			if (closestEnemy == null)
				continue;
			
			int airWeapon = bwapi.getUnitType(unit.getID()).getAirWeaponID();
			int airRange = bwapi.getWeaponType(airWeapon).getMinRange();
			int groundWeapon = bwapi.getUnitType(unit.getID()).getGroundWeaponID();
			int groundRange = bwapi.getWeaponType(groundWeapon).getMinRange();
			int range = groundRange;
			
			if (closestEnemy.isLifted() || bwapi.getUnitType(closestEnemy.getID()).isFlyer())
				range = airRange;
			
			if (closestDistance <= range)
				bwapi.attack(unit.getID(), closestEnemy.getID());
			else
				bwapi.move(unit.getID(), closestEnemy.getX(), closestEnemy.getY());
			
			
		}
		
	}

}
