package bwmcts.uct.iuctcd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import bwmcts.uct.UnitState;
import bwmcts.uct.NodeType;
import bwmcts.uct.UnitStateTypes;
import bwmcts.uct.iuctcd.IuctNode;
import bwmcts.sparcraft.GameState;
import bwmcts.sparcraft.Players;
import bwmcts.sparcraft.UnitAction;
import bwmcts.sparcraft.players.Player;
import bwmcts.sparcraft.players.Player_Kite;
import bwmcts.sparcraft.players.Player_NoOverKillAttackValue;
import bwmcts.sparcraft.players.Player_Pass;
import bwmcts.uct.UCT;
import bwmcts.uct.UctConfig;
import bwmcts.uct.UctNode;
import bwmcts.uct.UctStats;

public class IUCTCD extends UCT {
	
	public IUCTCD(UctConfig config) {
		super(config);
	}
	
	@Override
	public List<UnitAction> search(GameState state, long timeBudget){
		
		if (config.getMaxPlayerIndex() == 0 && state.whoCanMove() == Players.Player_Two){
			return new ArrayList<UnitAction>(); 
		} else if (config.getMaxPlayerIndex() == 1 && state.whoCanMove() == Players.Player_One){
			return new ArrayList<UnitAction>(); 
		}
		
		Date start = new Date();
		
		UctNode root = new IuctNode(null, NodeType.ROOT, new ArrayList<UnitState>(), config.getMaxPlayerIndex(), "ROOT");
		root.setVisits(1);
		
		// Reset stats if new game
		if (state.getTime()==0)
			stats.reset();
		
		int t = 0;
		while(new Date().getTime() <= start.getTime() + timeBudget){
			
			traverse(root, state.clone());
			t++;
			
		}
		
		stats.getIterations().add(t);
		//System.out.println("IUCTCD: " + t);
		
		//UctNode best = mostWinningChildOf(root);
		//UctNode best = mostVisitedChildOf(root);
		UctNode best = bestValueChildOf(root);
		//System.out.println(((IuctNode)best).getAbstractMove().size());
		
		if (best == null){
			System.out.println("IUCTCD: NULL MOVE!");
			return new ArrayList<UnitAction>();
		}
		
		if (config.isDebug())
			writeToFile(root.print(0), "tree.xml");
		
		List<UnitAction> actions = statesToActions(((IuctNode)best).getAbstractMove(), state.clone());

		return actions;
		
	}

	private float traverse(UctNode node, GameState state) {
		
		float score = 0;
		if (node.getVisits() == 0){
			node.setMove(statesToActions(((IuctNode)node).getAbstractMove(), state));
			updateState(node, state, true);
			score = evaluate(state.clone());
		} else {
			updateState(node, state, false);
			if (state.isTerminal()){
				score = evaluate(state.clone());
			} else {
				int playerToMove = getPlayerToMove(node, state);
				if (expandable(node, playerToMove))
					generateChildren(node, state, playerToMove);
				score = traverse(selectNode(node), state);
			}
		}
		node.setVisits(node.getVisits() + 1);
		node.setTotalScore(node.getTotalScore() + score);
		return score;
	}

	private boolean expandable(UctNode node, int playerToMove) {
		
		boolean us = playerToMove == config.getMaxPlayerIndex();
		if (!us && config.isNokModelling() && !node.getChildren().isEmpty())
			return false;
		
		if (node.getVisits() > config.getMaxChildren())
			return false;

		return true;
	}

