package searchclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import searchclient.Strategy.StrategyBFS;
import searchclient.Strategy.StrategyDFS;

public class Planner {

	public Agent agent;
	public Node state;
	// public Node trueState;// the current state of the agents plan. NOT
	// agent.initialstate!!
	// we need the initialState of the agent to see everything but the other
	// agents goals.

	List<Position> positions = new ArrayList<Position>();
	List<Node> plan = new ArrayList<Node>();
	List<Type> plantoPrint = new ArrayList<Type>();
	// public LinkedList<Node> solution = new LinkedList<Node>();
	public int trappedAgent;

	public boolean shufflePlan = false;

	private static enum Type {
		MoveBoxToGoal, StoreBox, FreeAgent, MoveToBox
	};

	public Planner(Agent theAgent) {

		positions = Heuristic.findStorage(theAgent.initialState);
		Heuristic.storageSpace = positions;

		this.agent = new Agent(theAgent);

		this.state = theAgent.initialState.Copy();
		// this.state.printGoals();

		updateGoalStates(this.state);
		////System.err.println("InitialState goals: " + state.goals2);

		////System.err.println("Boxes to give to closestbox: " +this.state.boxes2);

		// HOWTHINGSWORK highest level commands generate goalastates to
		// be achieved by normal moves.

	}

	public LinkedList<Node> findSolution(Strategy strategy) {
		LinkedList<Node> solution = new LinkedList<Node>();
		Node thePlan = findHighestPlan(this.agent, this.state); // returns node with goals properly set

		
		
		

		strategy.clearFrontier();
		

	//	System.err.println("Plan generated by findHighestPlan: "+thePlan);

		solution = implementHighPlan(thePlan, strategy);

		//System.err.println("Solution generated by findHighestPlan: " + solution);
		return solution;
	}



	private Goal findClosestFreeCelltoGoal(Goal g) {
		
		Goal newGoal = new Goal(null,null,new Position());
		
		
		
		// TODO Auto-generated method stub
		return null;
	}

	private Node MoveToRandomNearbyCell() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	private Node MoveToBox(Node node, Goal goal) {
		// make a goal state i n an actual goal so it can be achieved

		Node updateGoalState = node.Copy();

//		for (int i = 0; i < Node.MAX_ROW; i++) {
//			for (int j = 0; j < Node.MAX_COL; j++) {
//				if ('a' <= updateGoalState.goals[i][j] && updateGoalState.goals[i][j] <= 'z') {
//					updateGoalState.goals[i][j] = ' ';
//				}
//			}
//		}	
//		
//		//the goal position we want to end up in is coded as a '&'
//		updateGoalState.goals[goal.position.row][goal.position.col] = '&';
		updateGoalState.goals2 = new ArrayList<>();
		updateGoalState.isMove = true;
		updateGoalState.goals2.add(new Goal('&', "none", goal.position));
		System.err.println("AGENT " + node.theAgentName + " Has: --");
		System.err.println(updateGoalState);
		System.err.println("The goal is " + updateGoalState.goals2 + " ^^^ and ^^^ is move is " + updateGoalState.isMove); 
		
		return updateGoalState;
	}

	private Node MoveBoxToGoal(Node node, Goal goal) {
		// make a goal state i n an actual goal so it can be achieved

		Node newState = node.Copy();

		Position goalPos = null;

		// find the goal position
		if (!newState.goals2.contains(goal)) {

			//System.err.println("404: Goal not found. Shit");
			return null;
		} else {

			int goalpriority = 0;
			for (Goal g : newState.goals2) {
				if (g.name.equals(goal.name) && !g.isSatisfied) {
					//pick highest priority goal first!!!!
					if (goalpriority < g.priority) {
						goalPos = g.position;
						goalpriority = g.priority;

					}
				}
			}

		}

		//char[][] newGoalState = newState.goals;
		char[][] newGoalState = new char[Node.MAX_ROW][node.MAX_COL];//node.goals;

		for (int row = 0; row < Node.MAX_ROW; row++) {
			for (int col = 0; col < Node.MAX_COL; col++) {
				newGoalState[row][col] = newState.goals[row][col];

			}
		}

		for (int i = 0; i < Node.MAX_ROW; i++)
			for (int j = 0; j < Node.MAX_COL; j++) {
				if ('a' <= newGoalState[i][j] && newGoalState[i][j] <= 'z') {
					newGoalState[i][j] = 0;
				}
			}

		// fill it with the new goals bellow

		// name it a random goal name of those the agent has
		newGoalState[goalPos.row][goalPos.col] = node.goals2.get(0).name;

		// put in the newstate the new goals
		newState.goals = newGoalState;

		////System.err.println("Goal made in: " + goalPos);

		return newState;

	}

