package testing;

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.ArrayList;

import org.junit.Test;

import planning.AStar;
import planning.Cell;
import planning.Map;

public class PlanningTest {

	final static char MAPMOVEPLACE = '.';
	final static char MAPPRODUCTPLACE = 'P';
	final static char MAPOUTPUTQUEUE = 'o';
	final static char MAPINPUTQUEUE = 'I';
	final static char MAPWALL = 'X';
	
	
	@Test
	public void test() {
		
		int width = 28;
		int height = 20;
		String mpStr = 	"28;20;XXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
						"X..............ooooooooooo.X" +
						"X..............IIIIIIIIIIISX" +
						"X..PP..PP..PP..ooooooooooo.X" +
						"X..PP..PP..PP..IIIIIIIIIIISX" +
						"X..PP..PP..PP..ooooooooooo.X" +
						"X..PP..PP..PP..IIIIIIIIIIISX" +
						"X..PP..PP..PP..............X" +
						"X..PP..PP..PP..............X" +
						"X..PP..PP..PP..PP..PP..PP..X" +
						"X..PP..PP..PP..PP..PP..PP..X" +
						"X..PP..PP..PP..PP..PP..PP..X" +
						"X..PP..PP..PP..PP..PP..PP..X" +
						"X..PP..PP..PP..PP..PP..PP..X" +
						"X..PP..PP..PP..PP..PP..PP..X" +
						"X..PP..PP..PP..PP..PP..PP..X" +
						"X..PP..PP..PP..PP..PP..PP..X" +
						"X..........................X" +
						"X..........................X" +
						"XXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		
		AStar planner = new AStar();
		
		ArrayList<Point> srcPoint = new ArrayList<Point>();
		srcPoint.add(new Point(1,1));
		srcPoint.add(new Point(4,4));
		srcPoint.add(new Point(15,2));
		srcPoint.add(new Point(25,2));
		srcPoint.add(new Point(15,1));
		srcPoint.add(new Point(4,4));
		
		/*Point src = new Point(12,7);
		Point dest = new Point(15,2);*/

		for(int i =0; i < srcPoint.size()-1; i++)
		{
			Cell[] path = planner.findPath(this.createUIGraph(mpStr), srcPoint.get(i), srcPoint.get(i+1));
			for(Cell c : path)
			{
				System.out.println("x: " + c.getPosition().x + " y: " + c.getPosition().y);
			}
		}
		
		
		
		
		
		
		
		fail("Not yet implemented");
	}
	
	private Map createUIGraph(String uiStr) {
		String[] sub = uiStr.split(";");
		int width = Integer.parseInt(sub[0]);
		int height = Integer.parseInt(sub[1]);
		//Map uiMap = new Map(width, height);
		String map = sub[2];

		char[][] strMap = new char[height][width];

		for (int i = 0; i < height; i++) {
			String line = map.substring((i * width), ((i * width) + width));
			int linePos = 0;
			for (char p : line.toCharArray()) {
				/*switch (p) {
				case MAPMOVEPLACE:
					strMap[linePos][i] = p;// String.valueOf(p);//create movable
											// position in graph
					break;
				case MAPOUTPUTQUEUE:
					strMap[linePos][i] = p;// String.valueOf(p);//create output
											// queue position in graph
					break;
				case MAPINPUTQUEUE:
					strMap[linePos][i] = p;// String.valueOf(p);//create input
											// queue position in graph
					break;
				case MAPPRODUCTPLACE:
					strMap[linePos][i] = p;// String.valueOf(p);//create product
											// position in graph
					break;
				}*/
				strMap[i][linePos] = p;
				linePos++;
			}
		}
		return this.createMap(strMap, width, height);
	}

	private Map createMap(char[][] stringMap, int width, int height) {
		Map uiMap = new Map(width, height);
		for (int i = 0; i < height-1; i++) {
			for (int j = 0; j < width-1; j++) {
				char str = stringMap[i][j];
				// Node n = createGraphNode(str, i, j);
				Cell cl = new Cell();
				cl.setPosition(new Point(j, i));
				/*
				 * MAPMOVEPLACE = '.'; MAPPRODUCTPLACE = 'P'; MAPOUTPUTQUEUE =
				 * 'o'; MAPINPUTQUEUE = 'I'; MAPWALL = 'X';
				 */
				if (str == MAPMOVEPLACE)/* || str == MAPOUTPUTQUEUE
						|| str == MAPINPUTQUEUE)*/ {
					cl.setBlocked(false);
				}else if(str == MAPINPUTQUEUE)
					{
						cl.setInputQueue(true);
					}else if(str == MAPOUTPUTQUEUE)
						{
							cl.setOutputQueue(true);
						}else if(str == MAPPRODUCTPLACE)
					{
						cl.setProduct(true);
					}
					else // is not movable create block
				{
					cl.setBlocked(true);
				}
				uiMap.addCell(cl);
			}
		}
		return uiMap;
	}

}
