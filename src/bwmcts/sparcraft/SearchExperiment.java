package bwmcts.sparcraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javabot.JNIBWAPI;
import javabot.types.UnitType;
import javabot.types.UnitType.UnitTypes;

import bwmcts.sparcraft.players.Player;

public class SearchExperiment {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	Player[] players=new Player[2];
    String[] playerStrings=new String[2];
    List<GameState> states;
    Map map;
    boolean  showDisplay;

    String  resultsFile;
    boolean                        appendTimeStamp;
    String                 timeString;
    String                 configFileFull;
    String                 configFileSmall;
    String                 imageDir;

	Player[] resultsPlayers=new Player[2];
	int                        resultsStateNumber;
	int                        resultsNumUnits;
	int                        resultsEval;
	int                        resultsRounds;
	int                        resultsTime;
    int                         numGames;
	int                         numWins;
    int                         numLosses;
	int                         numDraws;

	Random					rand;

	public SearchExperiment(String configFile){
		
		showDisplay=false;
		appendTimeStamp=true;
		rand=new Random();
		configFileSmall = getBaseFilename(configFile);
		map = new Map(40, 22);
		setCurrentDateTime();
		parseConfigFile(configFile);
		writeConfig(configFile);
		setupResults();
	}

	public void setupResults(){
	   /* int np1 = players[0].size();
	    int np2 = players[1].size();
	
	    resultsStateNumber  = ivvv(np1, ivv(np2, iv()));
		resultsNumUnits     = ivvv(np1, ivv(np2, iv()));
		resultsEval         = ivvv(np1, ivv(np2, iv()));
		resultsRounds       = ivvv(np1, ivv(np2, iv()));
		resultsTime         = dvvv(np1, dvv(np2, dv()));
	    numGames            = ivv(np1, iv(np2, 0));
	    numWins             = ivv(np1, iv(np2, 0));
	    numLosses           = ivv(np1, iv(np2, 0));
	    numDraws            = ivv(np1, iv(np2, 0));*/
	}
	
	public void writeConfig(String configfile)
	{/*
	    std::ofstream config(getConfigOutFileName().c_str());
	    if (!config.is_open())
	    {
	        System::FatalError("Problem Opening Output File: Config");
	    }
	
	    std::vector<std::string> lines(getLines(configfile));
	
	    for (size_t l(0); l<lines.size(); ++l)
	    {
	        config << lines[l] << std::endl;
	    }
	
	    config.close();*/
	}
	
	public void writeResultsSummary()
	{/*
		std::ofstream results(getResultsSummaryFileName().c_str());
	    if (!results.is_open())
	    {
	        System::FatalError("Problem Opening Output File: Results Summary");
	    }
	
		for (size_t p1(0); p1 < players[0].size(); ++p1)
		{
	        for (size_t p2(0); p2 < players[1].size(); ++p2)
		    {
	            double score = 0;
	            if (numGames[p1][p2] > 0)
	            {
	                score = ((double)numWins[p1][p2] / (double)(numGames[p1][p2])) + 0.5*((double)numDraws[p1][p2] / (double)numGames[p1][p2]);
	            }
	
	            results << std::setiosflags(std::ios::fixed) << std::setw(12) << std::setprecision(7) << score << " ";
	        }
	
	        results << std::endl;
		}
	
	    results.close();*/
	}
	
	public void padString(String str, int length)
	{
	    while (str.length() < length)
	    {
	        str = str + " ";
	    }
	}
	
	public String getResultsSummaryFileName()
	{
	    String res = resultsFile;
	    
	    if (appendTimeStamp)
	    {
	        res += "_" + getDateTimeString();
	    }
	
	    res += "_results_summary.txt";
	    return res;
	}
	
	public String getResultsOutFileName()
	{
	    String res = resultsFile;
	    
	    if (appendTimeStamp)
	    {
	        res += "_" + getDateTimeString();
	    }
	
	    res += "_results_raw.txt";
	    return res;
	}
	
