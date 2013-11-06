package bwmcts.sparcraft;

import javabot.types.UnitSizeType;
import javabot.types.UnitType;
import javabot.types.UnitType.UnitTypes;
import javabot.types.WeaponType;

public class Unit implements Comparable<Unit> {
    UnitType     _unitType;				// the BWAPI unit type that we are mimicing
    int        _range;
	
	Position            _position;				// current location in a possibly infinite space
	
	int              _unitID;				// unique unit ID to the state it's contained in
    int              _playerID;				// the player who controls the unit
	
	int          _currentHP;				// current HP of the unit
	int          _currentEnergy;

	int            _timeCanMove;			// time the unit can next move
	int            _timeCanAttack;			// time the unit can next attack

	UnitAction          _previousAction=new UnitAction();		// the previous move that the unit performed
	int            _previousActionTime;	// the time the previous move was performed
	Position            _previousPosition;

    int    _prevCurrentPosTime;
     Position    _prevCurrentPos;


	public Unit(UnitType unitType, int playerID, Position pos){
		_unitType  =unitType;
	    _range    =      new      PlayerWeapon(PlayerProperties.Get(playerID), WeaponProperties.Get(_unitType.getGroundWeaponID()).type).GetMaxRange() + Constants.Range_Addition;
	    _unitID               =0;
	    _playerID             =playerID;
	    _currentHP            =unitType.getMaxHitPoints();
	    _currentEnergy        =unitType.getMaxEnergy()>0?50:0;
	    _timeCanMove          =0;
	    _timeCanAttack        =0;
	    _previousActionTime   =0;
	    _prevCurrentPosTime   =0;
	    _position=pos;
	    _prevCurrentPos=pos;
	    _previousPosition=pos;
		
	}
	//Unit(BWAPI::Unit * unit, BWAPI::Game * game, const IDType & playerID, const TimeType & gameTime);
	public Unit(UnitTypes unitType, int playerID, Position pos){
		
		
	}
	public Unit(UnitType unitType,  Position pos, int unitID, int playerID, int hp, int energy, int tm, int ta){
	
		_unitType             =unitType;
	    _range                =new PlayerWeapon(PlayerProperties.Get(playerID), WeaponProperties.Get(_unitType.getGroundWeaponID()).type).GetMaxRange() + Constants.Range_Addition;
	    //, _range                (unitType.groundWeapon().maxRange() + Constants::Range_Addition)
	    _position             =pos;
	    _unitID               =unitID;
	    _playerID             =playerID;
	    _currentHP            =hp;
	    _currentEnergy        =energy;
	    _timeCanMove          =tm;
	    _timeCanAttack        =ta;
	    _previousActionTime   =0;
	    _prevCurrentPosTime   =0;
	    _prevCurrentPos=pos;
	    _previousPosition=pos;
    }

	
	

