package bwmcts.sparcraft;

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
import bwmcts.mcts.uctcd.UCTCD;
import bwmcts.mcts.uctcd.UCTCDsingle;
import bwmcts.sparcraft.players.Player;
import bwmcts.sparcraft.players.Player_AttackClosest;
import bwmcts.sparcraft.players.Player_Defense;
import bwmcts.sparcraft.players.Player_Kite;
import bwmcts.sparcraft.players.Player_NoOverKillAttackValue;

public class TutorialCode implements BWAPIEventListener  {
	
	JNIBWAPI bwapi;
	public Unit getSampleUnit()
	{
	    // Unit has several constructors
	    // You will typically only be using this one to construct a 'starting' unit

	    // Unit(const BWAPI::UnitType unitType, const IDType & playerID, const Position & pos)

	    // The BWAPI::UnitType of the unit to be added
	    UnitType marine = bwapi.getUnitType(UnitTypes.Terran_Marine.ordinal());

	    // The player to add this unit to, specified by an IDType
	    int player = Players.Player_One.ordinal();

	    // A Position, measured in Pixel coordinates
	    Position p =new Position(5,5);

	    // Simple unit constructor
	    Unit marineAtOrigin= new Unit(marine, player, p);
	    System.out.println("Unit created" );
	    return marineAtOrigin;
	}

	public GameState AvsBState(int numA, UnitTypes typeA, int numB, UnitTypes typeB) throws Exception
	{
	    // GameState only has a default constructor, you must add units to it manually
	    GameState state=new GameState();
	    state._maxUnits=50;
	    state.setMap(new Map(40, 40));
	    int startY = 5;
	    int space = 10;
	    
	    // Or it can be added to the state via unit construction parameters
	    try {
	    state.addUnit(bwapi.getUnitType(typeB.ordinal()), Players.Player_Two.ordinal(),new Position(200,startY + space));
	    
	    } catch (Exception e){}
	    
	    try {
	    	for(int i = 0; i < numA; i++){
	    		//if(Math.random()<=0.6)
	    			state.addUnit(bwapi.getUnitType(typeA.ordinal()), Players.Player_One.ordinal(), new Position(500,startY + space*i));
	    		//else if(Math.random()<=0.9)
	    		//	state.addUnit(bwapi.getUnitType(UnitTypes.Terran_Firebat.ordinal()), Players.Player_One.ordinal(), new Position(500,startY + space*i));
	    		//else
	    		//	state.addUnit(bwapi.getUnitType(UnitTypes.Terran_Siege_Tank_Tank_Mode.ordinal()), Players.Player_One.ordinal(), new Position(500,startY + space*i));
	    	}
	    
	    } catch (Exception e){}
	    
	    for(int i = 1; i < numB; i++){
    		//if(Math.random()<=0.6)
    			state.addUnit(bwapi.getUnitType(typeB.ordinal()), Players.Player_Two.ordinal(),new Position(200,startY + space + space * i));
    		//else if(Math.random()<=0.9)
    		//	state.addUnit(bwapi.getUnitType(UnitTypes.Terran_Firebat.ordinal()), Players.Player_Two.ordinal(),new Position(200,startY + space + space * i));
    		//else
    		//	state.addUnit(bwapi.getUnitType(UnitTypes.Terran_Siege_Tank_Tank_Mode.ordinal()), Players.Player_Two.ordinal(),new Position(200,startY + space + space * i));	
	    }
	    
	    return state;
	}
	
	private GameState AvsBState(HashMap<UnitTypes, Integer> a,
			HashMap<UnitTypes, Integer> b) throws Exception {
		
		// GameState only has a default constructor, you must add units to it manually
	    GameState state=new GameState();
	    state._maxUnits=50;
	    state.setMap(new Map(40, 40));
	    int startY = 5;
	    int space = 10;
	    
	    for(UnitTypes type : b.keySet()){
	    	
	    	try {
	    	    state.addUnit(bwapi.getUnitType(type.ordinal()), Players.Player_Two.ordinal(),new Position(200,startY + space));
	    	} catch (Exception e){}
	    	
	    	try {
	 	    	for(int i = 1; i < b.get(type); i++){
	 	    		state.addUnit(bwapi.getUnitType(type.ordinal()), Players.Player_Two.ordinal(), new Position(200,startY + space*i));
	 	    	}
	 	    } catch (Exception e){
	 	    	System.out.println(e);
	 	    }
	    }
	    
	 	for(UnitTypes type : a.keySet()){
	    	
	    	try {
	 	    	for(int i = 0; i < a.get(type); i++){
	 	    		state.addUnit(bwapi.getUnitType(type.ordinal()), Players.Player_One.ordinal(), new Position(500,startY + space*i));
	 	    	}
	 	    
	 	    } catch (Exception e){}
	    }
	 	
	    return state;
	}

