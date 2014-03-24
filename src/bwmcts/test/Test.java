package bwmcts.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javabot.BWAPIEventListener;
import javabot.JNIBWAPI;
import javabot.types.UnitType;
import javabot.types.UnitType.UnitTypes;
import bwmcts.Util;
import bwmcts.clustering.UPGMA;
import bwmcts.combat.GPortfolioGreedyLogic;
import bwmcts.combat.GuctcdLogic;
import bwmcts.combat.IuctcdLogic;
import bwmcts.combat.PortfolioGreedyLogic;
import bwmcts.combat.UctcdLogic;
import bwmcts.mcts.guct.GUCTCD;
import bwmcts.mcts.iuct.IUCTCD;
import bwmcts.mcts.uctcd.UCTCD;
import bwmcts.sparcraft.*;
import bwmcts.sparcraft.players.Player;
import bwmcts.sparcraft.players.Player_AttackClosest;
import bwmcts.sparcraft.players.Player_Defense;
import bwmcts.sparcraft.players.Player_Kite;
import bwmcts.sparcraft.players.Player_KiteDPS;
import bwmcts.sparcraft.players.Player_NoOverKillAttackValue;
import bwmcts.sparcraft.players.Player_Random;

public class Test implements BWAPIEventListener  {
	
	private static boolean graphics = false;
	JNIBWAPI bwapi;
	
	public static void main(String[] args) throws Exception{
		System.out.println("Create TC instance");
		Test tc=new Test();
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
		
		graphics = true;
		
		Constants.Max_Units = 200;
		Constants.Max_Moves = Constants.Max_Units + Constants.Num_Directions + 1;
		
		//Player p1 = new Player_Kite(0);
		
		//Player p1 = new UctcdLogic(bwapi, new UCTCD(1.6, 20, 1, 0, 500, false), 200);
		GUCTCD guctcdA = new GUCTCD(1.6, 20, 1, 0, 100, false);
		guctcdA.setHpMulitplier(1);
		guctcdA.setClusters(6);
		GUCTCD guctcdB = new GUCTCD(1.6, 20, 0, 1, 100, false);
		guctcdB.setHpMulitplier(1);
		guctcdB.setClusters(6);
		Player p1 = new UctcdLogic(bwapi, new UCTCD(1.6, 20, 1, 0, 500, false, false),40);
		//Player p1 = new UctcdLogic(bwapi, new IUCTCD(1.6, 20, 1, 0, 500, false),40);
		//Player p1 = new UctcdLogic(bwapi, guctcdA, 40);
		//Player p1 = new GPortfolioGreedyLogic(bwapi, 1, 0, 100, 40, 6);
		//Player p1 = new PortfolioGreedyLogic(bwapi, 1, 0, 100, 400);
		
		//Player p1 = new Player_Random(0);		
		//Player p1 = new Player_KiteDPS(0);		
		//Player p1 = new Player_NoOverKillAttackValue(0);
		//Player p2 = new Player_KiteDPS(1);		
		//Player p1 = new UctcdLogic(bwapi, new UCTCD(1.6, 20, 1, 0, 500, false, true),40);
		//Player p1 = new UctcdLogic(bwapi, new IUCTCD(1.6, 20, 1, 0, 500, false),40);
		//Player p1 = new GPortfolioGreedyLogic(bwapi, 2, 2, 40, 1);
		//Player p1 = new PortfolioGreedyLogic(bwapi, 1, 0, 100, 400000);
		
		//Player p2 = new Player_Random(1);
		//Player p2 = new Player_NoOverKillAttackValue(1);
		//Player p2 = new Player_Random(1);
		
		//Player p2 = new UctcdLogic(bwapi, new IUCTCD(1.6, 20, 0, 1, 500, false), 40);
		//Player p2 = new UctcdLogic(bwapi, new UCTCD(1.6, 20, 0, 1, 500, false, false),40);
		Player p2 = new UctcdLogic(bwapi, guctcdB, 40);		
		//Player p2 = new GPortfolioGreedyLogic(bwapi, 2, 2, 30, 6);
		
		//oneTypeTest(p1, p2, UnitTypes.Terran_Marine, 25);

		//p2.setID(1);
		//oneTypeTest(p1, p2, UnitTypes.Zerg_Zergling, 10);

		//TODO: Write to file
		
		//PortfolioTest(p1, p2);
		
		realisticTest(p1, p2, 20);
		
		//upgmaTest(p1, p2, 100, 25);
		
		//upgmaScenario(p1, p2);
		
		//simulatorTest(p1, p2, 1, 250, 10, 100);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void PortfolioTest(Player p1, Player p2) throws Exception {
		int tanks = 8;
		int marines = 32;
		int firebats = 16;
		HashMap<UnitTypes, Integer> unitsA = new HashMap<UnitType.UnitTypes, Integer>();
		unitsA.put(UnitTypes.Terran_Siege_Tank_Tank_Mode, tanks);
		unitsA.put(UnitTypes.Terran_Marine, marines);
		unitsA.put(UnitTypes.Terran_Firebat, firebats);
		
		HashMap<UnitTypes, Integer> unitsB = new HashMap<UnitType.UnitTypes, Integer>();
		unitsB.put(UnitTypes.Terran_Siege_Tank_Tank_Mode, tanks);
		unitsB.put(UnitTypes.Terran_Marine, marines);
		unitsB.put(UnitTypes.Terran_Firebat, firebats);
		
		Constants.Max_Units = (marines+firebats+tanks)*2;
		Constants.Max_Moves = Constants.Max_Units + Constants.Num_Directions + 1;
		
		System.out.println("Marines: " + marines + "\tfirebats: " + firebats + "\tTanks: " + tanks + " on each side");
		
		List<Double> results = new ArrayList<Double>();

		testPortfolioGame(p1, p2, unitsA, unitsB);

	}

