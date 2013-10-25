package bwmcts.simulator.actions;

import bwmcts.simulator.Unit;

/**
 * An attack action.
 * 
 * @author Niels
 *
 */
public class AttackAction extends Action {

	private Unit target;

	public AttackAction(Unit unit, Unit target) {
		super(unit);
		this.target = target;
	}

	public Unit getTarget() {
		return target;
	}

	public void setTarget(Unit target) {
		this.target = target;
	}
	
}