	public String getConfigOutFileName()
	{
	    String conf = resultsFile;
	    
	    if (appendTimeStamp)
	    {
	        conf += "_" + getDateTimeString();
	    }
	
	    conf += "_config.txt";
	    return conf;
	}
	
	public String getDateTimeString()
	{
	    return timeString;
	}
	
	public void setCurrentDateTime() 
	{/*
	    time_t     now = time(0);
	    struct tm  tstruct;
	    char       buf[80];
	    
	    std::fill(buf, buf+80, '0');
	    tstruct = *localtime(&now);
	    strftime(buf, sizeof(buf), "%Y-%m-%d_%X", &tstruct);
	    //strftime(buf, sizeof(buf), "%X", &tstruct);
	
	    // need to replace ':' for windows filenames
	    for (int i(0); i<80; i++)
	    {
	        if (buf[i] == ':')
	        {
	            buf[i] = '-';
	        }
	    }
	
	    timeString = std::string(buf);*/
		timeString = new java.util.Date(System.currentTimeMillis()).toString();
	}
	
	public List<String> getLines(String filename)
	{/*
	    // set up the file
	    std::ifstream fin(filename.c_str());
	    if (!fin.is_open())
	    {
	         System::FatalError("Problem Opening File: " + filename);
	    }
	
		std::string line;
	
	    std::vector<std::string> lines;
	
	    // each line of the file will be a new player to add
	    while (fin.good())
	    {
	        // get the line and set up the string stream
	        getline(fin, line);
	       
	        // skip blank lines and comments
	        if (line.length() > 1 && line[0] != '#')
	        {
	            lines.push_back(line);
	        }
	    }
	
		fin.close();
	
	    return lines;*/
		return null;
	}
	
	public void parseConfigFile(String filename)
	{/*
	    std::vector<std::string> lines(getLines(filename));
	
	    for (size_t l(0); l<lines.size(); ++l)
	    {
	        std::istringstream iss(lines[l]);
	        std::string option;
	        iss >> option;
	
	        if (strcmp(option.c_str(), "Player") == 0)
	        {
	            addPlayer(lines[l]);
	        }
	        else if (strcmp(option.c_str(), "State") == 0)
	        {
	            addState(lines[l]);
	        }
	        else if (strcmp(option.c_str(), "MapFile") == 0)
	        {
	            std::string fileString;
	            iss >> fileString;
	            map = new Map;
	            map->load(fileString);
	        }
	        else if (strcmp(option.c_str(), "Display") == 0)
	        {
	            std::string option;
	            iss >> option;
	            iss >> imageDir;
	            if (strcmp(option.c_str(), "true") == 0)
	            {
	                showDisplay = true;
	            }
	        }
	        else if (strcmp(option.c_str(), "ResultsFile") == 0)
	        {
	            std::string fileString;
	            std::string append;
	            iss >> fileString;
	            iss >> append;
	            resultsFile = fileString;
	
	            appendTimeStamp = strcmp(append.c_str(), "true") == 0 ? true : false;
	        }
	        else if (strcmp(option.c_str(), "PlayerUpgrade") == 0)
	        {
	            int playerID(0);
	            std::string upgradeName;
	            int upgradeLevel(0);
	
	            iss >> playerID;
	            iss >> upgradeName;
	            iss >> upgradeLevel;
	
	            PlayerProperties::Get(playerID).SetUpgradeLevel(BWAPI::UpgradeTypes::getUpgradeType(upgradeName), upgradeLevel);
	        }
	        else if (strcmp(option.c_str(), "PlayerTech") == 0)
	        {
	            int playerID(0);
	            std::string techName;
	
	            iss >> playerID;
	            iss >> techName;
	
	            PlayerProperties::Get(playerID).SetResearched(BWAPI::TechTypes::getTechType(techName), true);
	        }
	        else
	        {
	            System::FatalError("Invalid Option in Configuration File: " + option);
	        }
	    }*/
	}
	