    public Unit() {
		// TODO Auto-generated constructor stub
	}
	// action functions
	public void setPreviousAction(UnitAction m, int previousMoveTime){
		 _previousAction = m;
		 _previousActionTime = previousMoveTime; 
		
	}
	public void updateAttackActionTime(int newTime){
		 _timeCanAttack = newTime; 
	}
	public void updateMoveActionTime(int newTime){
		 _timeCanMove = newTime;
	}
	public void attack(UnitAction move, Unit target, int gameTime){
		if (_previousAction.type() == UnitActionTypes.ATTACK || _previousAction.type() == UnitActionTypes.RELOAD)
	    {
	        // add the repeat attack animation duration
	        // can't attack again until attack cooldown is up
	        updateMoveActionTime      (gameTime + attackRepeatFrameTime());
	        updateAttackActionTime    (gameTime + attackCooldown());
	    }
	    // if there previous action was a MOVE action, add the move penalty
	    else if (_previousAction.type() == UnitActionTypes.MOVE)
	    {
	        updateMoveActionTime      (gameTime + attackInitFrameTime() + 2);
	        updateAttackActionTime    (gameTime + attackCooldown() + Constants.Move_Penalty);
	    }
	    else
	    {
	        // add the initial attack animation duration
	        updateMoveActionTime      (gameTime + attackInitFrameTime() + 2);
	        updateAttackActionTime    (gameTime + attackCooldown());
	    }
	    setPreviousAction(move, gameTime);
	}
	public void heal(UnitAction move, Unit target, int gameTime){
		 _currentEnergy -= healCost();

	    // can't attack again until attack cooldown is up
	    updateAttackActionTime        (gameTime + healCooldown());
	    updateMoveActionTime          (gameTime + healCooldown());

	    if (currentEnergy() < healCost())
	    {
	        updateAttackActionTime(1000000);
	    }

	    setPreviousAction(move, gameTime);
	
	}
	public void move(UnitAction move,int gameTime) {
		
		 _previousPosition = pos();

		    // get the distance to the move action destination
		    int dist = move.pos().getDistance(pos());
		    
		    // how long will this move take?
		    int moveDuration = (int)((double)dist / speed());

		    // update the next time we can move, make sure a move always takes 1 time step
		    updateMoveActionTime(gameTime + Math.max(moveDuration, 1));

		    // assume we need 4 frames to turn around after moving
		    updateAttackActionTime(Math.max(nextAttackActionTime(), nextMoveActionTime()));

		    // update the position
		    //_position.addPosition(dist * dir.x(), dist * dir.y());
		    _position.moveTo(move.pos());
		    
		    setPreviousAction(move, gameTime);
	}
	public void waitUntilAttack(UnitAction move, int gameTime){
		updateMoveActionTime(_timeCanAttack);
	    setPreviousAction(move, gameTime);
	}
	public void pass(UnitAction move, int gameTime){
		updateMoveActionTime(gameTime + Constants.Pass_Move_Duration);
	    updateAttackActionTime(gameTime + Constants.Pass_Move_Duration);
	    setPreviousAction(move, gameTime);
	}
	public void takeAttack(Unit attacker){
		PlayerWeapon weapon=attacker.getWeapon(this);
	    int      damage=weapon.GetDamageBase();

	    damage =Math.max((int)((damage-getArmor()) * weapon.GetDamageMultiplier(getSize())), 2);
	    
	    //std::cout << (int)attacker.player() << " " << damage << "\n";

	    updateCurrentHP(_currentHP - damage);
	}
	public void takeHeal(Unit healer){
		updateCurrentHP(_currentHP + healer.healAmount());
	}

	// conditional functions
	public boolean isMobile() {
		 return _unitType.isCanMove(); 
	}
	public boolean isOrganic()  {
		return _unitType.isOrganic(); 
	}
	public boolean isAlive()   {
		return _currentHP > 0;
	}
	public boolean canAttackNow()    {
		return !canHeal() && _timeCanAttack <= _timeCanMove;
	}
	public boolean canMoveNow()     {
		return isMobile() && _timeCanMove <= _timeCanAttack; 
	}
	public boolean canHealNow()  {
		return canHeal() && (currentEnergy() >= healCost()) && (_timeCanAttack <= _timeCanMove); 
	}
	public boolean canKite()      {
		return _timeCanMove < _timeCanAttack; 
	}
	public boolean canHeal()      { 
		return _unitType.getID() == UnitTypes.Terran_Medic.ordinal(); 
	}
	public boolean equalsID(Unit rhs) {return true;}
	public boolean canAttackTarget(Unit unit,int gameTime){

		WeaponType weapon =WeaponProperties.props[ unit.type().isFlyer() ? type().getAirWeaponID() : type().getGroundWeaponID()].type;

	    if (weapon.getDamageAmount() == 0)
	    {
	        return false;
	    }

	    // range of this unit attacking
	    int r = range();

	    // return whether the target unit is in range
	    return (r * r) >= getDistanceSqToUnit(unit, gameTime);
    }
	public boolean canHealTarget(Unit unit, int gameTime) { 
		if (!canHeal() || !unit.isOrganic() || !(unit.player() == player()) || (unit.currentHP() == unit.maxHP()))
	    {
	        // then it can't heal the target
	        return false;
	    }

	    // range of this unit attacking
	    int r = healRange();

	    // return whether the target unit is in range
	    return (r * r) >= getDistanceSqToUnit(unit, gameTime);
    }

    // id related
	public void setUnitID(int id){
		_unitID=id;
		}
	public int  getId(){
		return _unitID;
		}
	public int  player(){
		return _playerID;
		}

