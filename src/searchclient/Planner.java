package searchclient;

import java.util.ArrayList;
import java.util.HashMap;
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

	private static enum Type {
		MoveBoxToGoal, StoreBox, FreeAgent
	};

	public Planner(Agent theAgent) {

		System.err.println("FindStorage for");
		System.err.println(theAgent.initialState);

		positions = Heuristic.findStorage(theAgent.initialState);
		Heuristic.storageSpace = positions;
		System.err.println(positions.toString());

		// just debugging
		StringBuilder s = new StringBuilder();
		boolean x = false;

		for (int row = 0; row < theAgent.initialState.MAX_ROW; row++) {
			if (!SearchClient.walls[row][0]) {
				break;
			}
			for (int col = 0; col < theAgent.initialState.MAX_COL; col++) {

				if (theAgent.initialState.boxes[row][col] > 0) {
					s.append(theAgent.initialState.boxes[row][col]);
				} else if (theAgent.initialState.goals[row][col] > 0) {
					s.append(theAgent.initialState.goals[row][col]);
				} else if (SearchClient.walls[row][col]) {
					s.append("+");
				} else if (row == theAgent.initialState.agentRow && col == theAgent.initialState.agentCol) {
					s.append("0");
				} else {

					for (Position p : positions) {
						if (p.row == row && p.col == col) {
							s.append("o");
							x = true;
						}
					}
					if (!x) {
						s.append(" ");
					}
					x = false;
				}

			}
			s.append("\n");
		}

		System.err.println(s);
		////////////////////// debugabove//////////////////////////////////////

		// TODO Make simplified level where commands have immediate effects.
		// example: MoveBoxToGoal(1,A,a) moves A to a immediately.
		// Make high-level plan based on such moves.
		// example: FreeAgent(1),MoveBoxToGoal(A,a),MoveBoxToGoal(B,b),
		// StoreBox(B,Position x)
		// Take the moves, use classical heuristic methods to implement them.
		// Use custom, appropriate goalStates.
		// Make low-level plan for the agent (solution).
		// feed the result to conflict resolution class.

		this.agent = theAgent;

		this.state = agent.initialState;

		// TODO , HOWTHINGSWORK highest level commands generate goalastates to
		// be achieved by normal moves.

		findHighestPlan(agent, state);
		
		

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
		// colored boxes to storage
		// Always calls StoreBox()

		Node newState = node;

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
			return newState;

		}

		// find the box, set its position

		boolean flag = false;
		if (!newState.boxes2.contains(box)) {
			System.err.println("404: Box not found. Shit");
			return null;
		} else {
			for (Box b : newState.boxes2) {

				if (b == box) {
					System.err.println("Box moved: " + b + " from: " + b.position);
					b.position = shortestPos;
					System.err.println("to: " + b.position);

					break;
				}
			}

			plan.add(newState);
			plantoPrint.add(Type.StoreBox);

		}

		return newState;
	}

	private List<Node> findHighestPlan(Agent theAgent, Node currentState) {

		// TODO Have to expand all nodes in bfs fashion till goal state is
		// reached.
		// Take the shortest solution.
		// Limit FreeAgent to only happen on trapped agents but have highest
		// priority.
		for (Agent a : SearchClient.agents) {

			if (a.isTrapped == true && a.name != theAgent.name) {

				Box blockingBox = findClosestBlockingBox(a, currentState);

				if (blockingBox != null) {
					if (blockingBox.color == agent.color) {
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

			System.err.println("Goal :" + g.name + " ," + g.color + " is satisfied: " + g.isSatisfied);

			if (!g.isSatisfied && g.color == theAgent.color) {
				System.err.println("Goal accepted :" + g.name);
				aBox = findClosestBox(theAgent, currentState);
				plan.add(MoveBoxToGoal(currentState, aBox, g));
				plantoPrint.add(Type.MoveBoxToGoal);

			}
		}

		// Object plantoString;
		System.err.println("HighestPlan: " + plantoPrint + " made by agent: " + theAgent);

		/// System.err.println(" ");

		return plan;
	}

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

				// System.err.println("Box found for agent "+trappedAgent+" ,
				// box is: "+box);
			}
		}

		if (box == null)
			System.err.println("No blocking Box found");

		return box;
	}

	private Box findClosestBox(Agent thisAgent, Node node) // of same color
	{

		if (thisAgent == null) {
			System.err.println("Null agent when going into findClosestBox in Planner");
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
		List<Goal> goals = node.goals2;
		List<Box> boxes = node.boxes2;

		for (Goal g : goals) {
			for (Box b : boxes) {
				if (b.position == g.position && Character.toLowerCase(b.name) == g.name) {
					g.isSatisfied = true;
				}

			}
		}
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