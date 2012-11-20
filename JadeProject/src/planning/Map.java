package planning;

import java.awt.Point;

public class Map {
	
	private int width;
	private int height;
	
	private Cell[][] cells;
	
	public Map(int w, int h)
	{
		this.width = w;
		this.height = h;
		cells = new Cell[h][w];
	}
	
	public void addCell(Cell c)
	{
		//this.cells[c.getPosition().x][c.getPosition().y] = c;
		this.cells[c.getPosition().y][c.getPosition().x] = c;
	}
	
	public Cell[] getAdjacent(Cell c)
	{
		Cell[] next = new Cell[4];
		Point p = c.getPosition();
		/*if(p.y != 0){next[0] = cells[p.x][p.y-1];}
		if(p.x!=width-1){next[1]=cells[p.x+1][p.y];}
        if(p.y!=height-1){next[2]=cells[p.x][p.y+1];}
        if(p.x!=0){next[3]=cells[p.x-1][p.y];}*/
		if(p.y != 0){next[0] = cells[p.y-1][p.x];}
		if(p.x!=width-1){next[1]=cells[p.y][p.x+1];}
        if(p.y!=height-1){next[2]=cells[p.y+1][p.x];}
        if(p.x!=0){next[3]=cells[p.y][p.x-1];}
        return next;
	}
	
	public Cell getLowestAdjacent(Cell c)
	{
		Cell next[] = getAdjacent(c);
        Cell small = next[0];
        double dist = Double.MAX_VALUE;
        for(int i=0;i<4;i++){
            if(next[i]!=null){
                double nextDist = next[i].getDistanceFromStart();
                if(nextDist<dist && nextDist>=0){
                  small = next[i];
                    dist = next[i].getDistanceFromStart();
                }
            }
        }
        return small;
	}
	
	public int getWidth()
	{
		return this.width;
	}
	
	public int getHeight()
	{
		return this.height;
	}
	
	public Cell[][] getCells()
	{
		return this.cells;
	}

}