	private Node FreeAgent(Node node, Agent agent) {
		// look if the agent has isTrapped = true. Move same
		// colored boxes to storage by turning storage into goal state and
		// erasing existing goal states
		// Always calls StoreBox()

		Node newState = node.Copy();
		// char[][] newGoalState = null;

		if (!agent.isTrapped) {

			return null;
		}

		trappedAgent = agent.name;

		List<Box> blockingBoxes = new ArrayList<Box>();

		// find blocking boxes in node
		for (Box b : newState.boxes2) {
			if (b.isBlocking) {
				blockingBoxes.add(b);
				////System.err.println("blockingboxes: " + blockingBoxes);
			}
		}

		if (!blockingBoxes.isEmpty()) {
			for (Box b : blockingBoxes) {
				// the new node with the goal set to where the boxes should be
				// stored

				newState = StoreBox(newState, b); // problem when we have many
													// boxes of the same color

				if (newState != null) {
					break;
				}
				// //System.err.println("Newstate generated by StoreBoxes: " +
				// newState);
				// break;

			}
		}
		return newState;
	}

	private Node StoreBox(Node node, Box box) {
		// TODO if no isStorage cells exist ?

		Node newState = node.Copy();

		float length = 1000;
		Position shortestPos = null;

		// find shortest manhattan distance available position

		if (!Heuristic.storageSpace.isEmpty()) {
			for (Position p : Heuristic.storageSpace) {
				if (Position.manhattanDistance(p, box.position) < length && !p.equals(box.position)) {
					length = (Math.abs((p.row - box.position.row) + Math.abs((p.col - box.position.col))));
					shortestPos = p;
				}
			}

		} else {
			//System.err.println("No storage space available");
			// TODO what to do if no space available. Maybe move randomly.
			return null;

		}

		// find the box, set its position

		char[][] newGoalState = new char[Node.MAX_ROW][node.MAX_COL];//node.goals;

		for (int row = 0; row < Node.MAX_ROW; row++) {
			for (int col = 0; col < Node.MAX_COL; col++) {
				newGoalState[row][col] = node.goals[row][col];

			}
		}

		// empty the array
		for (int i = 0; i < Node.MAX_ROW; i++)
			for (int j = 0; j < Node.MAX_COL; j++) {
				if ('a' <= newGoalState[i][j] && newGoalState[i][j] <= 'z') {
					////System.err.println("moar " + newGoalState[i][j]);
					newGoalState[i][j] = 0;
					////System.err.println("moar2 " + newGoalState[i][j]);
				}
			}

		// fill it with the new goals bellow

		// boolean flag = false;
		if (!newState.boxes2.contains(box)) {
			//System.err.println("404: Box not found in storebox. Shit");
			return null;
		} else {
			newGoalState[shortestPos.row][shortestPos.col] = node.goals2.get(0).name;
			//remove spot from storagespace
			Heuristic.storageSpace.remove(shortestPos);
			length = 1000;
		}

		//System.err.println("Goal made in: " + shortestPos);

		// keeping these just in case, erase if cleaning
		// plan.add(newState);
		plantoPrint.add(Type.StoreBox);

		// this is the new node with the updated goalstate

		newState.goals = newGoalState;

		return newState;
	}

	private Node findHighestPlan(Agent theAgent, Node currentState) {

		Node thisCurrentState = currentState.Copy();

		// use that if needed, clean if not. Clean the below error too.
		// char[][] theGoalState = null;
		// System.err.println("Currentstate goals: "+currentState.goals2);

		// TODO Create highest plan, take first highest action, create and
		// return its goalState for further implementation
		for (Agent a : SearchClient.agents) {

			if (a.isTrapped && a.name != theAgent.name) {

				Box blockingBox = findClosestBlockingBox(a, thisCurrentState);

				// System.err.println(blockingBox);
				if (blockingBox != null) {

					if (blockingBox.color.equals(agent.color)) {
						// theGoalState = FreeAgent(currentState, a);

						// plan.clear();
						plan.add(FreeAgent(thisCurrentState, a));
						//System.err.println("Each plan for, freeAgent " + plan);

					}
					// System.err.println("=================>>>>shitt agents: "
					// + SearchClient.agents);

					plantoPrint.add(Type.FreeAgent);
					// System.err.println("GoalState for freeAgent:"+plan);

					break; // only free one agent at a time, then this
							// agent.isTrapped will be false and goes to next
							// one
				}
			} else if (!a.isTrapped) {
				System.err.println("Agent " + a.name + " not trapped.");
			}
		}

		// System.err.println("Goals megoals:" + currentState.goals2+"
		// goals[][]= "+currentState.theAgentColor);

		// TODO find closest box-goal distance and satisfy that first
		
		for (Goal g : thisCurrentState.goals2) {
			Box aBox = null;

			//System.err.println("all goals in currentstate planner: "+thisCurrentState.goals2);
			//System.err.println("Goal :" + g.name + " ," + g.color + " issatisfied: " + g.isSatisfied);

			if (!g.isSatisfied && g.color == theAgent.color) {
				//System.err.println("Goal accepted :" + g.name);

				aBox = findClosestBox(theAgent, thisCurrentState, g);


				plan.add(MoveBoxToGoal(thisCurrentState, g));
				plantoPrint.add(Type.MoveBoxToGoal);

			}
		}

		// Object plantoString;
		System.err.println("HighestPlan: " + plantoPrint + " made by agent: " + theAgent);

		/// System.err.println(" ");
//			for (Node na : plan)
//			System.err.println("Plan selected: " + na.toString());
		return plan.get(0);

	}