	public void addState(String line)
	{
	   // std::istringstream iss(line);
	
	    // the first number is the playerID
	    String state;
	    String stateType;
	    int numStates;
	    /*
	    iss >> state;
	    iss >> stateType;
	    iss >> numStates;
	
	    if (strcmp(stateType.c_str(), "StateSymmetric") == 0)
	    { 
	        int xLimit, yLimit;
	        iss >> xLimit;
	        iss >> yLimit;
	
	        std::vector<std::string> unitVec;
	        std::vector<int> numUnitVec;
	        std::string unitType;
	        int numUnits;
	
	        while (iss >> unitType)
	        {
	            iss >> numUnits;
	            unitVec.push_back(unitType);
	            numUnitVec.push_back(numUnits);
	        }
	
	        //std::cout << "\nAdding " << numStates <<  " Symmetric State(s)\n\n";
	
	        for (int s(0); s<numStates; ++s)
	        {
	            states.push_back(getSymmetricState(unitVec, numUnitVec, xLimit, yLimit));
	        }
	    }
	    else if (strcmp(stateType.c_str(), "StateRawDataFile") == 0)
	    {
	        std::string filename;
	        iss >> filename;
	
	        for (int i(0); i<numStates; ++i)
	        {
	            states.push_back(GameState(filename));
	        }
	    }
	    else if (strcmp(stateType.c_str(), "StateDescriptionFile") == 0)
	    {
	        std::string filename;
	        iss >> filename;
	        
	        for (int i(0); i<numStates; ++i)
	        {
	            parseStateDescriptionFile(filename);
	        }
	    }
	    else if (strcmp(stateType.c_str(), "SeparatedState") == 0)
	    {
	        int xLimit, yLimit;
	        int cx1, cy1, cx2, cy2;
	        iss >> xLimit;
	        iss >> yLimit;
	        iss >> cx1;
	        iss >> cy1;
	        iss >> cx2;
	        iss >> cy2;
	
	        std::vector<std::string> unitVec;
	        std::vector<int> numUnitVec;
	        std::string unitType;
	        int numUnits;
	
	        while (iss >> unitType)
	        {
	            iss >> numUnits;
	            unitVec.push_back(unitType);
	            numUnitVec.push_back(numUnits);
	        }
	
	        //std::cout << "\nAdding " << numStates <<  " Symmetric State(s)\n\n";
	
	        for (int s(0); s<numStates/2; ++s)
	        {
	            addSeparatedState(unitVec, numUnitVec, cx1, cy1, cx2, cy2, xLimit, yLimit);
	        }
	    }
	    else
	    {
	        System::FatalError("Invalid State Type in Configuration File: " + stateType);
	    } */ 
	}
	
	public void parseStateDescriptionFile(JNIBWAPI bwapi, String fileName)
	{
	    List<String> lines = getLines(fileName);
	    
	    GameState currentState=new GameState();
	
	    for (int u=0; u<lines.size(); u++)
	    {
	        //Stringstream iss(lines[u]);
	        String unitType="";
	        int playerID=0;
	        int x=0;
	        int y=0;
	
	        //iss >> unitType;
	        //iss >> playerID;
	        //iss >> x;
	        //iss >> y;
	
	        currentState.addUnit(getUnitType(bwapi,unitType), playerID, new Position(x, y));
	    }
	
	    states.add(currentState);
	}
	//BWAPI unittype
	public UnitType getUnitType(JNIBWAPI bwapi,String unitTypeString)
	{
	    UnitType type= bwapi.getUnitType(UnitTypes.valueOf(unitTypeString).ordinal());
	
	    //System::checkSupportedUnitType(type);
	
	    return type;
	}
	
	public void addGameState(GameState state) throws Exception
	{
	    if (states.size() >= 10000)
	    {
	        throw new Exception("Search Experiment cannot contain more than 10,000 states.");
	    }
	}
	