    // position related functions
	public Position  position() {
		return _position;
	}
	public Position pos() {
		return _position;
	}
	int  x()  {
		return _position.getX();
	}
	int  y() {
		return _position.getY();
	}
	public int range()  {
		return _range; 
	}
	public int healRange()  {
		return canHeal() ? 96 : 0; 
	}
	public int getDistanceSqToUnit(Unit u, int gameTime) {
		return getDistanceSqToPosition(u.currentPosition(gameTime), gameTime); 
	}
	public int getDistanceSqToPosition(Position p, int gameTime) {
		return currentPosition(gameTime).getDistanceSq(p);
	}
    public Position currentPosition(int gameTime)  {
    	if (_previousAction.type() == UnitActionTypes.MOVE)
        {
            // if gameTime is equal to previous move time then we haven't moved yet
            if (gameTime == _previousActionTime)
            {
                return _previousPosition;
            }
            // else if game time is >= time we can move, then we have arrived at the destination
            else if (gameTime >= _timeCanMove)
            {
                return _position;
            }
            // otherwise we are still moving, so calculate the current position
            else if (gameTime == _prevCurrentPosTime)
            {
                return _prevCurrentPos;
            }
            else
            {
                int moveDuration = _timeCanMove - _previousActionTime;
                float moveTimeRatio = (float)(gameTime - _previousActionTime) / moveDuration;
                _prevCurrentPosTime = gameTime;

                // calculate the new current position
                _prevCurrentPos = new Position(_position.getX(),_position.getY());
                _prevCurrentPos.subtractPosition(_previousPosition);
                _prevCurrentPos.scalePosition(moveTimeRatio);
                _prevCurrentPos.addPosition(_previousPosition);
               
                //_prevCurrentPos = _previousPosition + (_position - _previousPosition).scale(moveTimeRatio);
                return _prevCurrentPos;
            }
        }
        // if it wasn't a MOVE, then we just return the Unit position
        else
        {
            return _position;
        }
    }
    public void setPreviousPosition(int gameTime){
    	int moveDuration = _timeCanMove - _previousActionTime;
        float moveTimeRatio = (float)(gameTime - _previousActionTime) / moveDuration;
        _prevCurrentPosTime = gameTime;
        Position temp=_previousPosition;
        Position temp2=_position;
        temp2.subtractPosition(_previousPosition);
        temp2.scalePosition(moveTimeRatio);
        temp.addPosition(temp2);
        _prevCurrentPos = temp;
    }

    // health and damage related functions
	public int damage()  {
		 return _unitType.getID() == UnitTypes.Protoss_Zealot.ordinal() ? 
			        2 * (int)WeaponProperties.Get(_unitType.getGroundWeaponID()).type.getDamageAmount() : 
			    (int)WeaponProperties.Get(_unitType.getGroundWeaponID()).type.getDamageAmount(); 
	}
	
	public int damageGround()  {
		 return _unitType.getID() == UnitTypes.Protoss_Zealot.ordinal() ? 
			        2 * (int)WeaponProperties.Get(_unitType.getGroundWeaponID()).type.getDamageAmount() : 
			    (int)WeaponProperties.Get(_unitType.getGroundWeaponID()).type.getDamageAmount(); 
	}
	
	public int damageAir(){
		return WeaponProperties.Get(_unitType.getAirWeaponID()).type.getDamageAmount();
	}
	
	public int healAmount() {
		 return canHeal() ? 6 : 0;
	}
	public int maxHP() {
		return _unitType.getMaxHitPoints() + _unitType.getMaxShields(); 
	}
	public int currentHP() {
		return _currentHP; 
	}
	public int currentEnergy() {
		return _currentEnergy;
	}
	public int maxEnergy() {
		return _unitType.getMaxEnergy(); 
	}
	public int healCost() {
		return 3; 
	}
	public int getArmor() {
		  return UnitProperties.Get(type()).GetArmor(PlayerProperties.Get(player())); 
	}
	
	public float dpf()  {
		return (float)Math.max(Constants.Min_Unit_DPF, (float)damage() / ((float)attackCooldown() + 1)); 
	}
	public void updateCurrentHP(int newHP){
		 _currentHP =Math.min(maxHP(), newHP); 
	}
	public int getSize() {
		 return _unitType.getSizeID();
	}
	public WeaponType getWeapon(UnitType target) {
		return target.isFlyer() ? WeaponProperties.Get(_unitType.getAirWeaponID()).type : WeaponProperties.Get(_unitType.getGroundWeaponID()).type;
	}
	public PlayerWeapon getWeapon(Unit target){
		 return new PlayerWeapon(PlayerProperties.Get(player()), target.type().isFlyer() ? WeaponProperties.Get(_unitType.getAirWeaponID()).type : WeaponProperties.Get(_unitType.getGroundWeaponID()).type);
	}