	public Map getSampleMap() throws Exception
	{
	    // Maps are used to constrain the movement of Units on a battlefield

	    // There are 3 resolution scales at which positions operate in StarCraft maps
	    // Pixel Resolution = 1x1 pixel = StarCraft smallest movement resolution
	    // WalkTile Resolution = 8x8 pixels = StarCraft 'walkable' resolution
	    // BuildTile Resolution = 32x32 pixels, or 4x4 WalkTiles = StarCraft "map size" resolution

	    // Example: A Map of size 32*32 BuildTiles has size 128*128 WalkTiles or 1024*1024 pixels
	    
	    // The Map object constructor takes in size coordinates in BWAPI BuildTile resolution    
	    Map smallMap=new Map(100, 100);

	    // We can set the walkable values of WalkTile resolution via
	    // void setMapData(const size_t & buildTileX, const size_t & buildTileY, const bool val)
	    smallMap.setMapData(21, 98, false);

	    // The default map sets all tiles to walkable, with an upper-left boundary of (0,0) and a lower-right boundary of (x,y)
	    // We can query whether or not a unit can walk at a given position 
	    boolean canWalkHere = smallMap.isWalkable(new Position(100, 30));

	    // You can also construct a Map from a BWAPI::Game object, if you are using this code from within a bot
	    // Map gameMap(BWAPI::BroodWar)

	    // Once constructed, maps can be saved or loaded to files
	    // A sample map (Destination) is provided under the sample_experiment directory
	    // smallMap.load("mapname.txt");

	    // We can set the Map of a GameState via a pointer to the map, as Map objects can be quite large:
	    GameState state=getSampleState();
	    state.setMap(smallMap);

	    return smallMap;
	}

	// When dealing with players, use a shared pointer, it's safer
	// PlayerPtr is a boost::shared_pointer wrapper for Player *

	public Player getSamplePlayer(int playerID)
	{
	    // Player is the base class for all Player objects
	    //
	    // Scripted players all have the same constructor which is just the player ID which will be using this script
	    // It is imoprtant to set that player ID correctly, as that player will only be generating and returning moves for that player

		Player attackClosest=new Player_NoOverKillAttackValue(playerID);
		System.out.println("Player created");
	    return attackClosest;
	}

	public List<UnitAction> getSamplePlayerActionsFromState() throws Exception
	{
	    // get our sample player
	    int currentPlayerID = Players.Player_One.ordinal();
	    Player myPlayer = getSamplePlayer(currentPlayerID);

	    // Construct a blank vector of UnitActions, which are individual unit moves
	    List<UnitAction> move=new ArrayList<UnitAction>();

	    // Get a state
	    GameState state = getSampleState();

	    // Construct a MoveArray. This structure will hold all the legal moves for each unit possible for this state
	    HashMap<Integer, List<UnitAction>>moveArray=new HashMap<Integer,List<UnitAction>>();

	    // Generate the moves possible by currentPlayer from state into moveArray
	    try {
			state.generateMoves(moveArray, currentPlayerID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    // Call getMoves with these arguments
	    myPlayer.getMoves(state, moveArray, move);
	    
	    return move;
	}

	private GameState getSampleState() {
		// TODO Auto-generated method stub
		return null;
	}

	void runSampleGame() throws Exception
	{
	    // running a game is quite simple, you just need 2 players and an initial state
	    //GameState initialState = AvsBState(16, UnitTypes.Protoss_Dragoon, 16, UnitTypes.Protoss_Dragoon);
		//GameState initialState = AvsBState(20, UnitTypes.Terran_Marine, 20, UnitTypes.Terran_Marine);
		/*
		HashMap<UnitTypes, Integer> A = new HashMap<UnitType.UnitTypes, Integer>();
		A.put(UnitTypes.Terran_Firebat, 10);
		A.put(UnitTypes.Terran_Marine, 10);
		HashMap<UnitTypes, Integer> B = new HashMap<UnitType.UnitTypes, Integer>();
		B.put(UnitTypes.Protoss_Zealot, 5);
		B.put(UnitTypes.Protoss_Dragoon, 5);
		*/
		
		GameState initialState = AvsBState(20, UnitTypes.Terran_Marine, 20, UnitTypes.Terran_Marine);
		
	    // get the players
	    Player p1 = new Player_NoOverKillAttackValue(Players.Player_One.ordinal());
	    //Player p2 = new Player_NoOverKillAttackValue(Players.Player_Two.ordinal());
	    //Player p2 = new Player_Kite(Players.Player_Two.ordinal());
	    //Player p2 = new Player_Kite(Players.Player_Two.ordinal());
	    //Player p2 = new Player_Kite(Players.Player_Two.ordinal());
	    //Player p2 = getSamplePlayer(Players.Player_Two.ordinal());
	    //Player p2 = new UctcdLogic(bwapi, new GUCTCD(1.6,20,0,1,500,true));
	    
	    //Player p1 = new IuctcdLogic(bwapi, new IUCTCD(1.6,20,1,0,5000,false));
	    //p1.setID(0);
	    
	    //Player p2 = new IuctcdLogic(bwapi, new IUCTCD(1.6,20,0,1,5000,false));
	    Player p2 = new GuctcdLogic(bwapi, new GUCTCD(1.6,20,0,1,500,false));
	    //Player p2 = new GPortfolioGreedyLogic(bwapi,2,2,30,6);
	    //Player p2 = new GuctcdLogic(bwapi, new GUCTCD(1.6,20,0,1,200,false));
	    p2.setID(1);
	    
	    // enter a maximum move limit for the game to go on for
	    int moveLimit = 1000;

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
	    if (score._val > 0)
	    {
	        System.out.println( "Player One Wins!\n");
	    }
	    else if (score._val < 0)
	    {
	    	 System.out.println( "Player Two Wins!\n");
	    }
	    else
	    {
	    	 System.out.println( "Game is a draw!\n");
	    }
	}
	
	

	public static void main(String[] args) throws Exception{
		System.out.println("Create TC instance");
		TutorialCode tc=new TutorialCode();
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
		//System.out.println(getSamplePlayerActionsFromState());
			runSampleGame();
			/*
			runSampleGame();
			runSampleGame();
			runSampleGame();
			runSampleGame();
			runSampleGame();
			runSampleGame();
			runSampleGame();
			runSampleGame();
			runSampleGame();
			*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