	public void addPlayer(String line)
	{
	    //std::istringstream iss(line);
	
	    // Regular expressions for line validation (if I ever want to use them)
	    //std::regex ScriptRegex("[a-zA-Z]+[ ]+[0-1][ ]+[a-zA-Z]+[ ]*");
	    //std::regex AlphaBetaRegex("[a-zA-Z]+[ ]+[0-1][ ]+[a-zA-Z]+[ ]+[0-9]+[ ]+[0-9]+[ ]+[a-zA-Z]+[ ]+[a-zA-Z]+[ ]+[a-zA-Z]+[ ]+[a-zA-Z]+[ ]+[a-zA-Z]+[ ]+[a-zA-Z]+[ ]*");
	    //std::regex UCTRegex("[a-zA-Z]+[ ]+[0-1][ ]+[a-zA-Z]+[ ]+[0-9]+[ ]+[0-9.]+[ ]+[0-9]+[ ]+[0-9]+[ ]+[a-zA-Z]+[ ]+[a-zA-Z]+[ ]+[a-zA-Z]+[ ]+[a-zA-Z]+[ ]+[a-zA-Z]+[ ]+[a-zA-Z]+[ ]*");
	    //std::regex PortfolioRegex("[a-zA-Z]+[ ]+[0-1][ ]+[a-zA-Z]+[ ]+[0-9]+[ ]+[a-zA-Z]+[ ]+[0-9]+[ ][0-9]+[ ]*");
	
	    // the first number is the playerID
	
	    String player;
	    int playerID;
	    int playerModelID;
	    String playerModelString;
	    
	    //iss >> player;
	    //iss >> playerID;
	    //iss >> playerModelString;
	
/*	    playerStrings[playerID].push_back(playerModelString);
	
	    playerModelID = PlayerModels.getID(playerModelString);
	
	    //std::cout << "Player " << playerID << " adding type " << playerModelString << " (" << playerModelID << ")" << std::endl;
	
	   	if (playerModelID == PlayerModels.AttackClosest)		
	    { 
	        players[playerID].push_back(PlayerPtr(new Player_AttackClosest(playerID))); 
	    }
		else if (playerModelID == PlayerModels.AttackDPS)
	    { 
	        players[playerID].push_back(PlayerPtr(new Player_AttackDPS(playerID))); 
	    }
		else if (playerModelID == PlayerModels.AttackWeakest)		
	    { 
	        players[playerID].push_back(PlayerPtr(new Player_AttackWeakest(playerID))); 
	    }
		else if (playerModelID == PlayerModels.Kiter)				
	    { 
	        players[playerID].push_back(PlayerPtr(new Player_Kiter(playerID))); 
	    }
		else if (playerModelID == PlayerModels.KiterDPS)			
	    { 
	        players[playerID].push_back(PlayerPtr(new Player_KiterDPS(playerID))); 
	    }
	    else if (playerModelID == PlayerModels.Kiter_NOKDPS)			
	    { 
	        players[playerID].push_back(PlayerPtr(new Player_Kiter_NOKDPS(playerID))); 
	    }
	    else if (playerModelID == PlayerModels.Cluster)			
	    { 
	        players[playerID].push_back(PlayerPtr(new Player_Cluster(playerID))); 
	    }
		else if (playerModelID == PlayerModels.NOKDPS)	
	    { 
	        players[playerID].push_back(PlayerPtr(new Player_NOKDPS(playerID))); 
	    }
		else if (playerModelID == PlayerModels.Random)				
	    { 
	        players[playerID].push_back(PlayerPtr(new Player_Random(playerID))); 
	    }
	    else if (playerModelID == PlayerModels.PortfolioGreedySearch)				
	    { 
	        String enemyPlayerModel;
	        int timeLimit=0;
	        int iterations=1;
	        int responses=0;
	
	        //iss >> timeLimit;
	        //iss >> enemyPlayerModel;
	        //iss >> iterations;
	        //iss >> responses;
	
	        players[playerID].push_back(PlayerPtr(new Player_PortfolioGreedySearch(playerID, PlayerModels::getID(enemyPlayerModel), iterations, responses, timeLimit))); 
	    }
	    else if (playerModelID == PlayerModels.AlphaBeta)
	    {
	        int             timeLimitMS;
	        int             maxChildren;
	        String     moveOrdering;
	        String     evalMethod;
	        String     playoutScript1;
	        String     playoutScript2;
	        String     playerToMoveMethod;
	        String     opponentModelScript;
	
	        // read in the values
	        //iss >> timeLimitMS;
	        //iss >> maxChildren;
	        //iss >> moveOrdering;
	        //iss >> evalMethod;
	        //iss >> playoutScript1;
	        //iss >> playoutScript2;
	        //iss >> playerToMoveMethod;
	        //iss >> opponentModelScript;
	
	        // convert them to the proper enum types
	        //int moveOrderingID      = MoveOrderMethod::getID(moveOrdering);
	        //int evalMethodID        = EvaluationMethods::getID(evalMethod);
	        //int playoutScriptID1    = PlayerModels::getID(playoutScript1);
	        //int playoutScriptID2    = PlayerModels::getID(playoutScript2);
	        //int playerToMoveID      = PlayerToMove::getID(playerToMoveMethod);
	        //int opponentModelID     = PlayerModels::getID(opponentModelScript);
	
	        // construct the parameter object
	        AlphaBetaSearchParameters params;
	
	        // give the default parameters we can't set via options
		    params.setMaxDepth(50);
	        params.setSearchMethod(SearchMethods::IDAlphaBeta);
	
	        // set the parameters from the options in the file
		    params.setMaxPlayer(playerID);
		    params.setTimeLimit(timeLimitMS);
	        params.setMaxChildren(maxChildren);
	        params.setMoveOrderingMethod(moveOrderingID);
	        params.setEvalMethod(evalMethodID);
	        params.setSimScripts(playoutScriptID1, playoutScriptID2);
	        params.setPlayerToMoveMethod(playerToMoveID);
		
	        // add scripts for move ordering
	        if (moveOrderingID == MoveOrderMethod::ScriptFirst)
	        {
	            params.addOrderedMoveScript(PlayerModels::NOKDPS);
	            params.addOrderedMoveScript(PlayerModels::KiterDPS);
	            //params.addOrderedMoveScript(PlayerModels::Cluster);
	            //params.addOrderedMoveScript(PlayerModels::AttackWeakest);
	        }
	
	        // set opponent modeling if it's not none
	        if (opponentModelID != PlayerModels::None)
	        {
	            if (playerID == 0)
	            {
	                params.setSimScripts(playoutScriptID1, opponentModelID);
	                params.setPlayerModel(1, playoutScriptID2);
	            }
	            else
	            {
	                params.setSimScripts(opponentModelID, playoutScriptID2);
	                params.setPlayerModel(0, playoutScriptID1);
	            }
	        }
	
	        PlayerPtr abPlayer(new Player_AlphaBeta(playerID, params, TTPtr((TranspositionTable *)NULL)));
	        players[playerID].push_back(abPlayer); 
	    }
	    else if (playerModelID == PlayerModels.UCT)
	    {
	        int             timeLimitMS;
	        double          cValue;
	        int             maxTraversals;
	        int             maxChildren;
	        std::string     moveOrdering;
	        std::string     evalMethod;
	        std::string     playoutScript1;
	        std::string     playoutScript2;
	        std::string     playerToMoveMethod;
	        std::string     opponentModelScript;
	
	        // read in the values
	        iss >> timeLimitMS;
	        iss >> cValue;
	        iss >> maxTraversals;
	        iss >> maxChildren;
	        iss >> moveOrdering;
	        iss >> evalMethod;
	        iss >> playoutScript1;
	        iss >> playoutScript2;
	        iss >> playerToMoveMethod;
	        iss >> opponentModelScript;
	
	        // convert them to the proper enum types
	        int moveOrderingID      = MoveOrderMethod::getID(moveOrdering);
	        int evalMethodID        = EvaluationMethods::getID(evalMethod);
	        int playoutScriptID1    = PlayerModels::getID(playoutScript1);
	        int playoutScriptID2    = PlayerModels::getID(playoutScript2);
	        int playerToMoveID      = PlayerToMove::getID(playerToMoveMethod);
	        int opponentModelID     = PlayerModels::getID(opponentModelScript);
	
	        // construct the parameter object
	        UCTSearchParameters params;
	
	        // set the parameters from the options in the file
		    params.setTimeLimit(timeLimitMS);
	        params.setCValue(cValue);
		    params.setMaxPlayer(playerID);
	        params.setMaxTraversals(maxTraversals);
	        params.setMaxChildren(maxChildren);
	        params.setMoveOrderingMethod(moveOrderingID);
	        params.setEvalMethod(evalMethodID);
	        params.setSimScripts(playoutScriptID1, playoutScriptID2);
	        params.setPlayerToMoveMethod(playerToMoveID);
	        //params.setGraphVizFilename("__uct.txt");
	
	        // add scripts for move ordering
	        if (moveOrderingID == MoveOrderMethod::ScriptFirst)
	        {
	            params.addOrderedMoveScript(PlayerModels::NOKDPS);
	            params.addOrderedMoveScript(PlayerModels::KiterDPS);
	            //params.addOrderedMoveScript(PlayerModels::Cluster);
	        }
		
	        // set opponent modeling if it's not none
	        if (opponentModelID != PlayerModels::None)
	        {
	            if (playerID == 0)
	            {
	                params.setSimScripts(playoutScriptID1, opponentModelID);
	                params.setPlayerModel(1, playoutScriptID2);
	            }
	            else
	            {
	                params.setSimScripts(opponentModelID, playoutScriptID2);
	                params.setPlayerModel(0, playoutScriptID1);
	            }
	        }
	
	        PlayerPtr uctPlayer(new Player_UCT(playerID, params));
	        players[playerID].push_back(uctPlayer); 
	    }
		else
	    {
	        System::FatalError("Invalid Player Type in Configuration File: " + playerModelString);
	    }*/
	}
	
