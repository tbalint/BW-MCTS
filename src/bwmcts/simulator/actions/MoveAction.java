package bwmcts.simulator.actions;

import bwmcts.simulator.Unit;

/**
 * A Move Action.
 * @author Niels
 *
 */
public class MoveAction extends Action {

	private MoveDirection direction;

	public MoveAction(Unit unit, MoveDirection direction) {
		super(unit);
		this.direction = direction;
	}

	public MoveDirection getDirection() {
		return direction;
	}

	public void setDirection(MoveDirection direction) {
		this.direction = direction;
	}
	
}