	public LinkedList<Node> implementHighPlan(Node aInitialNode, Strategy strategy) {
		
		//Node goalState = null;
		Node initialNode = null;
		//goalState = aGoalState.Copy();
		initialNode = aInitialNode.Copy();
		
		//System.err.println("InitialNode generated by findHighestPlan in implement: "+initialNode + " for agent" + agent);

		
		
		System.err.format("Search starting with strategy %s.\n", strategy.toString());
		// strategy.clearFrontier();
		//System.err.println("Is frontier empty: " + strategy.frontierIsEmpty());
		strategy.addToFrontier(initialNode);

		int iterations = 0;
		while (true) {
			if (iterations == 1000) {
				// System.err.println(strategy.searchStatus());
				iterations = 0;
			}

			if (strategy.frontierIsEmpty()) {

				LinkedList<Node> noOpList = new LinkedList<Node>();
				initialNode.doNoOp = true;
				noOpList.add(initialNode);
				System.err.println("The frontier is empty");
				return noOpList;

			}

			Node leafNode = strategy.getAndRemoveLeaf();

			if (leafNode.isGoalState()) {
			
				//System.err.println("ExtractingPlan: " + leafNode.extractPlan());

				return leafNode.extractPlan();
			}

			strategy.addToExplored(leafNode);
			for (Node n : leafNode.getExpandedNodes()) { // The list of expanded
															// nodes is shuffled
															// randomly; see
															// Node.java.
				if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
					// System.err.println("Adding to frontier:
					// "+n.theAgentName+" "+n);
					strategy.addToFrontier(n);
				}
			}
			iterations++;
		}

	}


	//

	private Box findClosestBlockingBox(Agent trappedAgent, Node theNode) {

		Node node = null;
		node = theNode.Copy();

		if (agent == null) {
			//System.err.println("Null agent when going into findClosestBox in Planner");
		}

		Box box = null;
		// Position agentPos = trappedAgent.position;
		float minDistance = 10000;

		for (Box b : node.boxes2) {

			if (b.color == agent.color && b.isBlocking && Position.manhattanDistance(trappedAgent.position, b.position) < minDistance) {
				minDistance = Position.manhattanDistance(trappedAgent.position, b.position);
				box = b;

				// //System.err.println("Box found for agent "+trappedAgent+" box
				// is: "+box);
			}
		}

		if (box == null)
			System.err.println("No blocking Box found");

		return box;
	}

	private Box findClosestBox(Agent thisAgent, Node theNode, Goal theGoal) // of same color
	{

		Node node = null;
		node = theNode.Copy();

		if (thisAgent == null) {
			//System.err.println("Null agent when going into findClosestBox in Planner");
		}

		Box box = null;
		// Position agentPos = trappedAgent.position;
		float minDistance = 10000;

		for (Box b : node.boxes2) {


			//pick box closer to goal instead
			if (b.color.equals(thisAgent.color) && Position.manhattanDistance(theGoal.position, b.position) < minDistance) {
				minDistance = Position.manhattanDistance(theGoal.position, b.position);
				box = b;
			}
		}

		if (box == null)
			System.err.println("No Box found for agent " + agent);

		////System.err.println("Box found at "+box);
		return box;
	}

	// updates the status of all goals for a given node (agent sees and updates
	// only his own goals!!!)
	public void updateGoalStates(Node node) {
		List<Goal> goals = node.goals2;
		List<Box> boxes = node.boxes2;

		for (int row = 1; row < Node.MAX_ROW; row++) {
			for (int col = 1; col < Node.MAX_COL; col++) {
				char g = node.goals[row][col];
				char b = Character.toLowerCase(node.boxes[row][col]);
				// if (g > 0 && b != g) {
				//
				// }
				// satisfy goals
				if (g > 0) {
					for (Goal gl : goals) {
						if (gl.name == g && gl.position.equals(new Position(row, col))) {
							if (b == g) {
								gl.isSatisfied = true;

							} else if (b == 0) {
								gl.isSatisfied = false;
							}
						}
					}
				}
			}
		}

		node.goals2 = goals;
		node.boxes2 = boxes;

	}

	// checks if the goals for a given node are satisfied (agent sees only his
	// own goals!!!)
	private boolean isGoalState(Node node) {
		// TODO set isSatisfied properly
		for (Goal g : node.goals2) {
			if (!(g.isSatisfied == true)) {
				return false;
			}
		}

		return true;
	}

	public Type getFreeAgent() {
		// Auto-generated method stub
		return Type.FreeAgent;
	}

}