	public Position getRandomPosition(int xlimit, int ylimit)
	{
		int x = xlimit - (rand.nextInt() % (2*xlimit));
		int y = ylimit - (rand.nextInt() % (2*ylimit));
	
		return new Position(x, y);
	}
	
	public GameState getSymmetricState(JNIBWAPI bwapi, String[] unitTypes, int[] numUnits, int xLimit, int yLimit)
	{
		GameState state=new GameState();
	
	    Position mid= new Position(640, 360);
	
	    //std::cout << "   Adding";
	
	    // for each unit type to add
	    for (int i=0; i<unitTypes.length; i++)
	    {
	        UnitType type = bwapi.getUnitType(UnitTypes.valueOf(unitTypes[i]).ordinal());
	
	        // add the symmetric unit for each count in the numUnits Vector
	        for (int u=0; u<numUnits[i]; u++)
		    {
	            Position r= new Position((rand.nextInt() % (2*xLimit)) - xLimit, (rand.nextInt() % (2*yLimit)) - yLimit);
	            Position u1= new Position(mid.getX() + r.getX(), mid.getY() + r.getY());
	            Position u2= new Position(mid.getX() - r.getX(), mid.getY() - r.getY());
	
	            state.addUnit(type, Players.Player_One.ordinal(), u1);
	            state.addUnit(type, Players.Player_Two.ordinal(), u2);
		    }
	    }
	    
	    //std::cout << std::endl;
		state.finishedMoving();
		return state;
	}
	
