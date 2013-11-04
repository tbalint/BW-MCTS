package bwmcts.sparcraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javabot.BWAPIEventListener;
import javabot.JNIBWAPI;
import javabot.types.UnitType;
import javabot.types.UnitType.UnitTypes;
import bwmcts.simulator.Position;
import bwmcts.sparcraft.players.Player;
import bwmcts.sparcraft.players.Player_AttackClosest;

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

	public GameState getSampleState() throws Exception
	{
	    // GameState only has a default constructor, you must add units to it manually
	    GameState state=new GameState();
	    state._maxUnits=50;
	    state.setMap(new Map(50, 50));
	    // The recommended way of adding a unit to a state is to just construct a unit and add it with:
	    try {
	    state.addUnit(getSampleUnit());
	    } catch (Exception e){}
	    // Or it can be added to the state via unit construction parameters
	    try {
	    state.addUnit(bwapi.getUnitType(UnitTypes.Terran_Marine.ordinal()), Players.Player_One.ordinal(),new Position(10,10));
	    } catch (Exception e){}
	    try {
	    state.addUnit(bwapi.getUnitType(UnitTypes.Terran_Marine.ordinal()), Players.Player_Two.ordinal(), new Position(300,300));
	    state.addUnit(bwapi.getUnitType(UnitTypes.Terran_Marine.ordinal()), Players.Player_Two.ordinal(), new Position(305,305));
	    state.addUnit(bwapi.getUnitType(UnitTypes.Terran_Marine.ordinal()), Players.Player_Two.ordinal(), new Position(315,315));
	    state.addUnit(bwapi.getUnitType(UnitTypes.Terran_Marine.ordinal()), Players.Player_Two.ordinal(), new Position(310,310));
	    } catch (Exception e){}
	    // Units added with those 2 functions will be given a unique unitID inside GameState
	    // If you require setting your own unique unitID for a unit, for example when translating a BWAPI::Broodwar state to GameState

	    // Construct the unit
	    Unit u=new Unit(bwapi.getUnitType(UnitTypes.Terran_Marine.ordinal()), Players.Player_One.ordinal(), new Position(0,0));

	    // Set the unitID
	    u.setUnitID(5);

	    // Add it to the state and tell it not to change the unitID.
	    // If a state contains two units with the same ID, an error will occur
	    state.addUnitWithID(u);

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

		Player attackClosest=new Player_AttackClosest(playerID);
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

	void runSampleGame() throws Exception
	{
	    // running a game is quite simple, you just need 2 players and an initial state
	    GameState initialState = getSampleState();

	    // get the players
	    Player p1 = getSamplePlayer(Players.Player_One.ordinal());
	    Player p2 = getSamplePlayer(Players.Player_Two.ordinal());

	    // enter a maximum move limit for the game to go on for
	    int moveLimit = 1000;

	    // contruct the game
	    Game g=new Game(initialState, p1, p2, moveLimit,true);

	    // play the game
	    g.play();

	    // you can access the resulting game state after g has been played via getState
	    GameState finalState = g.getState();
	    System.out.println(finalState.playerDead(0) + "  "+finalState.playerDead(1));
	    // you can now evaluate the state however you wish. let's use an LTD2 evaluation from the point of view of player one
	    int score = finalState.eval(Players.Player_One.ordinal(), 0,0,0);

	    // StateEvalScore has two components, a numerical score and a number of Movement actions performed by each player
	    // with this evaluation, positive val means win, negative means loss, 0 means tie
	    if (score > 0)
	    {
	        System.out.println( "Player One Wins!\n");
	    }
	    else if (score < 0)
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
