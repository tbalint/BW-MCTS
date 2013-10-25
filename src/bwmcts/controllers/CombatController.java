package bwmcts.controllers;


import bwmcts.combat.ICombatLogic;
import javabot.BWAPIEventListener;
import javabot.JNIBWAPI;
import javabot.model.*;
import javabot.util.BWColor;

public class CombatController implements BWAPIEventListener {

	private JNIBWAPI bwapi;
	private ICombatLogic combatLogic;
	
	public static void main(String[] args) {
		new CombatController();
	}
	
	public CombatController() {
		bwapi = new JNIBWAPI(this);
		
		// combatLogic = new 
		
		bwapi.start();
	} 
	public void connected() {
		bwapi.loadTypeData();
	}
	
	// Method called at the beginning of the game.
	public void gameStarted() {		
		System.out.println("Game Started!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

		// allow me to manually control units during the game
		bwapi.enableUserInput();
		
		// set game speed to 30 (0 is the fastest. Tournament speed is 20)
		// You can also change the game speed from within the game by "/speed X" command.
		//bwapi.setGameSpeed(20);
		
		// analyze the map
		bwapi.loadMapData(true);
		
		// ============== YOUR CODE GOES HERE =======================

		// This is called at the beginning of the game. You can 
		// initialize some data structures (or do something similar) 
		// if needed. For example, you should maintain a memory of seen 
		// enemy buildings.
		
		bwapi.printText("Hello world!");
		bwapi.printText("This map is called "+bwapi.getMap().getName());
		bwapi.printText("My race ID: "+String.valueOf(bwapi.getSelf().getRaceID()));				// Z=0,T=1,P=2
		bwapi.printText("Enemy race ID: "+String.valueOf(bwapi.getEnemies().get(0).getRaceID()));	// Z=0,T=1,P=2
		
		// ==========================================================
	}
	
	// Method called once every second.
	public void act() {
		
		// ============== YOUR CODE GOES HERE =======================

		int time = 20;	// 20 ms
		combatLogic.act(bwapi, time);
		
	}
	
	// Method called on every frame (approximately 30x every second).
	public void gameUpdate() {
			
		// Draw debug information on screen
		drawDebugInfo();

		// Call the act() method every X frames
		int x = 1;
		if (bwapi.getFrameCount() % x == 0) 
			act();
		
	}

	// Some additional event-related methods.
	public void gameEnded() {}
	public void matchEnded(boolean winner) {}
	public void nukeDetect(int x, int y) {}
	public void nukeDetect() {}
	public void playerLeft(int id) {}
	public void unitCreate(int unitID) {}
	public void unitDestroy(int unitID) {}
	public void unitDiscover(int unitID) {}
	public void unitEvade(int unitID) {}
	public void unitHide(int unitID) {}
	public void unitMorph(int unitID) {}
	public void unitShow(int unitID) {
		
	}
	public void keyPressed(int keyCode) {}
	
    // Returns the id of a unit of a given type, that is closest to a pixel position (x,y), or -1 if we
    // don't have a unit of this type
    public int getNearestUnit(int unitTypeID, int x, int y) {
    	int nearestID = -1;
	    double nearestDist = 9999999;
	    for (Unit unit : bwapi.getMyUnits()) {
	    	if ((unit.getTypeID() != unitTypeID) || (!unit.isCompleted())) continue;
	    	double dist = Math.sqrt(Math.pow(unit.getX() - x, 2) + Math.pow(unit.getY() - y, 2));
	    	if (nearestID == -1 || dist < nearestDist) {
	    		nearestID = unit.getID();
	    		nearestDist = dist;
	    	}
	    }
	    return nearestID;
    }	
	
	// Draws debug information on the screen. 
	// Reimplement this function however you want. 
	public void drawDebugInfo() {

		// Draw our home position.
		//bwapi.drawText(new Point(5,0), "Our home position: "+String.valueOf(homePositionX)+","+String.valueOf(homePositionY), true);
		
		// Draw circles over workers (blue if they're gathering minerals, green if gas, yellow if they're constructing).
		for (Unit u : bwapi.getMyUnits())  {
			if (u.isGatheringMinerals()) bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.BLUE, false, false);
			else if (u.isGatheringGas()) bwapi.drawCircle(u.getX(), u.getY(), 12, BWColor.GREEN, false, false);
		}
		
	}
	
	
}
