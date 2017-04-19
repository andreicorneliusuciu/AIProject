package searchclient;

import java.util.ArrayList;
import java.util.List;

import searchclient.Strategy.StrategyBFS;
import searchclient.Strategy.StrategyDFS;

public class Planner {

	public Agent agent;
	public Node state; // the current state of the agents plan. NOT
						// agent.initialstate!!
	// we need the initialState of the agent to see everything but the other
	// agents goals.

	// agent contains specialised initialNode

	private static enum Type {
		MoveBoxToGoal, StoreBox, FreeAgent
	};

	public Planner(Agent agent) {

		// TODO Make simplified level where commands have immediate effects.
		// example: MoveBoxToGoal(1,A,a) moves A to a immediately.
		// Make high-level plan based on such moves.
		// example: FreeAgent(1),MoveBoxToGoal(A,a),MoveBoxToGoal(B,b),
		// StoreBox(B,Position x)
		// Take the moves, use classical heuristic methods to implement them.
		// Use custom, appropriate goalStates.
		// Make low-level plan for the agent (solution).
		// feed the result to conflict resolution class.

		this.agent = agent;

	}

	public Planner(Node initialState) // constructor just for testing
	{

		this.agent = new Agent(0, "Blue", new Position(initialState.agentRow, initialState.agentCol), initialState);
		// this.agent=agent;
		this.state = initialState;

		System.err.println("\n ///////////////////////////////// \n");
		System.err.println("Initial:" + state.boxes2.get(0));

		Node newN = MoveBoxToGoal(state, state.boxes2.get(0), state.goals2.get(0));
		System.err.println("MoveToGoal at " + state.goals2.get(0).position + " :" + newN.boxes2.get(0));

		Node newN1 = StoreBox(state, state.boxes2.get(0));
		System.err.println("StoreBox " + state.boxes2.get(0).name + " :" + newN1.boxes2.get(0));
		System.err.println("\n ///////////////////////////////// \n");

		Node newN2 = FreeAgent(state, agent);
		System.err.println("FreeAgent " + agent.name + " Node: " + newN2);
		System.err.println("\n ///////////////////////////////// \n");

		// List<Node> solutions = findHighestPlan(initialState);
		// System.err.println("Solution: " + solutions);
		// System.err.println("\n ///////////////////////////////// \n");

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
				if ((Math.abs((p.row - box.position.row) + Math.abs((p.col - box.position.col)))) < length) {
					length = (Math.abs((p.row - box.position.row) + Math.abs((p.col - box.position.col))));
					shortestPos = p;
				}
			}
		} else {
			System.err.println("No storage space available");
			// TODO what to do if no space available. Maybe move randomly.
			return newState;
		}
		/////////////////////////////////////////////////////////////////////////////

		// find the box, set its position
		if (!newState.boxes2.contains(box)) {
			System.err.println("404: Box not found. Shit");
			return null;
		} else {
			for (Box b : newState.boxes2) {

				if (b == box) {
					b.position = shortestPos;
					break;
				}
			}

		}
		return newState;
	}

	private List<Node> findHighestPlan(Agent agent) {

		// TODO Have to expand all nodes in bfs fashion till goal state is
		// reached.
		// Take the shortest solution.
		// Limit FreeAgent to only happen on trapped agents but have highest
		// priority.

		return null;
	}

	private List<Node> findHighestPlan(Node initialState) // testing only
	{

		// List<Node> solution = new ArrayList<Node>();
		Strategy strategy = new StrategyDFS();

		// strategy.addToFrontier(initialState);

		System.err.format("Search starting with strategy %s.\n", strategy.toString());
		strategy.addToFrontier(initialState);

		int iterations = 0;
		while (true) {
			if (iterations == 1000) {
				// System.err.println(strategy.searchStatus());
				iterations = 0;
			}

			if (strategy.frontierIsEmpty()) {
				return null;
			}

			Node leafNode = strategy.getAndRemoveLeaf();

			if (leafNode.isGoalState()) {
				return leafNode.extractPlan();
			}

			strategy.addToExplored(leafNode);
			for (Node n : leafNode.getExpandedNodes()) { // The list of expanded
															// nodes is shuffled
															// randomly; see
															// Node.java.
				if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
					strategy.addToFrontier(n);
				}
			}
			iterations++;
		}

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