	private void generateChildren(UctNode node, GameState state, int playerToMove) {
		
		List<UnitState> move = new ArrayList<UnitState>();
		
		boolean onlyNok = config.isNokModelling() && playerToMove != config.getMaxPlayerIndex();
		List<Integer> readyUnitIds = new ArrayList<Integer>();
		HashMap<Integer, List<UnitAction>> map;
		if (((IuctNode)node).getPossibleAbstractMoves() == null){

			map = new HashMap<Integer, List<UnitAction>>();
			try {
				state.generateMoves(map, playerToMove);
			} catch (Exception e) {
				e.printStackTrace();
			}
			readyUnitIds = getReadyUnitIds(map);
			((IuctNode)node).setPossibleAbstractMoves(getPossibleMoves(playerToMove, readyUnitIds, config.getMaxChildren()));
			
		}
		
		String label = "";
		
		if (onlyNok && node.getChildren().isEmpty()){
			move.addAll(getAllMove(UnitStateTypes.ATTACK, readyUnitIds, playerToMove));
			label = "NOK-AV";
		} else if (((IuctNode)node).getPossibleAbstractMoves().size() >= node.getVisits()){
			move.addAll(((IuctNode)node).getPossibleAbstractMoves().get(node.getVisits()-1));
			if (node.getVisits()==1){
				label = "NOK-AV";
			}else if (node.getVisits()==2){
				label = "KITER";
			}else{
				label = "MIX";
				if (sameScript(move))
					return;
			}
		}
		
		if (move == null || move.isEmpty())
			return;
		
		if (move.isEmpty()){
			int i = 0;
			i++;
		}
	
		IuctNode child = new IuctNode((IuctNode)node, getChildNodeType(node, state), move, playerToMove, label);
		node.getChildren().add(child);	
		
	}
	

	private boolean sameScript(List<UnitState> move) {
		UnitStateTypes type = move.get(0).type;
		for(UnitState state : move){
			if (state.type != type)
				return false;
		}
		return true;
	}

	private boolean uniqueMove(List<UnitState> move, IuctNode node) {

		if(node.getChildren().isEmpty())
			return true;
		
		for (UctNode child : node.getChildren()){
			boolean identical = true;
			if (child.getMove().size() != move.size()){
				identical = false;
			} else {
				for(int i = 0; i < move.size(); i++){
					if (!((IuctNode)child).getAbstractMove().get(i).equals(move.get(i))){
						identical = false;
						break;
					}
				}
			}
			if (identical){
				return false;
			}
		}
		
		return true;
		
	}

	private List<UnitState> getAllMove(UnitStateTypes type, List<Integer> readyUnitIds, int playerToMove) {

		List<UnitState> states = new ArrayList<UnitState>();
		
		for(Integer i : readyUnitIds){
			
			UnitState state = new UnitState(type, i, playerToMove);
			states.add(state);
			
		}
		
		return states;
	}
	
	private List<UnitState> getRandomMove(int playerToMove, HashMap<Integer, List<UnitAction>> map) {
		
		ArrayList<UnitState> move = new ArrayList<UnitState>();
		
		for(Integer i : map.keySet()){
			
			// Skip empty actions
			List<UnitAction> actions = map.get(i);
			if (actions.isEmpty())
				continue;
			
			// Random state
			UnitStateTypes type = UnitStateTypes.ATTACK;
			if (Math.random() >= 0.5f)
				type = UnitStateTypes.KITE;
			
			UnitState unitState = new UnitState(type, i, playerToMove);
			
			// Add random possible action
			move.add(unitState);
			
		}
		
		return move;
		
	}
	
	private List<List<UnitState>> getPossibleMoves(int playerToMove, List<Integer> readyUnitIds, int n) {
		
		if (readyUnitIds.isEmpty())
			return new ArrayList<List<UnitState>>();

		List<List<UnitState>> moves = new ArrayList<List<UnitState>>();
		moves.add(getAllMove(UnitStateTypes.ATTACK, readyUnitIds, playerToMove));
		moves.add(getAllMove(UnitStateTypes.KITE, readyUnitIds, playerToMove));
		
		n-=2;
		if (n>0){
			List<List<UnitState>> randomMoves = randomMoves(playerToMove, readyUnitIds, n);
			for (List<UnitState> move : randomMoves){
				if (!move.isEmpty())
					moves.add(move);
			}
		}
			
		
		return moves;
		
	}
	

	private List<Integer> getReadyUnitIds(HashMap<Integer, List<UnitAction>> map) {
		List<Integer> readyUnitIds = new ArrayList<Integer>();
		for(Integer i : map.keySet()){
			
			// Skip empty actions
			List<UnitAction> actions = map.get(i);
			if (actions.isEmpty())
				continue;
			
			readyUnitIds.add(i);
			
		}
		
		return readyUnitIds;
		
	}

