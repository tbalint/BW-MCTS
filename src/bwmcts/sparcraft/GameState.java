package bwmcts.sparcraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javabot.JNIBWAPI;
import javabot.types.UnitType;



public class GameState {

	 Map   _map;               

	 //Array2D<Unit, Constants::Num_Players, Constants::Max_Units>     _units;             
	 Unit[][] _units=new Unit[Constants.Num_Players][Constants.Max_Moves];
	 //Array2D<int, Constants::Num_Players, Constants::Max_Units>      _unitIndex;        
	 int[][] _unitIndex=new int[Constants.Num_Players][Constants.Max_Moves];
	 List<Unit>                                                  _neutralUnits;

	 int[]  _numUnits=new int[Constants.Num_Players];
	 int[]  _prevNumUnits=new int[Constants.Num_Players];

	 float[]  _totalLTD=new float[Constants.Num_Players];
	 float[] _totalSumSQRT=new float[Constants.Num_Players];

	 int[]                   _numMovements=new int[Constants.Num_Players];
	 int[]  _prevHPSum=new int[Constants.Num_Players];
		
	    int                                                        _currentTime;
	    int                                                          _maxUnits;
	    int                                                        _sameHPFrames;

	    // checks to see if the unit array is full before adding a unit to the state
	    private boolean   checkFull(int player){return true;}
	    private boolean checkUniqueUnitIDs(){return true;}

	    private void  performUnitAction(UnitAction move){
	    	Unit ourUnit		= getUnit(move._player, move._unit);
	    	int player		= ourUnit.player();
	    	int enemyPlayer  = getEnemy(player);

	    	if (move._moveType == UnitActionTypes.ATTACK)
	    	{
	    		Unit enemyUnit=getUnit(enemyPlayer,move._moveIndex);
	            //Unit & enemyUnit(getUnitByID(enemyPlayer ,move._moveIndex));
	    			
	    		// attack the unit
	    		ourUnit.attack(move, enemyUnit, _currentTime);
	    			
	    		// enemy unit takes damage if it is alive
	    		if (enemyUnit.isAlive())
	    		{				
	    			enemyUnit.takeAttack(ourUnit);

	    			// check to see if enemy unit died
	    			if (!enemyUnit.isAlive())
	    			{
	    				// if it died, remove it
	    				_numUnits[enemyPlayer]--;
	    			}
	    		}			
	    	}
	    	else if (move._moveType == UnitActionTypes.MOVE)
	    	{
	    		_numMovements[player]++;

	    		ourUnit.move(move, _currentTime);
	    	}
	    	else if (move._moveType == UnitActionTypes.HEAL)
	    	{
	    		Unit ourOtherUnit=getUnit(player,move._moveIndex);
	    			
	    		// attack the unit
	    		ourUnit.heal(move, ourOtherUnit, _currentTime);
	    			
	    		if (ourOtherUnit.isAlive())
	    		{
	    			ourOtherUnit.takeHeal(ourUnit);
	    		}
	    	}
	    	else if (move._moveType == UnitActionTypes.RELOAD)
	    	{
	    		ourUnit.waitUntilAttack(move, _currentTime);
	    	}
	    	else if (move._moveType == UnitActionTypes.PASS)
	    	{
	    		ourUnit.pass(move, _currentTime);
	    	}
	    }

	
	
	
	
	
	public GameState(){
		_map=null;
		_currentTime=0;
		//TODO MAX unit?
		_maxUnits=Constants.Max_Moves;
	    _sameHPFrames=0;
	    for (int u=0; u<_maxUnits; u++)
		{
	        _unitIndex[0][u] = u;
			_unitIndex[1][u] = u;
		}
	}
	
	public GameState(String filename){}

	public GameState(JNIBWAPI bwapi) {
	
		// TODO Auto-generated constructor stub
	}
	
