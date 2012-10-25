package agent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RobotAgent extends Agent {
	public Point location;//current location of robot
	public boolean isAllowedToMove;//needed to perform movement
	public boolean isMovingTowardsStorage;//true if moving to storage, false if returning an item.
	public Point destination;
	private AID[] storageAgents;// list of active storage agents
	private AID[] guiAgents;//list of active guiAgents 
	public List<Point> travelPoints = new ArrayList<Point>();//in this list is an ordered array of points where the robot is going
	public Integer currentPathCost; //amount of spaces robot needs to move before it s idle again
	boolean allowedToLeaveStorageAgent;
	/**
	*refreshes the list of current active GuiAgents
	*/
	public void updateGuiAgents()
	{
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Warehouse-GuiAgent");
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, template); 
			guiAgents = new AID[result.length];
			System.out.println("Search performed, result amount: " + result.length);
			for (int i = 0; i < result.length; ++i) {
				guiAgents[i] = result[i].getName();
				System.out.println(guiAgents[i].getName());
			}
		} catch (FIPAException e) {
			e.printStackTrace();
			System.out.println("in the catch");
		} 
	}
	
	/**
	*refreshes the list of current active StorageAgents
	*/
	public void updateStorageAgents()
	{
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Warehouse-StorageAgent");
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, template); 
			storageAgents = new AID[result.length];
			System.out.println("Search performed, result amount: " + result.length);
			for (int i = 0; i < result.length; ++i) {
				storageAgents[i] = result[i].getName();
				System.out.println(storageAgents[i].getName());
			}
		} catch (FIPAException e) {
			e.printStackTrace();
			System.out.println("in the catch");
		} 
	}
	
	/**
	*entry point of agent
	*/
	protected void setup() {
		isAllowedToMove=false;
		isMovingTowardsStorage=true;
		currentPathCost=0;
		allowedToLeaveStorageAgent=true;
		updateGuiAgents();//Robot agent wont start without an active GuiAgent
		if(guiAgents.length==0)
		{
			System.out.println("No gui agent found, robot agent is terminating");
			doDelete();
		}
		
		// RobotAgent wont start without coordination arguments (looks like: 1,1 ) on startup
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			location = new Point(Integer.parseInt((String) args[0]),Integer.parseInt((String) args[1]));
			destination = new Point(Integer.parseInt((String) args[0]),Integer.parseInt((String) args[1]));
			System.out.println(getAID().getName()+" is positioned at " + location.x +"," +location.y);
		}
		else {
			// Make the agent terminate
			System.out.println("No position defined, closing down");
			doDelete();
		}
		
		System.out.println("Hallo! Robot-agent "+getAID().getName()+" is ready.");
		
		//register at yellow pages serivce
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Warehouse-RobotAgent");
		sd.setName("Warehouse-RobotAutomation");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
			
		} catch (FIPAException e) {
			System.out.println("catch");
			e.printStackTrace();
		}
		
		addBehaviour(new OfferRequestsServer());//bid on current requests
		addBehaviour(new AcceptRequestServer());//accept an incomming request
		addBehaviour(new MovementBehaviour(this, 1000));//check movement every 1000ms
		addBehaviour(new AcceptMov());//awaits a yes or no from the storage agent after doing a movement claim request
		addBehaviour(new MapReceiver());//awaits a map message from guiAgent
		addBehaviour(new MapRequest());//one shot behaviour to load the map
		
	}

	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Printout a dismissal message
		System.out.println("Robot-agent "+getAID().getName()+" terminating.");
	}
	
	public Integer calculatePathCost(Point l)
	{//TODO : need a map to calculate path cost, need to make pathplanning solution
		
		Integer yDifference = destination.y-l.y;//Debugging purpose in version without a loaded map
		Integer xDifference = destination.x-l.x;//Debugging purpose in version without a loaded map
		if(yDifference<0)yDifference=yDifference*-1;//Debugging purpose in version without a loaded map
		if(xDifference<0)xDifference=xDifference*-1;//Debugging purpose in version without a loaded map
		return (currentPathCost+yDifference+xDifference);//Debugging purpose in version without a loaded map
	}
	
	private class OfferRequestsServer extends CyclicBehaviour {
		public void action() {//listen to incomming CFP from storageAgents
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {//only respond to CFP messages
				// CFP Message received. Process it
				String requestedLocation = msg.getContent();
				ACLMessage reply = msg.createReply();//add the sender as recipient
				Integer x = Integer.parseInt(requestedLocation.substring(0,requestedLocation.lastIndexOf(',')));//parse the x of product
				Integer y = Integer.parseInt(requestedLocation.substring(requestedLocation.lastIndexOf(',')+1,requestedLocation.length()));//parse the y of product
				Integer price = calculatePathCost(new Point(x,y));
				if (price != null) {
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(String.valueOf(price.intValue()));
					//System.out.println(getAID().getName() + ": My cost is " + price + ". To get to location: " +x + ","+y);//debugging purpose
				}
				else {
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("Pathplanning error");
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer
	
	private class AcceptRequestServer extends CyclicBehaviour {
		public void action() {//confirm that this agent will pick up the item
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {//this agent is chosen to pick up the item
				// ACCEPT_PROPOSAL Message received. Process it
				String requestedLocation = msg.getContent();
				Integer itemX = Integer.parseInt(requestedLocation.substring(0,requestedLocation.indexOf(',',1)));
				Integer itemY = Integer.parseInt(requestedLocation.substring(requestedLocation.indexOf(',',1)+1,requestedLocation.indexOf(':',1)));
				Integer inputQueueX= Integer.parseInt(requestedLocation.substring(requestedLocation.indexOf(':',1)+1,requestedLocation.indexOf(',',2)));
				Integer inputQueueY= Integer.parseInt(requestedLocation.substring(requestedLocation.indexOf(',',2)+1,requestedLocation.length()));
				ACLMessage reply = msg.createReply();

				reply.setPerformative(ACLMessage.INFORM);
				System.out.println(getAID().getName() +" will pick up the item at "+ itemX + ","+itemY +" and bring it to start of input queue at: " + (inputQueueX-11) + "," +inputQueueY);
				myAgent.send(reply);
				currentPathCost=calculatePathCost(new Point(itemX,itemY));
				
				travelPoints.add(new Point(itemX,itemY));
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer
	
	private class MovementBehaviour extends TickerBehaviour{
		public MovementBehaviour(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {//every 1000 ms
			if(isAllowedToMove  && allowedToLeaveStorageAgent)
			{//the robot claims spots with 3 in one time
				//TODO : make the robot move on his local map to the next location and edit the variable location, it wont collide with any other robot for the spot is claimed. 
			
				/*
				 * Different things need to be taken in consideration. The robot may move to return an item, or to deliver an item
				 */
				ACLMessage mapUpd = new ACLMessage(ACLMessage.INFORM);
				mapUpd.addReceiver(guiAgents[0]);//The gui agent needs to know that the robot actually did a move
				mapUpd.setContent("x,y");//x,y is for the GUI agent, it has to visualize movement
				mapUpd.setConversationId("map-update");
				myAgent.send(mapUpd); 
				currentPathCost--;
				//TODO : if the robot has moved to the last spot of the 3 claimed spots, isAllowedToMove has to be put to false. The robot has to claim 3 new spots
				if(1==0)//if robot reached a destionation
				{
					addBehaviour(new RobotAtDestionationBehaviour());//Tell the storage agent we arrived
					//TODO : remove the current destination point where the robot is now at, from the travelPoints
				}
			}
			if(0!=currentPathCost&&false==isAllowedToMove&& allowedToLeaveStorageAgent)//robot has to make movement claim request
			{// TODO : calculate the next three spots the robot wants to move to (towards first point in "travelPoints", to claim it at a storage agent. if the robot is able to claim a spot next to the storage agent, allowedToLeaveStorageAgent must be set to false
				//the agent will want to move to the entrance if input queue(See our map), which is 11 values left of the storage agent
				//if the agent arrives at the storage queue it wants to move towards the storage agent
				updateStorageAgents();
				ACLMessage movReq = new ACLMessage(ACLMessage.QUERY_IF);
				movReq.addReceiver(storageAgents[0]);//address only the first storage agent(could be randomnized to decrease workload), all storage agents are aware of the same map they share among them
				movReq.setContent("x,y;x,y;x,y;");
				movReq.setConversationId("mov-request");
				myAgent.send(movReq);
			}
		}
	}
	
	private class AcceptMov extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),MessageTemplate.MatchConversationId("mov-request"));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {//this agent received a YES or NO after his movement request
				String response = msg.getContent();
				if(response.contains("yes"))
				{
					isAllowedToMove=true;
				}
				else
				{
					//TODO : find a new path and new currentPathCost
					
				}
			}
			else {
				block();
			}
		}
	}
	
	private class awaitAllowanceToLeaveStorage extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),MessageTemplate.MatchConversationId("item-return"));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {//this agent received coords where to put the item back
				String response = msg.getContent();
				//TODO: Get the x,y coords from message and put this point first in his the list ot travelPoints
				isAllowedToMove=true;
			}
			else {
				block();
			}
		}
	}
	
	private class RobotAtDestionationBehaviour extends Behaviour {//one shot
		public void action() {//inform all storage agents that im near one of them
			allowedToLeaveStorageAgent=false;
			ACLMessage atStorage = new ACLMessage(ACLMessage.INFORM);
			atStorage.setConversationId("item-arrived");
			atStorage.setContent(location.x+","+location.y);
			updateStorageAgents();
			for (int i = 0; i < storageAgents.length; ++i) {
				atStorage.addReceiver(storageAgents[i]);
			} 
			
			myAgent.send(atStorage);
			done();
		}
		@Override
		public boolean done() {
			return true;
		}
	}
	
	private class MapRequest extends Behaviour {//one shot
		public void action() {//request the map from the GuiAgent
			updateGuiAgents();
			//System.out.println("I did a one shot map request");//debugging purpose
			ACLMessage mapReq = new ACLMessage(ACLMessage.QUERY_IF);
			
			mapReq.addReceiver(guiAgents[0]);
			mapReq.setContent("Give me map");
			mapReq.setConversationId("map-request");
			myAgent.send(mapReq);
			
			ACLMessage mapUpd = new ACLMessage(ACLMessage.INFORM);
			mapUpd.addReceiver(guiAgents[0]);//The gui agent needs to know that the robot actually did a move
			mapUpd.setContent(location.x+","+location.y);//put the agent on the map
			mapUpd.setConversationId("map-update");
			myAgent.send(mapUpd); 
			done();
		}
		@Override
		public boolean done() {
			return true;
		}
	}
	

	private class MapReceiver extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),MessageTemplate.MatchConversationId("map-request"));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {//this agent received a map after his map request
				String response = msg.getContent();
				//TODO : process the map string to a global stored map
			}
			else {
				block();
			}
		}
	}
	

}