	public void addSeparatedState(JNIBWAPI bwapi,  String[] unitTypes, int[] numUnits,
	                                                int cx1, int cy1, 
	                                                int cx2, int cy2,
									                int xLimit, int yLimit)
	{
		GameState state=new GameState();
	    GameState state2=new GameState();
	
	    // for each unit type to add
	    for (int i=0; i<unitTypes.length; i++)
	    {
	        UnitType type = bwapi.getUnitType(UnitTypes.valueOf(unitTypes[i]).ordinal());
	
	        // add the symmetric unit for each count in the numUnits Vector
	        for (int u=0; u<numUnits[i]; u++)
		    {
	            Position r=new Position((rand.nextInt() % (2*xLimit)) - xLimit, (rand.nextInt() % (2*yLimit)) - yLimit);
	            Position u1=new Position(cx1 + r.getX(), cy1 + r.getY());
	            Position u2=new Position(cx2 - r.getX(), cy2 - r.getY());
	
	            state.addUnit(type, Players.Player_One.ordinal(), u1);
	            state.addUnit(type, Players.Player_Two.ordinal(), u2);
	            state2.addUnit(type, Players.Player_One.ordinal(), u2);
	            state2.addUnit(type, Players.Player_Two.ordinal(), u1);
		    }
	    }
	    
		state.finishedMoving();
	
		states.add(state);
	    states.add(state2);
	}
	
