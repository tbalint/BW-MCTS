package bwmcts.sparcraft;

public class Position {

	int x;
	int y;
	
	public Position(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	public int getDistanceSq(Position p) {
		return (x-p.getX())*(x-p.getX()) + (y-p.getY())*(y-p.getY());
	}

	public void subtractPosition(Position pos) {
		x -= pos.getX();
		y -= pos.getY();
	}

	public void scalePosition(float f) {
		x = (int)(f * x);
        y = (int)(f * y);
		
	}

	public void addPosition(Position pos) {
		x += pos.getX();
		y += pos.getY();
	}

	public void moveTo(Position pos) {
		x=pos.getX();
		y=pos.getY();
		
	}

	public int getDistance(Position pos) {
		int dX = x - pos.getX();
        int dY = y - pos.getY();

        if (dX == 0)
        {
            return Math.abs(dY);
        }
        else if (dY == 0)
        {
            return Math.abs(dX);
        }
        else
        {
            return (int)Math.sqrt((double)(dX*dX - dY*dY));
        }
	}
	
	public String toString(){
		return "("+x+" : "+y+")";
	}
	
}
