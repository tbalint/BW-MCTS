package bwmcts.combat;

import bwmcts.simulator.Position;
import javabot.JNIBWAPI;
import javabot.model.Unit;

public class AttackClosestLogic implements ICombatLogic {

	@Override
	public void act(JNIBWAPI bwapi, int time) {
		
		for(Unit unit : bwapi.getMyUnits()){
			
			Position position = new Position(unit.getTileX(), unit.getTileY());
			
			// Find closes enemy
			float closestDistance = 999999999;
			Unit closestEnemy = null;
			for(Unit enemy : bwapi.getEnemyUnits()){
				
				Position enemyPosition = new Position(enemy.getTileX(), enemy.getTileY());
				
				float distance = util.distance(position, enemyPosition);
				
				if (distance < closestDistance){
					closestEnemy = enemy;
					closestDistance = distance;
				}
				
			}
			/*
			if (closestEnemy.){
				
			}
			*/
		}
		
	}

}
