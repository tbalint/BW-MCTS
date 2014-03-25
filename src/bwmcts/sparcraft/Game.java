/**
* This file is based on and translated from the open source project: Sparcraft
* https://code.google.com/p/sparcraft/
* author of the source: David Churchill
**/
package bwmcts.sparcraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;





import bwmcts.sparcraft.players.Player;
import bwmcts.sparcraft.players.Player_NoOverKillAttackValue;

public class Game {

	/**
	 * @param args
	 */
	
	private GameState state;
	
	private Player[]	_players=new Player[2];
	int	_playerToMoveMethod;
	private int				rounds;
	public int				moveLimit;
	
	private boolean display=false;
	public SparcraftUI ui;
		
	public Game(GameState initialState){

	}
	
	
	public Game(GameState initialState, Player p1, Player p2, int limit){
		state=initialState;
		_players[0]=p1;
		_players[1]=p2;
		this.moveLimit=limit;
		this.rounds=0;
	}

	public Game(GameState initialState, Player p1, Player p2, int limit, boolean display){
		state=initialState;
		_players[0]=p1;
		_players[1]=p2;
		this.moveLimit=limit;
		this.rounds=0;
		this.display=display;
		if (display){
	    	ui = new SparcraftUI(state);
	        
	        // Setup of the frame containing the game
	        JFrame f = new JFrame();
	        f.setSize(1000,700);
	        f.setTitle("Sparcraft in JAVA");
	        f.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
	        f.getContentPane().add(ui);    
	        f.setVisible(true);
	    	//state.print();
	    }
	}

	
// play the game until there is a winner
	public void play(){

		ArrayList<UnitAction>scriptMoves_A = new ArrayList<UnitAction>();
		ArrayList<UnitAction>scriptMoves_B = new ArrayList<UnitAction>();
		Player toMove;
		Player enemy;
		HashMap<Integer,List<UnitAction>> moves_A=new HashMap<Integer,List<UnitAction>>();
        HashMap<Integer,List<UnitAction>> moves_B=new HashMap<Integer,List<UnitAction>>();
        int playerToMove=-1;
	    // play until there is no winner
		//long generatemoves=0;
		//long getmoves=0;
		//long makemoves=0;
		//((Player_NoOverKillAttackValue)_players[0]).timeOnHpCopying=0;
		//((Player_NoOverKillAttackValue)_players[1]).timeOnHpCopying=0;
	    while (!this.gameOver()){
	    	
	        if (rounds >= moveLimit)
	        {
	            break;
	        }
	    	
	        scriptMoves_A.clear();
	        scriptMoves_B.clear();
	
	        // the player that will move next
	        playerToMove=getPlayerToMove();
	        toMove = _players[playerToMove];
	        enemy = _players[GameState.getEnemy(playerToMove)];

	        // generate the moves possible from this state
	        moves_A.clear();
	        moves_B.clear();
	        //long g=System.nanoTime();
			state.generateMoves(moves_A, toMove.ID());
			//generatemoves+=System.nanoTime()-g;
	        
	        // if both players can move, generate the other player's moves
	        if (state.bothCanMove())
	        {
	        	//g=System.nanoTime();
	        	state.generateMoves(moves_B, enemy.ID());
	        	//generatemoves+=System.nanoTime()-g;
	        	//g=System.nanoTime();
				enemy.getMoves(state, moves_B, scriptMoves_B);
				//getmoves+=System.nanoTime()-g;
				//g=System.nanoTime();
	            state.makeMoves(scriptMoves_B);
	            //makemoves+=System.nanoTime()-g;
	        }
	        
	        // the tuple of moves he wishes to make
	        //g=System.nanoTime();
	        toMove.getMoves(state, moves_A, scriptMoves_A);
	        //getmoves+=System.nanoTime()-g;
	        // make the moves
	        //g=System.nanoTime();
			state.makeMoves(scriptMoves_A);
			//makemoves+=System.nanoTime()-g;
	        if (display)
	        {
		        //state.print();
	        	GameState copy=state.clone();
	        	copy.finishedMoving();
	        	
	        	int nextTime=Math.min(copy.getUnit(0,0).firstTimeFree(), copy.getUnit(1,0).firstTimeFree());
	        	int time=state.getTime();
	        	if (time<nextTime){
		        	while (time<nextTime){
		        		copy.setTime(time);
				        ui.setGameState(copy);
				        ui.repaint();
			        	try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
			        	time++;
		        	}
	        	} else {
	        		ui.setGameState(copy);
			        ui.repaint();
		        	try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	        	}
	        }
	        
	        state.finishedMoving();
		    
	        rounds++;
	        
	    }
	    //System.out.println("time spent on sorting "+(double)state.timeSpentOnSorting/1000000);
	    //System.out.println("time spent on generatemoves "+(double)generatemoves/1000000);
	    //System.out.println("time spent on getmoves "+(double)getmoves/1000000);
	    //System.out.println("time spent on makemoves "+(double)makemoves/1000000);
	    //System.out.println("time spent on timeonHP "+(double)(((Player_NoOverKillAttackValue)_players[0]).timeOnHpCopying+((Player_NoOverKillAttackValue)_players[1]).timeOnHpCopying)/1000000);
	}


	 public int getRounds(){
	    return this.rounds;
	 }
	

// returns whether or not the game is over
	public boolean gameOver()
	{
	    return state.isTerminal(); 
	}

	public GameState getState()
	{
	    return state;
	}

// determine the player to move
	public int getPlayerToMove()
	{
	   Players whoCanMove=state.whoCanMove();
	
	   Players random = Math.random() >= 0.5 ? Players.Player_One : Players.Player_Two;
	   
	   //return (whoCanMove == Players::Player_Both) ? Players::Player_One : whoCanMove;
		return whoCanMove==Players.Player_Both ? random.ordinal(): whoCanMove.ordinal();
	}


	
	
}