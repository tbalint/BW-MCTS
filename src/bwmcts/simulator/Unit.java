package bwmcts.simulator;

import javabot.types.UnitType;

/**
 * Representation of a unit.
 * 
 * @author Niels
 *
 */
public class Unit {

	private Position position;
	int hitPoints;
	int nextAttack;
	int nextMove;
	UnitType type;
	
	public Unit(Position position, int hitPoints, int nextAttack, int nextMove,
			UnitType type) {
		super();
		this.position = position;
		this.hitPoints = hitPoints;
		this.nextAttack = nextAttack;
		this.nextMove = nextMove;
		this.type = type;
	}
	
	public Unit clone(){
		Unit clone = new Unit(new Position(position.getX(), position.getY()), hitPoints, nextAttack, nextMove, type);
		return clone;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public int getHitPoints() {
		return hitPoints;
	}

	public void setHitPoints(int hitPoints) {
		this.hitPoints = hitPoints;
	}

	public int getNextAttack() {
		return nextAttack;
	}

	public void setNextAttack(int nextAttack) {
		this.nextAttack = nextAttack;
	}

	public int getNextMove() {
		return nextMove;
	}

	public void setNextMove(int nextMove) {
		this.nextMove = nextMove;
	}

	public UnitType getType() {
		return type;
	}

	public void setType(UnitType type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((position == null) ? 0 : position.hashCode());
		result = prime * result + ((type == null) ? 0 : type.getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Unit other = (Unit) obj;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.getName().equals(other.type.getName()))
			return false;
		return true;
	}
	
	
	
}