	// misc functions
	public void finishedMoving(){
		sortUnits();

		// update the current time of the state
		updateGameTime();

	    // calculate the hp sum of each player
	    int[] hpSum=new int[2];
	    for (int p=0; p<Constants.Num_Players; p++)
		{
			hpSum[p] = 0;

			for (int u=0; u<numUnits(p); u++)
			{ 
	            hpSum[p] += getUnit(p, u).currentHP();
	        }
	    }

	    // if the hp sums match the last hp sum
	    if (hpSum[0] == _prevHPSum[0] && hpSum[1] == _prevHPSum[1])
	    {
	        _sameHPFrames++;
	    }
	    else
	    {
	        _sameHPFrames = 0;
	    }

	    for (int p=0; p<Constants.Num_Players; p++)
		{
	        _prevHPSum[p] = hpSum[p];
	    }
	}
	public void updateGameTime(){
		int who=whoCanMove().ordinal();

		// if the first player is to move, set the time to his time
		if (who == Players.Player_One.ordinal())
		{
			_currentTime = getUnit(Players.Player_One.ordinal(), 0).firstTimeFree();
		}
		// otherwise it is player two or both, so it's equal to player two's time
		else
		{
			_currentTime = getUnit(Players.Player_Two.ordinal(), 0).firstTimeFree();
		}
	}
	public boolean playerDead(int player){
		if (numUnits(player) <= 0)
		{
			return true;
		}

		for (int u=0; u<numUnits(player); u++)
		{
			if (getUnit(player, u).damage() > 0)
			{
				return false;
			}
		}

		return true;
	}
	public boolean isTerminal(){
		if (playerDead(Players.Player_One.ordinal()) || playerDead(Players.Player_Two.ordinal()))
	    {
	        return true;
	    }

	    if (_sameHPFrames > 200)
	    {
	        return true;
	    }

		for (int p=0; p<Constants.Num_Players; p++)
		{
			for (int u=0; u<numUnits(p); u++)
			{
				// if any unit on any team is a mobile attacker
				if (getUnit(p, u).isMobile() && !getUnit(p, u).canHeal())
				{
					// there is no deadlock, so return false
					return false;
				}
			}
		}

		// at this point we know everyone must be immobile, so check for attack deadlock
		for (int u1=0; u1<numUnits(Players.Player_One.ordinal()); u1++)
		{
			Unit unit1=getUnit(Players.Player_One.ordinal(), u1);

			for (int u2=0; u2<numUnits(Players.Player_Two.ordinal());u2++)
			{
				Unit unit2=getUnit(Players.Player_Two.ordinal(), u2);

				// if anyone can attack anyone else
				if (unit1.canAttackTarget(unit2, _currentTime) || unit2.canAttackTarget(unit1, _currentTime))
				{
					// then there is no deadlock
					return false;
				}
			}
		}
		
		// if everyone is immobile and nobody can attack, then there is a deadlock
		return true;
	}

	    // unit data functions
	public int numUnits(int player){
		return _numUnits[player];
	}
	public int prevNumUnits(int player) {
		return _prevNumUnits[player];
	}
	public int numNeutralUnits(){
		return _neutralUnits.size();
	}
	public int closestEnemyUnitDistance(Unit unit){
		int enemyPlayer=getEnemy(unit.player());

		int closestDist=0;

		for (int u=0; u<numUnits(enemyPlayer); u++)
		{
	        int dist=unit.getDistanceSqToUnit(getUnit(enemyPlayer, u), _currentTime);

			if (dist > closestDist)
			{
				closestDist = dist;
			}
		}

		return closestDist;
	}

