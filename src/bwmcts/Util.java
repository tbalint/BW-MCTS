package bwmcts;

import bwmcts.simulator.Position;

public class Util {
	
	public static int distance(Position a, Position b) {
		return (int) Math.sqrt( (a.getX() - b.getX())*(a.getX() - b.getX()) + (a.getY() - b.getY())*(a.getY() - b.getY()) );
	}

	public static int distance(int ax, int ay, int bx, int by) {
		return (int) Math.sqrt( (ax - bx)*(ax - bx) + (ay - by)*(ay - by) );
	}
	
}
