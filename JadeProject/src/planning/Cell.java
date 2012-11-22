package planning;

import java.awt.Point;

public class Cell {
	
	public static final double NORMAL = 1, BLOCKED = Double.MAX_VALUE, 
			PRODUCT = 50, QUEUE = 1.3,CLAIMED = 5;
	private double cost = 1.0;
	private Point position;
	
	private boolean isStart = false;
	private boolean isFinish = false;
	
	private boolean used = false;
	private double distFromStart = -1;
	private double distFromFinish = -1;
	
	private boolean partOfPath = false;
	
	public Cell()
	{
	}
	
	public void setPosition(Point position)
	{
		this.position = position;
	}
	
	public Point getPosition()
	{
		return this.position;
	}
	
	public double getCost()
	{
		return this.cost;
	}
	
	public void setCost(double cost)
	{
		this.cost = cost;
	}
	
	public void addToPathFromStart(double dist)
	{
		used = true;
		
		if(distFromStart == -1){
            distFromStart = dist+cost;
            return;
        }
        if(dist+cost<distFromStart){
            distFromStart = dist+cost;
        }
	}
	
	public void addToPathFromFinish(double dist){
        used = true;
        if(distFromFinish == -1){
            distFromFinish = dist+cost;
            return;
        }
        if(dist+cost<distFromFinish){
            distFromFinish = dist+cost;
        }
    }
	
	public boolean isStart()
	{
		return this.isStart;
	}

	public void setStart(boolean flag)
	{
		this.isStart = flag;
	}

	public boolean isFinish()
	{
		return this.isFinish;
	}
	
	public void setFinish(boolean flag)
	{
		this.isFinish = flag;
	}
	
	public boolean isBlocked()
	{
		return cost == BLOCKED;
	}
	
	public boolean isProduct()
	{
		return cost== PRODUCT;
	}
	
	public void setQueue(boolean flag)
	{
		if(flag)cost = QUEUE;
		else cost = NORMAL;
	}
	
	public void setProduct(boolean flag)
	{
		if(flag)cost = PRODUCT;
		else cost = NORMAL;
	}
	
	public void setBlocked(boolean flag)
	{
		if(flag)cost = BLOCKED;
		else cost = NORMAL;
	}
	
	public boolean isUsed()
	{
		return this.used;
	}
	
	public void resetCell()
	{
		used = false;
		this.setPartOfPath(false);
		this.distFromStart = this.distFromFinish = -1;
		this.setFinish(false);
		this.setStart(false);
	}
	
	public boolean isPartOfPath()
	{
		return this.partOfPath;
	}
	
	public void setPartOfPath(boolean flag)
	{
		this.partOfPath = flag;
	}
	
	public double getDistanceFromStart()
	{
		if(this.isStart){return 0;}
		if(this.isBlocked()){return -1;}
		return this.distFromStart;
	}
	
}