	    // Unit functions
	public void sortUnits(){
		for (int p=0; p<Constants.Num_Players; p++)
		{
			if (_prevNumUnits[p] <= 1)
			{
				_prevNumUnits[p]= _numUnits[p];
				continue;
			}
			else
			{
				//TODO Sort units
	            //sort(_unitIndex[p][0], _unitIndex[p][0] + _prevNumUnits[p], UnitIndexCompare(*this, p));
				
				
				Arrays.sort(_units[p],0,_prevNumUnits[p]);

				//System.out.println(_unitIndex[p].length);
				_prevNumUnits[p]= _numUnits[p];
			}
		}	
		
	}
	public void addUnit(Unit u) throws Exception{
		checkFull(u.player());
	    //System::checkSupportedUnitType(u.type());

	    // Calculate the unitID for this unit
	    // This will just be the current total number of units in the state
	    int unitID = _numUnits[Players.Player_One.ordinal()] + _numUnits[Players.Player_Two.ordinal()];

	    // Set the unit and it's unitID
	    u.setUnitID(unitID);
	    
	    _units[u.player()][_numUnits[u.player()]]=u;

	    // Increment the number of units this player has
		_numUnits[u.player()]++;
		_prevNumUnits[u.player()]++;

	    // And do the clean-up
		finishedMoving();
		calculateStartingHealth();

	    if (!checkUniqueUnitIDs())
	    {
	        throw new Exception("GameState has non-unique Unit ID values");
	    }
		
	}
	public void addUnit(UnitType unitType, int playerID, Position pos){
		checkFull(playerID);
	    //System::checkSupportedUnitType(type);

	    // Calculate the unitID for this unit
	    // This will just be the current total number of units in the state
	    int unitID = _numUnits[Players.Player_One.ordinal()] + _numUnits[Players.Player_Two.ordinal()];

	    // Set the unit and it's unitID
	    _units[playerID][_numUnits[playerID]] = new Unit(unitType, playerID, pos);
	    _units[playerID][_numUnits[playerID]].setUnitID(unitID);
	    // Increment the number of units this player has
		_numUnits[playerID]++;
		_prevNumUnits[playerID]++;

	    // And do the clean-up
		finishedMoving();
		calculateStartingHealth();
	}
	public void addUnitWithID(Unit u){
		 checkFull(u.player());
		  //  System::checkSupportedUnitType(u.type());

		    // Simply add the unit to the array
		 _units[u.player()][_numUnits[u.player()]]=u;

		    // Increment the number of units this player has
			_numUnits[u.player()]++;
			_prevNumUnits[u.player()]++;

		    // And do the clean-up
			finishedMoving();
			calculateStartingHealth();
	}
	public void addNeutralUnit(Unit unit){
		_neutralUnits.add(unit);
	}
	public Unit getUnit(int player, int unitIndex){
		 return _units[player][_unitIndex[player][unitIndex]];
	}
	public Unit getUnitByID(int unitID) {
		for (int p=0; p<Constants.Num_Players; p++)
		{
			for (int u=0; u<numUnits(p); u++)
			{
				if (getUnit(p, u).getId() == unitID)
				{
					return getUnit(p, u);
				}
			}
		}

		System.out.println("GameState Error: getUnitByID() Unit not found");
		return null;
	}
	//public Unit getUnit(int player, int unitIndex){return null;}
	public Unit getUnitByID(int player, int unitID) {
		for (int u=0; u<numUnits(player); u++)
		{
			if (getUnit(player, u).getId() == unitID)
			{
				return getUnit(player, u);
			}
		}

		System.out.println("GameState Error: getUnitByID() Unit not found");
		return null;
	}

