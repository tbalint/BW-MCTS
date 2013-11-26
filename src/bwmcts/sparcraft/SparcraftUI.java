package bwmcts.sparcraft;

import javabot.types.UnitType.UnitTypes;

import javax.swing.*;

import bwmcts.clustering.UPGMA;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class SparcraftUI extends JComponent {
	
	int offSetX=250;
	int offSetY=10;
	
	
	HashMap<String,Image> images=new HashMap<String,Image>();
	Image background;
	String dirPath="img\\";
	//Image Terran_Marine;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private GameState _state;
	
	public SparcraftUI(GameState state) {
		_state=state;
		for (UnitTypes u: UnitTypes.values()){
			//System.out.println(u.toString());
			//System.out.println(dirPath+u.toString()+".png");
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
			drawScaleForMap(g, map.getPixelWidth(), map.getPixelHeight());
		}
		
		g.setColor(Color.blue);
		int k=0;
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
				drawUnitInformation(g,a,++k);
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
				drawUnitInformation(g,a,++k);
			}
			
		} 
		
		/*UPGMA clustering=new UPGMA(_state.getAllUnit()[1], 1, 1);
	    HashMap<Integer,List<Unit>>clusters=  clustering.getClusters(5);
	    int clusterId=0;
	    for (List<Unit> list : clusters.values()){
	    	g.setColor(getColor(clusterId++));
	    	for (Unit a:list){
	    		g.drawOval(a.pos().getX()-10+offSetX, a.pos().getY()-10+offSetY, 20, 20);
	    	}
	    }*/
	}
	
	private void drawScaleForMap(Graphics g, int pixelWidth, int pixelHeight) {
		for (int i=0; i<(int)pixelWidth;i+=50){
			if (i!=0 && i % 100==0){
				g.drawString(String.valueOf(i), offSetX+i, offSetY);
			}
			g.drawLine(offSetX+i, offSetY, offSetX+i, offSetY-10);
		}
		for (int i=0; i<(int)pixelHeight;i+=50){
			if (i!=0 && i % 100==0){
				g.drawString(String.valueOf(i), offSetX-20, offSetY+i);
			}
			g.drawLine(offSetX-10, offSetY+i, offSetX, offSetY+i);
		}
	}


	private void drawUnitInformation(Graphics g, Unit u, int i) {
		g.drawString(u.getId()+":"+u.name()+" HP:"+u.currentHP()+" A:"+u.getArmor()+" D:"+u.damageGround()+"/"+u.damageAir(), 3, i*20);
		
		g.drawRect(u.pos().getX()+offSetX-15, u.pos().getY()-15+offSetY, (int)(30*u._currentHP/u.maxHP()), 1);
		
	}

	public void setGameState(GameState state){
		_state=state;
	}
	
	private void drawImageOnPosition(Graphics g,Image i, Position p){
		int width=i.getWidth(this);
		int height=i.getHeight(this);

		g.drawImage(i, p.getX()+offSetX-(int)(width/2), p.getY()+offSetY-(int)(height/2), width, height, this);
	}
	
	private Color getColor(int i){
		
		switch (i){
			case 0:
				return Color.CYAN;
			case 1:
				return Color.GREEN;
			case 2:
				return Color.WHITE;
			case 3:
				return Color.PINK;
			case 4:
				return Color.ORANGE;
			case 5:
				return Color.YELLOW;
			case 6:
				return Color.LIGHT_GRAY;
			case 7:
				return Color.RED;
			case 8:
				return Color.BLUE;
			default:
				return Color.BLACK;
		}
	}
}