	private void testPortfolioGame(Player p1, Player p2,
			HashMap<UnitTypes, Integer> unitsA,
			HashMap<UnitTypes, Integer> unitsB) throws Exception {
		
		GameState initialState = gameState(unitsA, unitsB);
		
		p1.setID(0);
		p2.setID(1);
	    
	    // enter a maximum move limit for the game to go on for
	    int moveLimit = 20000;

	    // contruct the game
	    Game g=new Game(initialState, p1, p2, moveLimit, graphics);
	    
	    g.play();
		
	}

	/************************
	 * *** SIMULATOR TEST ***
	 * **********************
	 * @param p1
	 * @param p2
	 * @throws Exception 
	 */
	private void simulatorTest(Player p1, Player p2, int min, int max, int steps, int runs) throws Exception {
		
		// Combat size
		for(int i = 1; i < 20; i+=Math.max(1, i/4)){
			
			// Step limit
			for(int s = 0; s < steps; s ++){
				
				List<Double> times = new ArrayList<Double>();
				int limit = (int)(min + (float)(max-min)*(float)((float)s/(float)steps));
				
				// Runs
				for(int r = 0; r < runs; r++){
					
					//Constants.callOfDistanceFunction1=0;
					//Constants.callOfDistanceFunction2=0;
					//Constants.callOfDistanceFunction3=0;
					double time = runSimulator(p1, p2, i, limit);
					//System.out.println(Constants.callOfDistanceFunction1+" / "+Constants.callOfDistanceFunction2+" / "+Constants.callOfDistanceFunction3);
					if (time == -1)
						break;
					
					times.add(time);
				}
				
				// Calc deviation and average
				//System.out.println("Average: " + average(times) + "\tDeviation: " + deviation(times));
				System.out.println((i*7*2) + "\t" + limit + "\t" + average(times) + "\t" + deviation(times));
				
			}
			
		}
		
	}
	
	private double runSimulator(Player p1, Player p2, int i, int moveLimit) throws Exception {
		
		HashMap<UnitTypes, Integer> unitsA = new HashMap<UnitType.UnitTypes, Integer>();
		unitsA.put(UnitTypes.Terran_Siege_Tank_Tank_Mode, i);
		unitsA.put(UnitTypes.Terran_Marine, i*4);
		unitsA.put(UnitTypes.Terran_Firebat, i*2);
		
		HashMap<UnitTypes, Integer> unitsB = new HashMap<UnitType.UnitTypes, Integer>();
		unitsB.put(UnitTypes.Terran_Siege_Tank_Tank_Mode, i);
		unitsB.put(UnitTypes.Terran_Marine, i*4);
		unitsB.put(UnitTypes.Terran_Firebat, i*2);
		
		Constants.Max_Units = i*8*2;
		Constants.Max_Moves = Constants.Max_Units + Constants.Num_Directions + 1;
		
		GameState state = gameState(unitsA, unitsB);
		
		long a = System.nanoTime();
		
		// contruct the game
	    Game g=new Game(state, p1, p2, moveLimit, false);

	    // play the game
	    g.play();

		long b = System.nanoTime();
	    double time = (double)(b - a) / 1000000;
	    
	    //int limit = Math.min(g.getRounds(), moveLimit);
	    
	    if (moveLimit < g.getRounds())
	    	return -1;
	    	
	    //System.out.println("Units: " + i*7*2 + "\tMoveLimit: " + moveLimit + "\tTime: " + time + " ms.");

	    return time;
		
	}