	private List<List<UnitState>> randomMoves(int playerToMove, List<Integer> readyUnitIds, int n) {
			
		int r = readyUnitIds.size();
		if (r == 0 || n<=0)
			return new ArrayList<List<UnitState>>();
		
		if (r==1){
			UnitStateTypes type = UnitStateTypes.ATTACK;
			if (Math.random() >= 0.5f)
				type = UnitStateTypes.KITE;
			UnitState unitState = new UnitState(type, readyUnitIds.get(0), playerToMove);
			List<UnitState> move = new ArrayList<UnitState>();
			move.add(unitState);
			List<List<UnitState>> moves = new ArrayList<List<UnitState>>();
			moves.add(move);
			return moves;
		}

		List<List<UnitState>> movesAll = new ArrayList<List<UnitState>>();
		List<List<UnitState>> movesNok = new ArrayList<List<UnitState>>();
		List<List<UnitState>> movesKite = new ArrayList<List<UnitState>>();
		List<UnitState> moveNok = new ArrayList<UnitState>();
		List<UnitState> moveKite = new ArrayList<UnitState>();
		UnitState unitStateNok = new UnitState(UnitStateTypes.ATTACK, readyUnitIds.get(0), playerToMove);
		UnitState unitStateKite = new UnitState(UnitStateTypes.KITE, readyUnitIds.get(0), playerToMove);
		moveNok.add(unitStateNok);
		moveKite.add(unitStateKite);
		movesNok.add(moveNok);
		movesKite.add(moveKite);
		
		n -= 2;
		
		int a = (int)Math.ceil((double)n/2);
		int b = (int)Math.floor((double)n/2);
		
		List<List<UnitState>> submoves = randomMoves(playerToMove, readyUnitIds.subList(1, readyUnitIds.size()), a);
		
		int added = 0;
		for(List<UnitState> submove : submoves){
			List<UnitState> moveN = new ArrayList<UnitState>();
			for(UnitState s : moveNok)
				moveN.add(s);
			moveN.addAll(submove);
			movesAll.add(moveN);
			added++;
			if (added >= n)
				break;
			List<UnitState> moveK = new ArrayList<UnitState>();
			for(UnitState s : moveKite)
				moveK.add(s);
			moveK.addAll(submove);
			movesAll.add(moveK);
			added++;
			if (added >= n)
				break;	
		}
		
		//movesAll.addAll(movesNok);
		//movesAll.addAll(movesKite);
		
		return movesAll;
	}




	private List<UnitAction> statesToActions(List<UnitState> move, GameState state) {
		
		int player = 0;
		
		if (move == null || move.isEmpty() || move.get(0) == null)
			return new ArrayList<UnitAction>();
		else
			player = move.get(0).player;
		
		Player attack = new Player_NoOverKillAttackValue(player);
		Player kite = new Player_Kite(player);
		
		HashMap<Integer, List<UnitAction>> map = new HashMap<Integer, List<UnitAction>>();
		
		try {
			state.generateMoves(map, player);
		} catch (Exception e) {e.printStackTrace();}
		
		List<Integer> attackingUnits = new ArrayList<Integer>();
		List<Integer> kitingUnits = new ArrayList<Integer>();
		
		// Divide units into two groups
		for(UnitState unitState : move){
			
			if (unitState.type == UnitStateTypes.ATTACK)
				attackingUnits.add(unitState.unit);
			else if (unitState.type == UnitStateTypes.KITE)
				kitingUnits.add(unitState.unit);
			
		}
		
		List<UnitAction> allActions = new ArrayList<UnitAction>();
		HashMap<Integer, List<UnitAction>> attackingMap = new HashMap<Integer, List<UnitAction>>();
		HashMap<Integer, List<UnitAction>> kitingMap = new HashMap<Integer, List<UnitAction>>();

		for(Integer i : attackingUnits)
			if (map.get(i) != null)
				attackingMap.put(i, map.get(i));
			
		
		for(Integer i : kitingUnits)
			if (map.get(i) != null)
				kitingMap.put(i, map.get(i));
		
		// Add attack actions
		List<UnitAction> attackActions = new ArrayList<UnitAction>();
		attack.getMoves(state, attackingMap, attackActions);
		allActions.addAll(attackActions);
		
		// Add kite actions
		List<UnitAction> kiteActions = new ArrayList<UnitAction>();
		kite.getMoves(state, kitingMap, kiteActions);
		allActions.addAll(kiteActions);
		
		return allActions;
	}
	
	public String toString(){
		return "IUCTCD - "+this.config.toString();
	}
	
}