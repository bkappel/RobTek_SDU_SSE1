package customAgents;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GUI extends JFrame implements ActionListener{

	public class guiClaimer
	{
		public int previousPoint;
		public Color previousColor;
	}
	public static final char WALL = 'X';
	public static final char BOX = 'P';
	public static final char OUT_QUEUE = 'o';
	public static final char IN_QUEUE = 'I';
	public static final char EMPTY_SPACE = '.';
	public static final char MOVE_ROBOT = 'R';
	public static final char MOVE_BOX = 'V';
	public static final char DROP_BOX = 'D';
	//public Color previousColor;
	//public Point previousLocation;
	
	public List<guiClaimer> guiClaimers = new ArrayList<guiClaimer>();
	
	private int mapWidth;
	private int mapHeight;
	private int squareSize = 25;
	private String initMapString;
	private String mapString;
	private JPanel[] map;
	private GUIAgent myAgent;

	public GUI(GUIAgent agent) {
		this.myAgent = agent;
		startGui();
	}
	public void makeMove(char who, int fromX, int fromY, int toX, int toY) {

		System.out.println("Move is made from: " +fromX+";"+fromY+ " to "+ toX+";"+toY);
		
		int fromIndex = fromY * mapWidth + fromX;
		int toIndex = toY * mapWidth + toX;
		guiClaimer currentClaimer = null;
		for(int i = 0 ; i <guiClaimers.size();i++)
		{
			if(guiClaimers.get(i).previousPoint==fromIndex)
			{
				currentClaimer=guiClaimers.get(i);
			}
		}
		if(currentClaimer==null)
		{//initialization of a robot agent
			currentClaimer = new guiClaimer();
			currentClaimer.previousColor=Color.gray;
			currentClaimer.previousPoint=toIndex;
			guiClaimers.add(currentClaimer);
		}
		
		Color fromColor = Color.PINK;
		Color toColor = Color.PINK;
		
		switch (who) {
		case MOVE_ROBOT:
			toColor = Color.RED;
			fromColor = currentClaimer.previousColor;
			break;
		case MOVE_BOX:
			toColor = Color.MAGENTA;
			if(currentClaimer.previousColor==Color.GREEN || currentClaimer.previousColor==Color.MAGENTA)
			{
				fromColor=Color.GRAY;
			}
			else fromColor = currentClaimer.previousColor;
			break;
		case DROP_BOX:
			toColor = Color.RED;
			fromColor = Color.GREEN;
			break;
		default:
			break;
		}
		
		if(!(map[toIndex].getBackground()==Color.red ||map[toIndex].getBackground()==Color.MAGENTA))
		{
			currentClaimer.previousColor=map[toIndex].getBackground();
		} 
		
		currentClaimer.previousPoint=toIndex;
		
		if (fromIndex != toIndex) {
			map[fromIndex].setBackground(fromColor);
			map[toIndex].setBackground(toColor);
		}
	}

	public String getMapString() {
		return mapWidth + ";" + mapHeight + ";" + mapString;
	}
	public void setMapString(String mapString) {
		mapString = mapString;
	}
	public JPanel createContentPane (){
		try {
			FileInputStream fstream = new FileInputStream("map.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			mapHeight = Integer.parseInt(br.readLine());
			mapWidth = Integer.parseInt(br.readLine());
			mapString  = new String();

			for (int i = 0; i < mapHeight; i++) {
				mapString += br.readLine();
			}

			//Close the input stream
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		map = new JPanel[mapHeight*mapWidth];

		JPanel totalGUI = new JPanel();
		totalGUI.setLayout(null);

		for (int i = 0; i < mapHeight; i++)
			for (int j = 0; j < mapWidth; j++)
			{
				int position = i*mapWidth+j;
				//System.out.println (position);
				map[position] = new JPanel();
				map[position].setLayout(null);
				switch (mapString.charAt(position)) {
				case 'X':
					map[position].setBackground(Color.BLACK);
					break;
				case '.':
					map[position].setBackground(Color.GRAY);
					break;
				case 'P':
					map[position].setBackground(Color.GREEN);
					break;
				case 'S':
					map[position].setBackground(Color.BLUE);
					break;
				case 'I':
					map[position].setBackground(Color.LIGHT_GRAY);
					break;
				case 'o':
					map[position].setBackground(Color.DARK_GRAY);
					break;
				default:
					break;
				}

				map[position].setLocation(j*squareSize, i*squareSize);
				map[position].setSize(squareSize, squareSize);

				totalGUI.add(map[position]);
			}
		initMapString = mapString;
		totalGUI.setOpaque(true);
		return totalGUI;
	}


	private void createAndShowGUI() {

		JFrame.setDefaultLookAndFeelDecorated(true);
		setContentPane(createContentPane());

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(mapWidth*squareSize+30, mapHeight*squareSize+30);
		setVisible(true);
	}

	public void startGui()
	{//TODO : Read the map from an external file and store it in global variable
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
	}

}