	/********************
	 * *** UPGMA TEST ***
	 * ******************
	 * @param p1
	 * @param p2
	 * @throws Exception 
	 */
	private void upgmaTest(Player p1, Player p2, int runs, int numClusters) throws Exception {
		
		p1.setID(0);
		p2.setID(1);
		
		for(int i = 1; i < 50; i+=Math.max(1, i/4)){
			
			List<Double> times = new ArrayList<Double>();
			for (int r = 0; r < runs; r++){
				double time = runUpgma(p1, p2, i, numClusters);
				times.add(time);
			}
			
			// Calc deviation and average
			System.out.println((i*7) + "\t" + average(times) + "\t" + deviation(times));
			
		}
		
	}
	
	private double runUpgma(Player p1, Player p2, int i, int numClusters) throws Exception {
		
		HashMap<UnitTypes, Integer> unitsA = new HashMap<UnitType.UnitTypes, Integer>();
		unitsA.put(UnitTypes.Terran_Siege_Tank_Tank_Mode, i);
		unitsA.put(UnitTypes.Terran_Marine, i*4);
		unitsA.put(UnitTypes.Terran_Firebat, i*2);
		
		HashMap<UnitTypes, Integer> unitsB = new HashMap<UnitType.UnitTypes, Integer>();

		Constants.Max_Units = i*8;
		Constants.Max_Moves = Constants.Max_Units + Constants.Num_Directions + 1;
		
		GameState state = gameState(unitsA, unitsB);
	
		long a = System.nanoTime();
		UPGMA upgmaPlayerA = new UPGMA(state.getAllUnit()[0], 1, 1);
		HashMap<Integer, List<Unit>> clusters = upgmaPlayerA.getClusters(numClusters);
		long b = System.nanoTime();
	    double time = (double)(b - a) / 1000000;
	    //System.out.println("\nMarines: " + (i*4) + "\tFirebats: " + (i*2) + "\tTanks: " + i + "\tTime: " + time + " ms.");
	    
	    return time;
		
	}
	
