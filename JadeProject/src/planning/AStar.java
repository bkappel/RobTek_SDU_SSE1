package planning;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

public class AStar {

	
	private final int NOPATH =-1, NOTFOUND=0, FOUND=1;
	private double mincost;
	
	private Vector edge;
	private Vector done;
	private Map map;
	private int stepSpeed = 20;
	private int maxSteps = 30;
	
	private double distFromStart = 0;
	private boolean findFirst = false; 
	
	private Cell beginCell;
	private Cell finishCell;
	
	public AStar()
	{
		
	}
	
	public double manhattanDistance(Point a, Point b, double low)
	{
		return low *(Math.abs(a.x-b.x)+Math.abs(a.y-b.y)-1);
	}
	
	public Cell[] findPath(Map map, Point source, Point destination)
	{
		Cell[][] cls = map.getCells();
		mincost = Double.MAX_VALUE;
        for(int i=0;i<map.getWidth()-1;i++){
            for(int j=0;j<map.getHeight()-1;j++){
                mincost = Math.min(cls[j][i].getCost(),mincost);
            }
        }
        
        this.map = map;
        for(int i=0;i<map.getWidth()-1;i++){
            for(int j=0;j<map.getHeight()-1;j++){
                cls[j][i].resetCell();
            }
        }
        beginCell = cls[source.y][source.x];
        finishCell = cls[destination.y][destination.x];
        beginCell.setStart(true);
        finishCell.setFinish(true);
        
        edge = new Vector();
        done = new Vector();
        
        edge.addElement(beginCell);
        int pass = 0;
        boolean found = false;
        double start, diff;
        int state = NOTFOUND;
        while(state == NOTFOUND && pass<maxSteps)
        {
        	pass++;
        	state = step();
        }
        if(state==FOUND){
        	return createPath(map);
        }
        
        return null;
	}
	
	public int step(){
        int tests = 0;
        boolean found = false;
        boolean growth = true;
        Cell finish = finishCell;
        Point end = finish.getPosition();
        Vector temp = (Vector) edge.clone();
        double min = Double.MAX_VALUE;
        double score;
        Cell best = (Cell)temp.elementAt(temp.size()-1); ;
        Cell now;
        for(int i=0;i<temp.size();i++){
            now = (Cell)temp.elementAt(i);
            if(!done.contains(now)){
                score =now.getDistanceFromStart();
                score += manhattanDistance(now.getPosition(),end,mincost);
                if(score<min){
                    min = score;
                        best = now;
                }
            }
        }
        now = best;
        edge.removeElement(now);
        done.addElement(now);
        Cell next[] = map.getAdjacent(now);
        for(int i=0;i<4;i++){
            if(next[i]!=null){
                if(next[i]==finish){found=true;}
                if(!next[i].isBlocked()){
                    next[i].addToPathFromStart(now.getDistanceFromStart());
                    tests++;
                    if(!edge.contains(next[i]) && !done.contains(next[i])){edge.addElement(next[i]);growth=true;}
                }
            }
            if(found){return FOUND;}
        }
        if(edge.size()==0){return NOPATH;}
        return NOTFOUND;
    }

	public Cell[] createPath(Map map)
	{
		ArrayList<Cell> pathInv = new ArrayList<Cell>();
		boolean finished = false;
		Cell next;
		Cell now = finishCell;
		Cell stop = beginCell;
		pathInv.add(now);
		while(!finished)
		{
			next = map.getLowestAdjacent(now);
			now = next;
			//now.setPartOfPath(true);
			pathInv.add(next);
			if(now == stop){
				finished = true;
				pathInv.add(now);
			}
		}
		Collections.reverse(pathInv);
		ArrayList<Cell> tmp = new ArrayList<Cell>();
		for(int i =1; i < pathInv.size(); i++)
		{
			Cell current = pathInv.get(i);
			Cell previous = pathInv.get(i-1);
			if(current.getPosition().equals(previous.getPosition()))
			{
				tmp.add(current);
			}else
			{
				//tmp.add(previous);
				tmp.add(current);
			}
		}
		
		
		return tmp.toArray(new Cell[tmp.size()]);
		//return pathInv.toArray(new Cell[pathInv.size()]);
	}
	
}