	public Unit getClosestEnemyUnit(int player, int unitIndex){
		int enemyPlayer=getEnemy(player);
		Unit myUnit=getUnit(player,unitIndex);

		int minDist=Integer.MAX_VALUE;
		int minUnitInd=0;
	    int minUnitID=255;

		Position currentPos = myUnit.currentPosition(_currentTime);

		for (int u=0; u<_numUnits[enemyPlayer]; u++)
		{
	        Unit enemyUnit=getUnit(enemyPlayer, u);
	        int distSq = myUnit.getDistanceSqToUnit(enemyUnit, _currentTime);

			if ((distSq < minDist))// || ((distSq == minDist) && (enemyUnit.ID() < minUnitID)))
			{
				minDist = distSq;
				minUnitInd = u;
	            minUnitID = enemyUnit.getId();
			}
	        else if ((distSq == minDist) && (enemyUnit.getId() < minUnitID))
	        {
	            minDist = distSq;
				minUnitInd = u;
	            minUnitID = enemyUnit.getId();
	        }
		}

		return getUnit(enemyPlayer, minUnitInd);
	}
	public Unit getClosestOurUnit(int player, int unitIndex){
		Unit myUnit=getUnit(player,unitIndex);

		int minDist=Integer.MAX_VALUE;
		int minUnitInd=0;

		Position currentPos = myUnit.currentPosition(_currentTime);

		for (int u=0; u<_numUnits[player]; u++)
		{
			if (u == unitIndex || getUnit(player, u).canHeal())
			{
				continue;
			}

			//size_t distSq(myUnit.distSq(getUnit(enemyPlayer,u)));
			int distSq=currentPos.getDistanceSq(getUnit(player, u).currentPosition(_currentTime));

			if (distSq < minDist)
			{
				minDist = distSq;
				minUnitInd = u;
			}
		}

		return getUnit(player, minUnitInd);
	}
	public Unit getUnitDirect(int player, int unit){
		return _units[player][unit];
	}
	public Unit getNeutralUnit(int u){
		return _neutralUnits.get(u);
	}
	    
	    // game time functions
	public void setTime(int time){
		_currentTime = time;
	}
	public int getTime(){
		return _currentTime;
	}

	    // evaluation functions
	public int    eval( int player, int evalMethod, int p1Script,int p2Script)  {
		int score=0;
		int enemyPlayer=getEnemy(player);

		// if both players are dead, return 0
		if (playerDead(enemyPlayer) && playerDead(player))
		{
			return 0;
		}

		//StateEvalScore simEval;

		/*if (evalMethod == EvaluationMethods::LTD)
		{
			score = StateEvalScore(evalLTD(player), 0);
		}
		else if (evalMethod == EvaluationMethods::LTD2)
		{
			score = StateEvalScore(evalLTD2(player), 0);
		}
		else if (evalMethod == EvaluationMethods::Playout)
		{
			score = evalSim(player, p1Script, p2Script);
		}

		if (score.val() == 0)
		{
			return score;
		}*/

		int winBonus=0;

		if (playerDead(enemyPlayer) && !playerDead(player))
		{
			winBonus = 100000;
		}
		else if (playerDead(player) && !playerDead(enemyPlayer))
		{
			winBonus = -100000;
		}

		return score + winBonus;
	}
	public int         evalLTD(int player){return 0;}
	public int         evalLTD2(int player){return 0;}
	public int         LTD(int player){return 0;}
	public int         LTD2(int player){return 0;}
	public StateEvalScore    evalSim(int player, int p1, int p2){return null;}
	public int getEnemy(int player){
		return (player + 1) % 2;
	}

	    // unit hitpoint calculations, needed for LTD2 evaluation
	public void calculateStartingHealth(){
		for (int p=0; p<Constants.Num_Players; p++)
		{
			float totalHP=0;
			float totalSQRT=0;

			for (int u=0; u<_numUnits[p]; u++)
			{
				totalHP += getUnit(p, u).maxHP() * getUnit(p, u).dpf();
				totalSQRT += Math.sqrt(getUnit(p,u).maxHP()) * getUnit(p, u).dpf();;
			}

			_totalLTD[p] = totalHP;
			_totalSumSQRT[p] = totalSQRT;
		}
	}
	public void setTotalLTD(float p1,float p2){}
	public void setTotalLTD2(float p1,float p2){}
	public float getTotalLTD(int player){return 0;}  
	public float getTotalLTD2(int player){return 0;}    

