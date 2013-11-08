package bwmcts.sparcraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;


import bwmcts.sparcraft.players.Player;

public class Game {

	/**
	 * @param args
	 */
	
	private GameState state;
	
	private Player[]	_players=new Player[2];
	int	_playerToMoveMethod;
	private int				rounds;
	private Timer				t=new Timer();
	private double				gameTimeMS;
	public int				moveLimit;
	
	private boolean display=false;
	private SparcraftUI ui;
		
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
	    	state.print();
	    }
	}

	
// play the game until there is a winner
	public void play(){

		ArrayList<UnitAction>scriptMoves_A = new ArrayList<UnitAction>();
		ArrayList<UnitAction>scriptMoves_B = new ArrayList<UnitAction>();

	    t.start();
	    
	    // play until there is no winner
	    while (!this.gameOver()){
	        if (rounds >= moveLimit)
	        {
	            break;
	        }

	        Timer frameTimer=new Timer();
	        frameTimer.start();
	
	        scriptMoves_A.clear();
	        scriptMoves_B.clear();
	
	        // the player that will move next
	        int playerToMove=getPlayerToMove();
	        Player toMove = _players[playerToMove];
	        Player enemy = _players[state.getEnemy(playerToMove)];

	        // generate the moves possible from this state
	        HashMap<Integer,List<UnitAction>> moves_A=new HashMap<Integer,List<UnitAction>>();
	        HashMap<Integer,List<UnitAction>> moves_B=new HashMap<Integer,List<UnitAction>>();
	        try {
				state.generateMoves(moves_A, toMove.ID());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

	        // the tuple of moves he wishes to make
	        toMove.getMoves(state, moves_A, scriptMoves_A);
	        
	        // if both players can move, generate the other player's moves
	        if (state.bothCanMove())
	        {
	            try {
					state.generateMoves(moves_B, enemy.ID());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	            enemy.getMoves(state, moves_B, scriptMoves_B);
	
	            try {
					state.makeMoves(scriptMoves_B);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	
	        // make the moves
	        try {
				state.makeMoves(scriptMoves_A);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        if (display)
	        {
		        state.print();
		        ui.setGameState(state);
		        ui.repaint();
	        	try {
					Thread.sleep(0);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        
	        state.finishedMoving();
	        rounds++;
	        
	        
	    }
	
	    gameTimeMS = t.getElapsedTimeInMilliSec();
	    
	    //System.out.println("Game rounds: " + rounds);
	}


	 public int getRounds(){
	    return this.rounds;
	 }
	
	public double getTime()
	{
	    return gameTimeMS;
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
	
	   //return (whoCanMove == Players::Player_Both) ? Players::Player_One : whoCanMove;
		return whoCanMove==Players.Player_Both ? Players.Player_One.ordinal(): whoCanMove.ordinal();
	}


	
	
}
