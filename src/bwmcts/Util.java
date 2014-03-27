package bwmcts;

import java.util.List;


import bwmcts.sparcraft.Unit;

public class Util {
	
	public static float avgDistance(List<Unit> units) {
		
		float distance = 0;
		int n = 0;
		for(Unit a : units){
		
			boolean found = false;
			for(Unit b : units){
				
				if (a.getId() == b.getId()){
					found = true;
					continue;
				}
				
				if (!found)
					continue;
				
				distance += a.pos().getDistance(b.pos());
				n++;
			}
		}
		
		return distance/n;
	}
	
}