    // time and cooldown related functions
	public int moveCooldown() {
		return (int)((double)Constants.Move_Distance / _unitType.getTopSpeed()); 
	}
	public int attackCooldown()  {
		return WeaponProperties.Get(_unitType.getGroundWeaponID()).GetCooldown(PlayerProperties.Get(_playerID)); 
	}
	public int healCooldown(){
		return 8;
	}
	public int nextAttackActionTime(){
		return _timeCanAttack; 
	}
	public int nextMoveActionTime(){
		return _timeCanMove;
	}
	public int previousActionTime() {
		return _previousActionTime; 
	}
	public int firstTimeFree(){
		return _timeCanAttack <= _timeCanMove ? _timeCanAttack : _timeCanMove; 
	}
	public int attackInitFrameTime() {
		return AnimationFrameData.getAttackFrames(_unitType)[0]; 
	}
	public int attackRepeatFrameTime(){
		return AnimationFrameData.getAttackFrames(_unitType)[1]; 
	}
	public void setCooldown(int attack, int move){
		 _timeCanAttack = attack; _timeCanMove = move; 
	}

    // other functions
	public int typeID(){
		return _unitType.getID(); 
	}
	public double  speed(){
		 return _unitType.getTopSpeed(); 
	}
	public UnitType   type(){
		 return _unitType; 
	}
	public UnitAction  previousAction() {
		 return _previousAction; 
	}
	public String name() {
		return _unitType.getName().replaceAll(" ", "_"); 
	}
	public void print() {
		 System.out.printf("%s %5d [%5d %5d] (%5d, %5d)\n", _unitType.getName(), currentHP(), nextAttackActionTime(), nextMoveActionTime(), x(), y());
	}
	
	public int compareTo(Unit u) {
		// TODO Auto-generated method stub
		if (!isAlive())
	    {
			return 1;
	        //return -1;
	    }
	    else if (!u.isAlive())
	    {
	    	return -1;
	        //return 1;
	    }

	    if (firstTimeFree() == u.firstTimeFree())
	    {
	    	return getId() >= u.getId()? 1:-1;
	        //return getId() < u.getId()? 1:-1;
	    }
	    else
	    {
	    	return firstTimeFree() >= u.firstTimeFree()? 1:-1;
	        //return firstTimeFree() < u.firstTimeFree()? 1:-1;
	    }
		
	}
    
	public Unit clone(){
		Unit u=new Unit();
		u._unitType=this._unitType;				// the BWAPI unit type that we are mimicing
	    u._range=this._range;
		
	    if (this._position!=null)
	    	u._position=new Position(this._position.getX(),this._position.getY());				// current location in a possibly infinite space
		
		u._unitID=this._unitID;				// unique unit ID to the state it's contained in
	    u._playerID=this._playerID;				// the player who controls the unit
		
		u._currentHP=this._currentHP;				// current HP of the unit
		u._currentEnergy=this._currentEnergy;

		u._timeCanMove=this._timeCanMove;			// time the unit can next move
		u._timeCanAttack=this._timeCanAttack;			// time the unit can next attack

		if (this._previousAction!=null)
			u._previousAction=this._previousAction.clone();;		// the previous move that the unit performed
		u._previousActionTime=this._previousActionTime;	// the time the previous move was performed
		if (this._previousPosition!=null)
			u._previousPosition=new Position(this._previousPosition.getX(),this._previousPosition.getY());

	    u._prevCurrentPosTime=this._prevCurrentPosTime;
	    if (this._prevCurrentPos!=null)
	    	u._prevCurrentPos=new Position(this._prevCurrentPos.getX(),this._prevCurrentPos.getY());
		return u;
	}
	
	public static Unit translateUnit(javabot.model.Unit u){
		return new Unit();
	}
	
	// hash functions
	//const HashType          calculateHash(const size_t & hashNum, const TimeType & gameTime) const;
	//void                    debugHash(const size_t & hashNum, const TimeType & gameTime) const;
}