	public String[][] getExpDescription(int p1Ind, int p2Ind, int state)
	{/*
	    // 2-column description vector
	    svv desc(2, sv());
	
	    std::stringstream ss;
	
	    desc[0].push_back("Player 1:");
	    desc[0].push_back("Player 2:");
	    desc[0].push_back("State #:");
	    desc[0].push_back("Units:");
	
	    for (size_t p1(0); p1 < players[0].size(); ++p1)
		{
	        std::stringstream ss;
	        ss << "P1 " << p1 << ":";
	        desc[0].push_back(ss.str());
	    }
	
	    for (size_t p2(0); p2 < players[1].size(); ++p2)
		{
	        std::stringstream ss;
	        ss << "P2 " << p2 << ":";
	        desc[0].push_back(ss.str());
	    }
	
	    for (size_t p1(0); p1 < players[0].size(); ++p1)
		{
	        for (size_t p2(0); p2 < players[1].size(); ++p2)
		    {
	            std::stringstream ps;    
	            ps << p1 << " vs " << p2;
	            desc[0].push_back(ps.str());
	        }
	    }
	
	    ss << PlayerModels::getName(players[0][p1Ind]->getType());        desc[1].push_back(ss.str()); ss.str(std::string());
	    ss << PlayerModels::getName(players[1][p2Ind]->getType());        desc[1].push_back(ss.str()); ss.str(std::string());
	    ss << state << " of " << states.size();                         desc[1].push_back(ss.str()); ss.str(std::string());
	    ss << states[state].numUnits(0);                                desc[1].push_back(ss.str()); ss.str(std::string());
	
	    for (size_t p1(0); p1 < players[0].size(); ++p1)
		{
	        desc[1].push_back(PlayerModels::getName(players[0][p1]->getType()));
	    }
	
	    for (size_t p2(0); p2 < players[1].size(); ++p2)
		{
	        desc[1].push_back(PlayerModels::getName(players[1][p2]->getType()));
	    }
	
	    char buf[30];
		for (size_t p1(0); p1 < players[0].size(); ++p1)
		{
	        for (size_t p2(0); p2 < players[1].size(); ++p2)
		    {
	            double score = 0;
	            if (numGames[p1][p2] > 0)
	            {
	                score = ((double)numWins[p1][p2] / (double)(numGames[p1][p2])) + 0.5*((double)numDraws[p1][p2] / (double)numGames[p1][p2]);
	            }
	
	            sprintf(buf, "%.7lf", score);
			    desc[1].push_back(std::string(buf));
	        }
		}
	
	    return desc;*/
	    return null;
	}
	
	public String getBaseFilename(String filename)
	{
	    for (int i=filename.length()-1; i>=0; i--)
	    {
	        if (filename.charAt(i) == '/' || filename.charAt(i) == '\\')
	        {
	            return filename.substring(i+1,filename.length());
	        }
	    }
	
	    return filename;
	}
	