	/**
	 * UPGMA Scenario for screenshots of clusterings.
	 * @param p1
	 * @param p2
	 */
	private void upgmaScenario(Player p1, Player p2) {
		
		try {
			testUPGMAScenario(p1,p2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	void testUPGMAScenario(Player p1, Player p2) throws Exception
	{
		
		GameState state = upgmaGameState();
		
		p1.setID(0);
		p2.setID(1);
	    
	    // enter a maximum move limit for the game to go on for
	    int moveLimit = 20000;

	    // contruct the game
	    Game g=new Game(state, p1, p2, moveLimit, graphics);

	    //UPGMA upgma = new UPGMA(initialState.getAllUnit()[0], 1, 1);
	    //upgma.getClusters(6);
	    //g.play();
	    /*
	    g.ui.c = 1;
	    g.ui.setGameState(state);
	    g.ui.repaint();
	    
	    Thread.sleep(2000);
        
	    g.ui.c = 10;
	    g.ui.setGameState(state);
	    g.ui.repaint();
	    
	    Thread.sleep(2000);
	    
	    g.ui.c = 50;
	    g.ui.setGameState(state);
	    g.ui.repaint();
	    
	    Thread.sleep(2000);
	    */
	    g.ui.c = 100;
	    g.ui.setGameState(state);
	    g.ui.repaint();
	    
	    Thread.sleep(2000);
	    
	    g.ui.c = 200;
	    g.ui.setGameState(state);
	    g.ui.repaint();
	    
	    Thread.sleep(2000);
	    
	    g.ui.c = 500;
	    g.ui.setGameState(state);
	    g.ui.repaint();
	    
	    Thread.sleep(2000);
	    
	    g.ui.c = 1000;
	    g.ui.setGameState(state);
	    g.ui.repaint();
	    
	    Thread.sleep(2000);
	    
	    g.ui.c = 2000;
	    g.ui.setGameState(state);
	    g.ui.repaint();
	    
	    Thread.sleep(2000);
	    
	    g.ui.c = 5000;
	    g.ui.setGameState(state);
	    g.ui.repaint();
	    
	    Thread.sleep(2000);
	    
	    g.ui.c = 10000;
	    g.ui.setGameState(state);
	    g.ui.repaint();
	    
	    Thread.sleep(2000);
	    
	    g.ui.c = 20000;
	    g.ui.setGameState(state);
	    g.ui.repaint();
	    
	    Thread.sleep(2000);
	    
	    g.ui.c = 50000;
	    g.ui.setGameState(state);
	    g.ui.repaint();
	    
	    Thread.sleep(2000);
	    
	    g.ui.c = 100000;
	    g.ui.setGameState(state);
	    g.ui.repaint();
	    
	    Thread.sleep(2000);
	    
	    g.ui.c = 200000;
	    g.ui.setGameState(state);
	    g.ui.repaint();
	    
	    Thread.sleep(2000);
	    
	    g.ui.c = 500000;
	    g.ui.setGameState(state);
	    g.ui.repaint();
	    
	}
	
	private GameState upgmaGameState() {
		
		// GameState only has a default constructor, you must add units to it manually
	    GameState state=new GameState();
	    try {
			state.setMap(new Map(25, 20));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	    // Marines
	    for(int i = 0; i < 8; i++){
	    	
		    Unit u = new Unit(bwapi.getUnitType(UnitTypes.Terran_Marine.ordinal()), Players.Player_One.ordinal(),
		    		new Position((int)(50+Math.random()*350),(int)(50+Math.random()*120)));
		    u._currentHP = (int) (Math.random()*50);
		    try {
				state.addUnit(u);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    // Firebats
	    for(int i = 0; i < 8; i++){
		    Unit u = new Unit(bwapi.getUnitType(UnitTypes.Terran_Firebat.ordinal()), Players.Player_One.ordinal(),
		    		new Position((int)(50+Math.random()*350),(int)(50+Math.random()*120)));
		    u._currentHP = (int) (Math.random()*60);
		    try {
				state.addUnit(u);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    // Medics
	    for(int i = 0; i < 8; i++){
		    Unit u = new Unit(bwapi.getUnitType(UnitTypes.Terran_Medic.ordinal()), Players.Player_One.ordinal(),
		    		new Position((int)(50+Math.random()*350),(int)(50+Math.random()*120)));
		    u._currentHP = (int) (Math.random()*50);
		    try {
				state.addUnit(u);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    return state;
	}

	/***********************
	 ***  REALISTIC TEST ***
	 ***********************
	 * @param p2 
	 * @param p1 
	 */
	private void realisticTest(Player p1, Player p2, int runs) {
		
		// Combat size
		for(int i = 2; i <= 10; i+=2){
			try {
				System.out.println("--- " + ((i*10)+(i*5)+(i)));
				float result = testRealisticGames(p1, p2, i*10, i*5, i, runs);
				System.out.println("REALISTIC TEST RESULT: " + result);
				
				//System.out.println("Result=" + result);
				// TODO:
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	float testRealisticGames(Player p1, Player p2, int marines, int firebats, int tanks, int games) throws Exception{
		
		HashMap<UnitTypes, Integer> unitsA = new HashMap<UnitType.UnitTypes, Integer>();
		unitsA.put(UnitTypes.Terran_Siege_Tank_Tank_Mode, tanks);
		unitsA.put(UnitTypes.Terran_Marine, marines);
		//unitsA.put(UnitTypes.Terran_Firebat, firebats);
		
		HashMap<UnitTypes, Integer> unitsB = new HashMap<UnitType.UnitTypes, Integer>();
		unitsB.put(UnitTypes.Terran_Siege_Tank_Tank_Mode, tanks);
		unitsB.put(UnitTypes.Terran_Marine, marines);
		//unitsB.put(UnitTypes.Terran_Firebat, firebats);
		
		Constants.Max_Units = (marines+firebats+tanks)*2;
		Constants.Max_Moves = Constants.Max_Units + Constants.Num_Directions + 1;
		
		System.out.println("Marines: " + marines + "\tfirebats: " + firebats + "\tTanks: " + tanks + " on each side");
		
		List<Double> results = new ArrayList<Double>();
		int wins = 0;
		for(int i = 1; i <= games; i++){
			double result = testGame(p1, p2, unitsA, unitsB);
			results.add(result);
			if (result>0)
				wins++;
			
			if(i%1==0){
				//System.out.println("Score average: " + average(results) + "\tDeviation: " + deviation(results));
				System.out.println("Win average: " + ((double)wins)/((double)i));
			}
		}
		
		// Calc deviation and average
		System.out.println("--------------- Score average: " + average(results) + "\tDeviation: " + deviation(results));
		System.out.println("--------------- Win average: " + ((double)wins)/((double)games));
		
		return (float)wins / (float)games;
		
	}

	/**********************
	 ***  ONE TYPE TEST ***
	 **********************/
	private void oneTypeTest(Player p1, Player p2, UnitTypes type, int runs) {
		
		for(int i = 1; i < 16; i++){
			try {
				float result = oneTypeGames(p1, p2, i*8, type, runs);
				//System.out.println("MARINE TEST RESULT: " + result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	float oneTypeGames(Player p1, Player p2, int units, UnitTypes type, int games) throws Exception{
		
		HashMap<UnitTypes, Integer> unitsA = new HashMap<UnitType.UnitTypes, Integer>();
		unitsA.put(type, units);
		HashMap<UnitTypes, Integer> unitsB = new HashMap<UnitType.UnitTypes, Integer>();
		unitsB.put(type, units);
		
		Constants.Max_Units = units*2;
		Constants.Max_Moves = Constants.Max_Units + Constants.Num_Directions + 1;
		
		System.out.println("Units: " + units + " of type: " + type + " on each side.");
		
		float score = 0;
		List<Double> results = new ArrayList<Double>();
		int wins = 0;
		for(int i = 0; i < games; i++){
			double result = testGame(p1, p2, unitsA, unitsB);
			results.add(result);
			if (result>0)
				wins++;
			else if (result <0)
				wins--;
			
			System.out.println("Result of game " + i + ": " + result);
			score += result;
		}
		
		// Calc deviation and average
		System.out.println("Score average: " + average(results) + "\tDeviation: " + deviation(results));
		System.out.println("Win average: " + ((double)wins)/((double)games));
		
		return score / games;
		
	}
	
	int testGame(Player p1, Player p2, HashMap<UnitTypes, Integer> unitsA, HashMap<UnitTypes, Integer> unitsB) throws Exception
	{
		
		GameState initialState = gameState(unitsA, unitsB);
		
		p1.setID(0);
		p2.setID(1);
	    
	    // enter a maximum move limit for the game to go on for
	    int moveLimit = 20000;

	    // contruct the game
	    Game g=new Game(initialState, p1, p2, moveLimit, graphics);

	    // play the game
	    /*
	    for(int i = 0; i < 10000000; i++){
	    	if(Math.random()>100)
	    		break;
	    }
	    */
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
	    state.setMap(new Map(25, 20));
	    
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
 	    		int y = startY + space*(i%unitsPerLine) + space;
 	    		try {
 	    			state.addUnit(bwapi.getUnitType(type.ordinal()), Players.Player_One.ordinal(), new Position(x, y));
 	    		} catch (Exception e){
 		 	    	//e.printStackTrace();
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
 	    		int y = startY + space*(i%unitsPerLine) + space;
 	    		try {
 	    			state.addUnit(bwapi.getUnitType(type.ordinal()), Players.Player_Two.ordinal(), new Position(x, y));
 	    		} catch (Exception e){
		 	    	//e.printStackTrace();
		 	    }
 	    	}
	 	    
	    	startXB += space * 2;
	    	
	    }
	 	
	    return state;
	}

	private double deviation(List<Double> times) {
		double average = average(times);
		double sum = 0;
		for(Double d : times){
			sum += (d - average) * (d - average);
		}
		return Math.sqrt(sum/times.size());
	}

	private double average(List<Double> times) {
		double sum = 0;
		for(Double d : times){
			sum+=d;
		}
		return sum/((double)times.size());
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