package bwmcts.sparcraft;

import javabot.types.UnitType.UnitTypes;

import javax.swing.*;

import java.awt.*;
import java.util.HashMap;

public class SparcraftUI extends JComponent {
	
	int offSetX=30;
	int offSetY=30;
	
	
	HashMap<String,Image> images=new HashMap<String,Image>();
	Image background;
	String dirPath="c:\\itu\\Sparcraft\\starcraft_images\\";
	//Image Terran_Marine;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private GameState _state;
	
	public SparcraftUI(GameState state) {
		_state=state;
		for (UnitTypes u: UnitTypes.values()){
			System.out.println(u.toString());
			System.out.println(dirPath+u.toString()+".png");
			images.put(u.toString(), Toolkit.getDefaultToolkit().getImage(dirPath+"units\\"+u.toString()+".png"));
		}
		//Terran_Marine = Toolkit.getDefaultToolkit().getImage("c:\\itu\\Sparcraft\\starcraft_images\\units\\Terran_Marine.png");
		int  i=(int)(Math.random()*10 % 4); 
		background=Toolkit.getDefaultToolkit().getImage(dirPath+"ground\\ground"+(i>0?i:"")+".png");
	}
	
	
	public void paint(Graphics g){
		Map map=_state.getMap();
		if (map!=null){
			if (background !=null){
				g.drawImage(background, offSetX, offSetY, map.getPixelWidth(), map.getPixelHeight(),this);
			} else {
				g.drawRect(offSetX, offSetY, map.getPixelWidth(), map.getPixelHeight());
			}
			
		}
		
		g.setColor(Color.blue);
		for (Unit a : _state.getAllUnit()[0]){
			if (a!=null && a.isAlive()){
				Image i=images.get(a.type().getName().replaceAll(" ", "_"));
				if (i!=null){
					drawImageOnPosition(g,i,a.pos());
			    } else{
			    	g.drawOval(a.pos().getX()-2+offSetX, a.pos().getY()-2+offSetY, 4, 4);
			    }
				if (a._previousAction!=null && a._previousAction._moveType==UnitActionTypes.ATTACK){
					g.drawLine(a.pos().getX()-2+offSetX, a.pos().getY()-2+offSetY, a._previousAction.pos().getX()-2+offSetX, a._previousAction.pos().getY()-2+offSetY);
				}
					
			}
			
		}
		g.setColor(Color.red);
		for (Unit a : _state.getAllUnit()[1]){
			if (a!=null && a.isAlive()){
				Image i=images.get(a.type().getName().replaceAll(" ", "_"));
				if (i!=null){
					drawImageOnPosition(g,i,a.pos());
			    } else {
			    	g.drawOval(a.pos().getX()-2+offSetX, a.pos().getY()-2+offSetY, 4, 4);
			    }
				if (a._previousAction!=null && a._previousAction._moveType==UnitActionTypes.ATTACK){
					g.drawLine(a.pos().getX()-2+offSetX, a.pos().getY()-2+offSetY, a._previousAction.pos().getX()-2+offSetX, a._previousAction.pos().getY()-2+offSetY);
				}
			}
			
		} 
	}
	
	public void setGameState(GameState state){
		_state=state;
	}
	
	private void drawImageOnPosition(Graphics g,Image i, Position p){
		int width=i.getWidth(this);
		int height=i.getHeight(this);

		g.drawImage(i, p.getX()+offSetX-(int)(width/2), p.getY()+offSetY-(int)(height/2), width, height, this);
	}
}
