package bwmcts.sparcraft;


public class UnitAction {

	int	_unit,	_player,_moveIndex;
	UnitActionTypes _moveType;
	Position        _p;

	public UnitAction(){
		_unit=255;
		_player=255;
		_moveType=UnitActionTypes.NONE;
		_moveIndex=255;

	}

	public UnitAction( int unitIndex, int player, UnitActionTypes type, int moveIndex, Position dest){
		_unit=unitIndex;
		_player=player;
		_moveType=type;
		_moveIndex=moveIndex;
		_p=dest;
	}

	public UnitAction( int unitIndex, int player, UnitActionTypes type, int moveIndex){
		_unit=unitIndex;
		_player=player;
		_moveType=type;
		_moveIndex=moveIndex;
	}

	
	
	public int unit(){ return _unit; }
	public int player()	{ return _player; }
	public int index()	{ return _moveIndex; }
    public Position pos()    { return _p; }

	public String moveString(){
		if (_moveType == UnitActionTypes.ATTACK) 
		{
			return "ATTACK";
		}
		else if (_moveType == UnitActionTypes.MOVE)
		{
			return "MOVE";
		}
		else if (_moveType == UnitActionTypes.RELOAD)
		{
			return "RELOAD";
		}
		else if (_moveType == UnitActionTypes.PASS)
		{
			return "PASS";
		}
		else if (_moveType == UnitActionTypes.HEAL)
		{
			return "HEAL";
		}

		return "NONE";
	}

	public Position getDir(){
		assert(_moveType == UnitActionTypes.MOVE);

		return new Position(Constants.Move_Dir[_moveIndex][0], Constants.Move_Dir[_moveIndex][1]);
	}

	
	public UnitActionTypes type(){return _moveType;}
	
	public String toString(){
		return this._moveIndex+","+this._player+","+this._unit+","+this.moveString()+","+this.pos();
		
	}
}
