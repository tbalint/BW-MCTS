package bwmcts.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javabot.BWAPIEventListener;
import javabot.JNIBWAPI;
import javabot.types.UnitType;
import javabot.types.UnitType.UnitTypes;
import bwmcts.combat.GPortfolioGreedyLogic;
import bwmcts.combat.GuctcdLogic;
import bwmcts.combat.IuctcdLogic;
import bwmcts.combat.PortfolioGreedyLogic;
import bwmcts.combat.UctcdLogic;
import bwmcts.mcts.guct.GUCTCD;
import bwmcts.mcts.iuct.IUCTCD;
import bwmcts.sparcraft.*;
import bwmcts.sparcraft.players.Player;
import bwmcts.sparcraft.players.Player_AttackClosest;
import bwmcts.sparcraft.players.Player_Defense;
import bwmcts.sparcraft.players.Player_Kite;
import bwmcts.sparcraft.players.Player_NoOverKillAttackValue;

public class AiVsAiTest implements BWAPIEventListener  {
	
	JNIBWAPI bwapi;
	
	public static void main(String[] args) throws Exception{
		System.out.println("Create TC instance");
		AiVsAiTest tc=new AiVsAiTest();
		tc.bwapi=new JNIBWAPI(tc);
		tc.bwapi.start();
		
	}

