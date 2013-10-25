package bwmcts.simulator;

import java.util.ArrayList;
import java.util.List;

import bwmcts.simulator.actions.Action;

/**
 * Representation of a move which is a set of actions.
 * 
 * @author Niels
 *
 */
public class Move {

	private List<Action> actions;

	public Move() {
		super();
		this.actions = new ArrayList<Action>();
	}
	
	public Move(List<Action> actions) {
		super();
		this.actions = actions;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}
	
}
