package bwmcts.sparcraft;

import javax.swing.*;

import java.awt.*;

public class SparcraftUI extends JComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private GameState _state;
	
	public SparcraftUI(GameState state) {
		_state=state;
	}
	
	
	public void paint(Graphics g){
		Map map=_state.getMap();
		if (map!=null){
			g.drawRect(50, 50, map.getPixelWidth(), map.getPixelHeight());
		}
		g.setColor(Color.blue);
		for (Unit a : _state.getAllUnit()[0]){
			if (a!=null && a.isAlive()){
				g.drawOval(a.pos().getX()+48, a.pos().getY()+48, 4, 4);
				//if (a._previousAction!=null && a._previousAction._moveType==UnitActionTypes.ATTACK){
				//	System.out.println(a.previousAction().toString());
				//	g.drawLine(a.pos().getX()-2, a.pos().getY()-2, a._previousAction.pos().getX()-2, a._previousAction.pos().getY()-2);
				//}
					
			}
			
		}
		g.setColor(Color.red);
		for (Unit a : _state.getAllUnit()[1]){
			if (a!=null && a.isAlive()){
				g.drawOval(a.pos().getX()+48, a.pos().getY()+48, 4, 4);
				//if (a._previousAction!=null && a._previousAction._moveType==UnitActionTypes.ATTACK){
				//	g.drawLine(a.pos().getX()-2, a.pos().getY()-2, a._previousAction.pos().getX()-2, a._previousAction.pos().getY()-2);
				//}
			}
			
		} 
	}
	
	public void setGameState(GameState state){
		_state=state;
	}
}