	public void runExperiment()
	{/*
	    std::ofstream results(getResultsOutFileName().c_str());
	    if (!results.is_open())
	    {
	        System::FatalError("Problem Opening Output File: Results Raw");
	    }
	    
	    // set the map file for all states
	    for (size_t state(0); state < states.size(); ++state)
		{
	        states[state].setMap(map);
	    }
	
		#ifdef USING_VISUALIZATION_LIBRARIES
			Display * disp = NULL;
	        if (showDisplay)
	        {
	            disp = new Display(map ? map->getBuildTileWidth() : 40, map ? map->getBuildTileHeight() : 22);
	            disp->SetImageDir(imageDir);
	            disp->OnStart();
			    disp->LoadMapTexture(map, 19);
	        }
		#endif
	
		results << "   P1    P2    ST  UNIT       EVAL    RND           MS | UnitType PlayerID CurrentHP XPos YPos\n";
	    
		// for each player one player
		for (size_t p1Player(0); p1Player < players[0].size(); p1Player++)
		{
			// for each player two player
			for (size_t p2Player(0); p2Player < players[1].size(); p2Player++)
			{
				// for each state we care about
				for (size_t state(0); state < states.size(); ++state)
				{
	                char buf[255];
	                fprintf(stderr, "%s  ", configFileSmall.c_str());
					fprintf(stderr, "%5d %5d %5d %5d", (int)p1Player, (int)p2Player, (int)state, (int)states[state].numUnits(Players::Player_One));
					sprintf(buf, "%5d %5d %5d %5d", (int)p1Player, (int)p2Player, (int)state, (int)states[state].numUnits(Players::Player_One));
	                results << buf;
	
					resultsPlayers[0].push_back(p1Player);
					resultsPlayers[1].push_back(p2Player);
					resultsStateNumber[p1Player][p2Player].push_back(state);
					resultsNumUnits[p1Player][p2Player].push_back(states[state].numUnits(Players::Player_One));
					
					// get player one
					PlayerPtr playerOne(players[0][p1Player]);
	
					// give it a new transposition table if it's an alpha beta player
					Player_AlphaBeta * p1AB = dynamic_cast<Player_AlphaBeta *>(playerOne.get());
					if (p1AB)
					{
						p1AB->setTranspositionTable(TTPtr(new TranspositionTable()));
					}
	
					// get player two
					PlayerPtr playerTwo(players[1][p2Player]);
					Player_AlphaBeta * p2AB = dynamic_cast<Player_AlphaBeta *>(playerTwo.get());
					if (p2AB)
					{
						p2AB->setTranspositionTable(TTPtr(new TranspositionTable()));
					}
	
					// construct the game
					Game g(states[state], playerOne, playerTwo, 20000);
					#ifdef USING_VISUALIZATION_LIBRARIES
	                    if (showDisplay)
	                    {
						    g.disp = disp;
	                        disp->SetExpDesc(getExpDescription(p1Player, p2Player, state));
	                    }
					#endif
	
					// play the game to the end
					g.play();
					
					ScoreType gameEval = g.getState().eval(Players::Player_One, SparCraft::EvaluationMethods::LTD2).val();
	
	                numGames[p1Player][p2Player]++;
	                if (gameEval > 0)
	                {
	                    numWins[p1Player][p2Player]++;
	                }
	                else if (gameEval < 0)
	                {
	                    numLosses[p1Player][p2Player]++;
	                }
	                else if (gameEval == 0)
	                {
	                    numDraws[p1Player][p2Player]++;
	                }
	
					double ms = g.getTime();
					sprintf(buf, " %10d %6d %12.2lf", gameEval, g.getRounds(), ms);
					fprintf(stderr, "%12d %12.2lf\n", gameEval, ms);
	
					resultsEval[p1Player][p2Player].push_back(gameEval);
					resultsRounds[p1Player][p2Player].push_back(g.getRounds());
					resultsTime[p1Player][p2Player].push_back(ms);
	
	                results << buf;
	                printStateUnits(results, g.getState());
	                results << std::endl;
	                
	                writeResultsSummary();
				}
			}
		}
	    
	    results.close();
	    */
	}
	
	
	public void printStateUnits(String results, GameState state)
	{
	 /*   stringstream ss;
	    for (int p=0; p<Constants.Num_Players; p++)
	    {
	        for (int u=0; u<state.numUnits(p); u++)
	        {
	            Unit unit=state.getUnit(p,u);
	            Position pos= unit.currentPosition(state.getTime());
	                        
	            //ss << " | " << unit.name() << " " << (int)unit.player() << " " << unit.currentHP() << " " << pos.x() << " " << pos.y();
	        }
	    }
	    //results << ss.str();*/
	}
}
