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
import bwmcts.sparcraft.*;
import bwmcts.sparcraft.players.Player;
import bwmcts.sparcraft.players.Player_AttackClosest;
import bwmcts.sparcraft.players.Player_Defense;
import bwmcts.sparcraft.players.Player_Kite;
import bwmcts.sparcraft.players.Player_NoOverKillAttackValue;

public class Test implements BWAPIEventListener  {
	
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
		
		//Player p2 = new Player_Kite(1);
		
		//Player p1 = new UctcdLogic(bwapi, new GUCTCD(1.6, 20, 1, 0, 500, false));
		Player p1 = new Player_NoOverKillAttackValue(0);		
		Player p2 = new Player_NoOverKillAttackValue(1);
		//Player p2 = new UctcdLogic(bwapi, new GUCTCD(1.6, 2, 0, 1, 20, false));
		//Player p2 = new GPortfolioGreedyLogic(bwapi, 2, 2, 30, 6);
		
		//marineTest(p1, p2);
		
		//zerglingTest(p1, p2);
		
		//realisticTest(p1, p2);
		
		//upgmaTest(p1, p2, 10, 6);
		
		simulatorTest(p1, p2, 1, 250, 10, 10);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				// Runs
				for(int r = 0; r < runs; r++){
					int limit = (int)(min + (float)(max-min)*(float)((float)s/(float)steps));
					double time = runSimulator(p1, p2, i, limit);
					
					if (time == -1)
						break;
					
					times.add(time);
				}
				
				// Calc deviation and average
				System.out.println("Average: " + average(times) + "\tDeviation: " + deviation(times));
				
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
	    	
	    System.out.println("Units: " + i*7*2 + "\tMoveLimit: " + moveLimit + "\tTime: " + time + " ms.");

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
		
		for(int i = 1; i < 20; i+=Math.max(1, i/4)){
			
			List<Double> times = new ArrayList<Double>();
			for (int r = 0; r < runs; r++){
				double time = runUpgma(p1, p2, i, numClusters);
				times.add(time);
			}
			
			// Calc deviation and average
			System.out.println("Average: " + average(times) + "\tDeviation: " + deviation(times));
			
		}
		
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
	    System.out.println("\nMarines: " + (i*4) + "\tFirebats: " + (i*2) + "\tTanks: " + i + "\tTime: " + time + " ms.");
	    
	    for(Integer c : clusters.keySet()){
			
	    	float distance = Util.avgDistance(clusters.get(c));
			
			System.out.print("Cluster " + c + ": {" + distance + "}[");
			
			int n = 0;
			for(Unit u : clusters.get(c)){
				if (n != 0)
					System.out.print(", ");
				System.out.print("(" + u.type().getName() + ")");
				n++;
			}
			
			System.out.println("] ");
			
		}
	    
	    return time;
		
	}

	/***********************
	 ***  REALISTIC TEST ***
	 ***********************
	 * @param p2 
	 * @param p1 
	 */
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
 	    		int y = startY + space*(i%unitsPerLine);
 	    		try {
 	    			state.addUnit(bwapi.getUnitType(type.ordinal()), Players.Player_Two.ordinal(), new Position(x, y));
	 	    	} catch (Exception e){
		 	    	//e.printStackTrace();
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