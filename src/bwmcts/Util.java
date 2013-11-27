package bwmcts;

import java.util.List;

import bwmcts.sparcraft.Position;
import bwmcts.sparcraft.Unit;

public class Util {
	
	public static int distance(Position a, Position b) {
		return (int) Math.sqrt( (a.getX() - b.getX())*(a.getX() - b.getX()) + (a.getY() - b.getY())*(a.getY() - b.getY()) );
	}

	public static int distance(int ax, int ay, int bx, int by) {
		return (int) Math.sqrt( (ax - bx)*(ax - bx) + (ay - by)*(ay - by) );
	}

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
				
				distance += distance(a.pos(), b.pos());
				n++;
			}
		}
		
		return distance/n;
	}
	
}
