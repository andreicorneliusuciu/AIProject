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
	public Node state; // the current state of the agents plan. NOT
						// agent.initialstate!!
	// we need the initialState of the agent to see everything but the other
	// agents goals.

	List<Position> positions = new ArrayList<Position>();
	List<Node> plan = new ArrayList<Node>();
	List<Type> plantoPrint = new ArrayList<Type>();
	public LinkedList<Node> solution = new LinkedList<Node>();

	private static enum Type {
		MoveBoxToGoal, StoreBox, FreeAgent
	};

	public Planner(Agent theAgent) {

		// System.err.println("FindStorage for");
		// System.err.println(theAgent.initialState);

		positions = Heuristic.findStorage(theAgent.initialState);
		Heuristic.storageSpace = positions;
		//System.err.println(positions.toString());

		this.agent = theAgent;

		this.state = agent.initialState;

		// HOWTHINGSWORK highest level commands generate goalastates to
		// be achieved by normal moves.

		// TODO make this work and print shit and call implementighestPlan after
		// it
		Node thePlan = findHighestPlan(agent, state); // returns goal state
		solution = implementHighPlan(this.state, thePlan, new StrategyBFS());
		//System.err.println("HighestSolution: " + solution);

	}

	private Node MoveBoxToGoal(Node node, Box box, Goal goal) {
		// find box and goal in the node and update their values

		Node newState = node;

		Position goalPos = null;

		// find the goal position
		if (!newState.goals2.contains(goal)) {
			System.err.println("404: Goal not found. Shit");
			return null;
		} else {
			for (Goal b : newState.goals2) {

				if (b == goal) {
					goalPos = b.position;
					break;
				}
			}

		}
		// find the box, set its position to goalPosition
		if (!newState.boxes2.contains(box)) {
			System.err.println("404: Box not found. Shit");
			return null;
		} else {
			for (Box b : newState.boxes2) {

				if (b == box) {
					b.position = goalPos;
					break;
				}
			}

		}

		return newState;
	}

	private Node FreeAgent(Node node, Agent agent) {
		// look if the agent has isTrapped = true. Move same
		// colored boxes to storage by turning storage into goal state and
		// erasing existing goal states
		// Always calls StoreBox()

		Node newState = node;
		// char[][] newGoalState = null;

		if (!agent.isTrapped) {
			return null;
		}

		List<Box> blockingBoxes = new ArrayList<Box>();

		// find blocking boxes in node
		for (Box b : newState.boxes2) {
			if (b.isBlocking) {
				blockingBoxes.add(b);
			}
		}

		if (!blockingBoxes.isEmpty()) {
			for (Box b : blockingBoxes) {
				// the new node with the goal set to where the boxes should be
				// stored
				newState = StoreBox(newState, b);

			}
		}

		return newState;
	}

	private Node StoreBox(Node node, Box box) {
		// TODO if no isStorage cells exist ?

		Node newState = node;

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
			System.err.println("No storage space available");
			// TODO what to do if no space available. Maybe move randomly.
			return null;

		}

		// find the box, set its position

		char[][] newGoalState = node.goals.clone();

		// empty the array
		for (Goal g : node.goals2) {

			newGoalState[g.position.row][g.position.col] = 0;
		}

		// fill it with the new goals bellow

		// boolean flag = false;
		if (!newState.boxes2.contains(box)) {
			System.err.println("404: Box not found. Shit");
			return null;
		} else {
			// for (Box b : newState.boxes2) {
			//
			// if (b.equals(box)) {
			// b.position = shortestPos;
			// //System.err.println("to: " + b.position);
			//
			// break;
			// }
			// name it a random goal name of those the agent has
			newGoalState[shortestPos.row][shortestPos.col] = node.goals2.peek().name; //was got(0)
		}
		//System.err.println("Goal made in: " + shortestPos);

		// keeping these just in case, erase if cleaning
		plan.add(newState);
		plantoPrint.add(Type.StoreBox);

		// this is the new node with the updated goalstate
		newState.goals = newGoalState;

		return newState;
	}

	private Node findHighestPlan(Agent theAgent, Node currentState) {

		// use that if needed, clean if not. Clean the below error too.
		char[][] theGoalState = null;

		// TODO Create highest plan, take first highest action, create and
		// return its goalState for further implementation
		for (Agent a : SearchClient.agents) {

			if (a.isTrapped == true && a.name != theAgent.name) {

				Box blockingBox = findClosestBlockingBox(a, currentState);

				if (blockingBox != null) {
					if (blockingBox.color == agent.color) {
						// theGoalState = FreeAgent(currentState, a);

						plan.add(FreeAgent(currentState, a));
					}
					plantoPrint.add(Type.FreeAgent);
					break; // only free one agent at a time, then this
							// agent.isTrapped will be false and goes to next
							// one
				}
			}
		}

		// TODO find closest box-goal distance and satisfy that first
		for (Goal g : currentState.goals2) {
			Box aBox = null;

			//System.err.println("Goal :" + g.name + " ," + g.color + " is satisfied: " + g.isSatisfied);

			if (!g.isSatisfied && g.color == theAgent.color) {
				//System.err.println("Goal accepted :" + g.name);
				aBox = findClosestBox(theAgent, currentState);

				theGoalState = MoveBoxToGoal(currentState, aBox, g).goals;
				plan.add(MoveBoxToGoal(currentState, aBox, g));
				plantoPrint.add(Type.MoveBoxToGoal);

			}
		}

		// Object plantoString;
		//System.err.println("HighestPlan: " + plantoPrint + " made by agent: " + theAgent);

		/// System.err.println(" ");

		return plan.get(0);
	}

	public LinkedList<Node> implementHighPlan(Node goalState, Node initialNode, Strategy strategy) {
		System.err.format("{Planner} Search starting with strategy %s.\n", strategy.toString());
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
			// System.err.println("Leafn" + leafNode + leafNode.parent);

			if (leafNode.isGoalState(goalState)) {
				//System.err.println("Returns" + leafNode.extractPlan());
				return leafNode.extractPlan();
			}

			strategy.addToExplored(leafNode);
//			for (Node n : leafNode.getExpandedNodes()) { // The list of expanded
//															// nodes is shuffled
//															// randomly; see
//															// Node.java.
//				if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
//					// System.err.println("Adding to frontier:
//					// "+n.theAgentName+" "+n);
//					strategy.addToFrontier(n);
//				}
//			}
			iterations++;
		}
	}

	//

	private Box findClosestBlockingBox(Agent trappedAgent, Node node) {

		if (agent == null) {
			System.err.println("Null agent when going into findClosestBox in Planner");
		}

		Box box = null;
		// Position agentPos = trappedAgent.position;
		float minDistance = 10000;

		for (Box b : SearchClient.allBoxes) {

			if (b.color == agent.color && b.isBlocking
					&& Position.manhattanDistance(trappedAgent.position, b.position) < minDistance) {
				minDistance = Position.manhattanDistance(trappedAgent.position, b.position);
				box = b;

				// System.err.println("Box found for agent "+trappedAgent+" box
				// is: "+box);
			}
		}

		if (box == null) {
			//System.err.println("No blocking Box found");
		}
		
		return box;
	}

	private Box findClosestBox(Agent thisAgent, Node node) // of same color
	{

		if (thisAgent == null) {
			//System.err.println("Null agent when going into findClosestBox in Planner");
		}

		Box box = null;
		// Position agentPos = trappedAgent.position;
		float minDistance = 10000;

		for (Box b : node.boxes2) {

			if (b.color == thisAgent.color
					&& Position.manhattanDistance(thisAgent.position, b.position) < minDistance) {
				minDistance = Position.manhattanDistance(thisAgent.position, b.position);
				box = b;

				// System.err.println("Box found for agent "+agent+" , box is:
				// "+box);
			}
		}

		if (box == null)
			System.err.println("No Box found");

		return box;
	}

	// updates the status of all goals for a given node (agent sees and updates
	// only his own goals!!!)
	private void updateGoalStates(Node node) {
//		List<Goal> goals = node.goals2;
//		List<Box> boxes = node.boxes2;
//
//		for (Goal g : goals) {
//			for (Box b : boxes) {
//				if (b.position == g.position && Character.toLowerCase(b.name) == g.name) {
//					g.isSatisfied = true;
//				}
//
//			}
//		}
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

}