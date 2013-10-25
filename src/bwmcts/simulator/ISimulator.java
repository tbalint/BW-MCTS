package bwmcts.simulator;

public interface ISimulator {

	/**
	 * Returns the result of the game from playerA's perspective.
	 * If getResult > 0 playerA is leading the game.
	 * If getResult < 0 playerB is leading the game.
	 * If getResult = 0 the situation is tied. 
	 * @param state
	 * 		The game state to simulate from
	 * @param playerA
	 * 		Player one
	 * @param playerB
	 * 		Player two
	 * @param frames
	 * 		The number of frames to simulate before returning a result.
	 * @return
	 * 		The result of the game from playerA's perspective.
	 */
	public float getResult(	GameState state, 
							IPlayer playerA, 
							IPlayer playerB,
							int frames);
	
}