	@Override
	public void connected() {
		// TODO Auto-generated method stub
		System.out.println("BWAPI connected");
		bwapi.loadTypeData();
		try {
		AnimationFrameData.Init();
		PlayerProperties.Init();
		WeaponProperties.Init(bwapi);
		UnitProperties.Init(bwapi);
		//tc.bwapi.start();
		
		System.out.println("BWAPI created"+ bwapi.getUnitType(3).getName());
		
		//Player p2 = new Player_Kite(1);
		
		Player p1 = new UctcdLogic(bwapi, new GUCTCD(1.6, 20, 1, 0, 500, false));
		Player p2 = new Player_NoOverKillAttackValue(1);
		//Player p2 = new UctcdLogic(bwapi, new GUCTCD(1.6, 2, 0, 1, 20, false));*´
		
		//marineTest(p1, p2);
		
		realisticTest(p1, p2);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/***********************
	 ***  REALISTIC TEST 
	 * @param p2 
	 * @param p1 ***
	 ***********************/
	private void realisticTest(Player p1, Player p2) {
		try {
			float result = testRealisticGames(p1, p2, 64, 32, 16, 1);
			System.out.println("REALISTIC TEST RESULT: " + result);
			// TODO:
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/********************
	 ***  MARINE TEST ***
	 ********************/
	private void marineTest(Player p1, Player p2) {
		
		try {
			float result = testMarineGames(p1, p2, 128, 1);
			System.out.println("MARINE TEST RESULT: " + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Returns the average score of the games.
	 * @param p1
	 * @param p2
	 * @param marines
	 * @param games
	 * @return
	 * @throws Exception
	 */
	float testMarineGames(Player p1, Player p2, int marines, int games) throws Exception{
		
		HashMap<UnitTypes, Integer> unitsA = new HashMap<UnitType.UnitTypes, Integer>();
		unitsA.put(UnitTypes.Terran_Marine, marines);
		HashMap<UnitTypes, Integer> unitsB = new HashMap<UnitType.UnitTypes, Integer>();
		unitsB.put(UnitTypes.Terran_Marine, marines);
		
		float score = 0;
		
		for(int i = 0; i < games; i++){
			float result = testGame(p1, p2, unitsA, unitsB);
			System.out.println("Result of game " + i + ": " + result);
			score += result;
		}
		
		return score / games;
		
	}
	
	/**
	 * Returns the average score of the games.
	 * @param p1
	 * @param p2
	 * @param marines
	 * @param games
	 * @return
	 * @throws Exception
	 */
	float testRealisticGames(Player p1, Player p2, int marines, int firebats, int tanks, int games) throws Exception{
		
		HashMap<UnitTypes, Integer> unitsA = new HashMap<UnitType.UnitTypes, Integer>();
		unitsA.put(UnitTypes.Terran_Siege_Tank_Tank_Mode, tanks);
		unitsA.put(UnitTypes.Terran_Marine, marines);
		unitsA.put(UnitTypes.Terran_Firebat, firebats);
		
		HashMap<UnitTypes, Integer> unitsB = new HashMap<UnitType.UnitTypes, Integer>();
		unitsB.put(UnitTypes.Terran_Siege_Tank_Tank_Mode, tanks);
		unitsB.put(UnitTypes.Terran_Marine, marines);
		unitsB.put(UnitTypes.Terran_Firebat, firebats);
		
		float score = 0;
		
		for(int i = 0; i < games; i++){
			float result = testGame(p1, p2, unitsA, unitsB);
			System.out.println("Result of game " + i + ": " + result);
			score += result;
		}
		
		return score / games;
		
	}
	

	int testGame(Player p1, Player p2, HashMap<UnitTypes, Integer> unitsA, HashMap<UnitTypes, Integer> unitsB) throws Exception
	{

		GameState initialState = gameState(unitsA, unitsB);
		
		p1.setID(0);
		p2.setID(1);
	    
	    // enter a maximum move limit for the game to go on for
	    int moveLimit = 2000;

	    // contruct the game
	    Game g=new Game(initialState, p1, p2, moveLimit,true);

	    // play the game
	    g.play();

	    // you can access the resulting game state after g has been played via getState
	    GameState finalState = g.getState();
	    // you can now evaluate the state however you wish. let's use an LTD2 evaluation from the point of view of player one
	    StateEvalScore score = finalState.eval(Players.Player_One.ordinal(), EvaluationMethods.LTD2);
	    // StateEvalScore has two components, a numerical score and a number of Movement actions performed by each player
	    // with this evaluation, positive val means win, negative means loss, 0 means tie
	    return score._val;
	}
	
	private GameState gameState(HashMap<UnitTypes, Integer> unitsA,
			HashMap<UnitTypes, Integer> unitsB) throws Exception {
		
		// GameState only has a default constructor, you must add units to it manually
	    GameState state=new GameState();
	    state.setMap(new Map(50, 30));
	    
	    int startXA = 275;
	    int startXB = 575;
	    int space = 20;
	    int startY = space*4;
	    int unitsPerLine = 16;
	    
	    for(UnitTypes type : unitsA.keySet()){
	    	
	    	try {
	    	    state.addUnit(bwapi.getUnitType(type.ordinal()), Players.Player_One.ordinal(),new Position(startXA, startY + space));
	    	} catch (Exception e){}
	    	
 	    	for(int i = 1; i < unitsA.get(type); i++){
 	    		int x = startXA - (i/unitsPerLine) * space;
 	    		int y = startY + space*(i%unitsPerLine);
 	    		try {
 	    			state.addUnit(bwapi.getUnitType(type.ordinal()), Players.Player_One.ordinal(), new Position(x, y));
 	    		} catch (Exception e){
 		 	    	e.printStackTrace();
 		 	    }
 	    	}
	 	    
	    	startXA -= space * 2;
	    	
	    }
	    
	    for(UnitTypes type : unitsB.keySet()){
	    	
	    	try {
	    	    state.addUnit(bwapi.getUnitType(type.ordinal()), Players.Player_Two.ordinal(),new Position(startXB, startY + space));
	    	} catch (Exception e){}
	    	
 	    	for(int i = 1; i < unitsB.get(type); i++){
 	    		int x = startXB + (i/unitsPerLine) * space;
 	    		int y = startY + space*(i%unitsPerLine);
 	    		try {
 	    			state.addUnit(bwapi.getUnitType(type.ordinal()), Players.Player_Two.ordinal(), new Position(x, y));
	 	    	} catch (Exception e){
		 	    	e.printStackTrace();
		 	    }
 	    	}
	 	    
	    	
	    	startXB += space;
	    	
	    }
	 	
	    return state;
	}

	

	@Override
	public void gameStarted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameEnded() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(int keyCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void matchEnded(boolean winner) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void playerLeft(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nukeDetect(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nukeDetect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unitDiscover(int unitID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unitEvade(int unitID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unitShow(int unitID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unitHide(int unitID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unitCreate(int unitID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unitDestroy(int unitID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unitMorph(int unitID) {
		// TODO Auto-generated method stub
		
	}
}