	    // move related functions
	public void generateMoves(HashMap<Integer,List<UnitAction>> moves, int playerIndex) throws Exception{
		moves.clear();

	    // which is the enemy player
		int enemyPlayer  = getEnemy(playerIndex);

	    // make sure this player can move right now
	    int canMove=whoCanMove().ordinal();
	    if (canMove == enemyPlayer)
	    {
	    	System.out.println("GameState Error - Called generateMoves() for a player that cannot currently move");
	        return;//throw new Exception("GameState Error - Called generateMoves() for a player that cannot currently move");
	    }

		// we are interested in all simultaneous moves
		// so return all units which can move at the same time as the first
		int firstUnitMoveTime = getUnit(playerIndex, 0).firstTimeFree();
			
		for (int unitIndex=0; unitIndex < _numUnits[playerIndex]; unitIndex++)
		{
			// unit reference
			Unit unit =getUnit(playerIndex,unitIndex);
			if (unit==null){continue;}
			// if this unit can't move at the same time as the first
			if (unit.firstTimeFree() != firstUnitMoveTime)
			{
				// stop checking
				break;
			}

			if (unit.previousActionTime() == _currentTime && _currentTime != 0)
			{
	            System.out.println("Previous Move Took 0 Time: " + unit.previousAction().moveString());
	            return;
			}
			ArrayList<UnitAction> actionTemp=new ArrayList<UnitAction>();
			

			// generate attack moves
			if (unit.canAttackNow())
			{
				for (int u=0; u<_numUnits[enemyPlayer]; u++)
				{
					Unit enemyUnit=getUnit(enemyPlayer, u);
					if (enemyUnit!=null)
						if (unit.canAttackTarget(enemyUnit, _currentTime) && enemyUnit.isAlive())
						{
							actionTemp.add(new UnitAction(unitIndex, playerIndex, UnitActionTypes.ATTACK, u, enemyUnit.pos()));
		                    //moves.add(UnitAction(unitIndex, playerIndex, UnitActionTypes::ATTACK, unit.ID()));
						}
				}
			}
			else if (unit.canHealNow())
			{
				for (int u=0; u<_numUnits[playerIndex]; u++)
				{
					// units cannot heal themselves in broodwar
					if (u == unitIndex)
					{
						continue;
					}

					Unit ourUnit=getUnit(playerIndex, u);
					if (ourUnit!=null)
						if (unit.canHealTarget(ourUnit, _currentTime) && ourUnit.isAlive())
						{
							actionTemp.add(new UnitAction(unitIndex, playerIndex, UnitActionTypes.HEAL, u,unit.pos()));
		                    //moves.add(UnitAction(unitIndex, playerIndex, UnitActionTypes::HEAL, unit.ID()));
						}
				}
			}
			// generate the wait move if it can't attack yet
			else
			{
				if (!unit.canHeal())
				{
					actionTemp.add(new UnitAction(unitIndex, playerIndex, UnitActionTypes.RELOAD, 0,unit.pos()));
				}
			}
			
			// generate movement moves
			if (unit.isMobile())
			{
	            // In order to not move when we could be shooting, we want to move for the minimum of:
	            // 1) default move distance move time
	            // 2) time until unit can attack, or if it can attack, the next cooldown
	            double timeUntilAttack          = unit.nextAttackActionTime() - getTime();
	            timeUntilAttack                 = timeUntilAttack == 0 ? unit.attackCooldown() : timeUntilAttack;

	            // the default move duration
	            double defaultMoveDuration      = (double)Constants.Move_Distance / unit.speed();

	            // if we can currently attack
	            double chosenTime  = Math.min(timeUntilAttack, defaultMoveDuration);

	            // the chosen movement distance
	            int moveDistance       = (int) (chosenTime * unit.speed());

	            // DEBUG: If chosen move distance is ever 0, something is wrong
	            if (moveDistance == 0)
	            {
	                throw new Exception("Move Action with distance 0 generated");
	            }

	            // we are only generating moves in the cardinal direction specified in common.h
				for (int d=0; d<Constants.Num_Directions; d++)
				{			
	                // the direction of this movement
	              	Position dir= new Position(Constants.Move_Dir[d][0], Constants.Move_Dir[d][1]);
	            
	                if (moveDistance == 0)
	                {
	                    System.out.printf("%lf %lf %lf\n", timeUntilAttack, defaultMoveDuration, chosenTime);
	                }

	                // the final destination position of the unit
	                Position dest = new Position(unit.pos().getX()+moveDistance*dir.getX(),unit.pos().getY()+ moveDistance*dir.getY());

	                // if that poisition on the map is walkable
	                if (isWalkable(dest) || (unit.type().isFlyer() && isFlyable(dest)))
					{
	                    // add the move to the MoveArray
						actionTemp.add(new UnitAction(unitIndex, playerIndex, UnitActionTypes.MOVE, d, dest));
					}
				}
			}

			// if no moves were generated for this unit, it must be issued a 'PASS' move
			if (actionTemp.isEmpty())
			{
				actionTemp.add(new UnitAction(unitIndex, playerIndex, UnitActionTypes.PASS, 0,unit.pos()));
			}
			moves.put(unitIndex, actionTemp);
		}
		
	}
	public void makeMoves(List<UnitAction> moves) throws Exception{
		 if (moves.size() > 0)
		    {
		       	int canMove=whoCanMove().ordinal();
		        int playerToMove=moves.get(0).player();
		        if (canMove == getEnemy(playerToMove))
		        {
		            //throw new Exception("GameState Error - Called makeMove() for a player that cannot currently move");
		            System.out.print(" GameState Error - Called makeMove() for a player that cannot currently move ");
		        	return;
		        }
		    }
		    
		    for (int m=0; m<moves.size(); m++)
		    {
		        performUnitAction(moves.get(m));
		    }
	}
	public int getNumMovements(int player){
		return _numMovements[player];
	}
	public Players whoCanMove(){
		int p1Time=getUnit(0,0).firstTimeFree();
		int p2Time=getUnit(1,0).firstTimeFree();

		// if player one is to move first
		if (p1Time < p2Time)
		{
			return Players.Player_One;
		}
		// if player two is to move first
		else if (p1Time > p2Time)
		{
			return Players.Player_Two;
		}
		else
		{
			return Players.Player_Both;
		}
	}
	public boolean bothCanMove(){
		return getUnit(0, 0).firstTimeFree() == getUnit(1, 0).firstTimeFree();
	}
			  
