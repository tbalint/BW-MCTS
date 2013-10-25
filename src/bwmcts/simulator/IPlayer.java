package bwmcts.simulator;

public interface IPlayer {

	/**
	 * Returns a move made by the player.
	 * @param state
	 * 		The game state from where the move should be taken.
	 * @param playerNum
	 * 		The if of the player in the game state.
	 * @return
	 * 		a move
	 */
	public Move getMove(GameState state, int playerNum);
	
}