	    // map-related functions
	public void setMap(Map map) throws Exception{
		_map = map;

	    // check to see if all units are on walkable tiles
	    for (int p=0; p<Constants.Num_Players; p++)
	    {
	        for (int u=0; u<numUnits(p); u++)
	        {
	            Position pos=getUnit(p, u).pos();

	            if (!isWalkable(pos))
	            {
	                //std::stringstream ss;
	                //ss << "Unit initial position on non-walkable map tile: " << getUnit(p, u).name() << " (" << pos.x() << "," << pos.y() << ")";
	                //System::FatalError(ss.str());
	            	throw new Exception("Unit is on non-walkable map tile");
	            }
	        }
	    }
		
	}
	public Map getMap() {
		return _map;
		} 
	public boolean isWalkable(Position pos){
		if (_map != null)
		{
			return _map.isWalkable(pos);
		}

		// if there is no map, then return true
		return true;
	}
	public boolean isFlyable(Position pos){
		if (_map != null)
		{
			return _map.isFlyable(pos);
		}
	
		// if there is no map, then return true
		return true;
	}            
	
	public Unit[][] getAllUnit(){
		return _units;
	}

	    // hashing functions
	public int calculateHash(int hashNum) {return 0;}

	    // state i/o functions
	public void print(){
		
		
		System.out.printf("State - Time: %d\n", _currentTime);

		for (int p=0; p<Constants.Num_Players; p++)
		{
			for (int u=0; u<_numUnits[p]; u++)
			{
				Unit unit=getUnit(p, u);

				
				System.out.printf("  P%d %5d %5d    (%3d, %3d)     %s\n", unit.player(), unit.currentHP(), unit.firstTimeFree(), unit.x(), unit.y(), unit.name());
			}
		}
		System.out.println();
	}
	public void write(String filename){}
	public void read(String filename){}
	
	public GameState clone(){
		return null;
		
		// TODO:
	}

}
