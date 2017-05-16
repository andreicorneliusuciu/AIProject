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
	public boolean noPlan = false;

	public boolean shufflePlan = false;

	private static enum Type {
		MoveBoxToGoal, StoreBox, FreeAgent, MoveToBox
	};

	public Planner(Agent theAgent) {

		positions = Heuristic.findStorage(theAgent.initialState);
		Heuristic.storageSpace = positions;

		this.agent = theAgent;

		this.state = theAgent.initialState.Copy();
		// this.state.printGoals();

		updateGoalStates(this.state);
		//// System.err.println("InitialState goals: " + state.goals2);

		//// System.err.println("Boxes to give to closestbox: "
		//// +this.state.boxes2);

		// HOWTHINGSWORK highest level commands generate goalastates to
		// be achieved by normal moves.

	}

	public LinkedList<Node> findSolution(Strategy strategy) {
		LinkedList<Node> solution = new LinkedList<Node>();
		Node thePlan = findHighestPlan(this.agent, this.state); // returns node with goals properly set
		strategy.clearFrontier();

		if(!this.noPlan){
		solution = implementHighPlan(thePlan, strategy);

		System.err.println("Solution generated by findHighestPlan: " + solution);
		} else {
			state.doNoOp = true;
			solution.add(state);
		}
		return solution;
	}

	private Position findClosestFreeCelltoBox(Box b) {

		Position newPos = new Position(b.position.row, b.position.col);

		System.err.println("AGENT SHEET: " + agent + "      " + newPos.toString());
		if (!SearchClient.walls[newPos.row - 1][newPos.col]) {
			newPos.row = newPos.row - 1;
			newPos.col = newPos.col;
		} else if (!SearchClient.walls[newPos.row + 1][newPos.col]) {
			newPos.row = newPos.row + 1;
			newPos.col = newPos.col;
		} else if (!SearchClient.walls[newPos.row][newPos.col + 1]) {
			newPos.row = newPos.row;
			newPos.col = newPos.col + 1;
		} else if (!SearchClient.walls[newPos.row - 1][newPos.col - 1]) {
			newPos.row = newPos.row;
			newPos.col = newPos.col - 1;
		}

		return newPos;
	}

	private Node MoveToRandomNearbyCell() {
		// TODO Auto-generated method stub
		return null;
	}

	private Node MoveToBox(Node node, Position pos) {
		// make a goal state i n an actual goal so it can be achieved

		Node updateGoalState = node.Copy();

		for (int i = 0; i < Node.MAX_ROW; i++) {
			for (int j = 0; j < Node.MAX_COL; j++) {
				if ('a' <= updateGoalState.goals[i][j] && updateGoalState.goals[i][j] <= 'z') {
					updateGoalState.goals[i][j] = 0;
				}
			}
		}
		//
		// //the goal position we want to end up in is coded as a '&'
		// updateGoalState.goals[goal.position.row][goal.position.col] = '&';
		updateGoalState.goals2 = new ArrayList<>();
		updateGoalState.isMove = true;
		updateGoalState.goals2.add(new Goal('&', "none", pos));
		System.err.println("AGENT " + node.theAgentName + " Has: --");
		System.err.println(updateGoalState);
		System.err.println("The goal is " + updateGoalState.goals2 + " ^^^ and ^^^ is move is " + updateGoalState.isMove);

		return updateGoalState;
	}

	private Node MoveBoxToGoal(Node node, Goal goal) {
		// make a goal state i n an actual goal so it can be achieved

		Node newState = node.Copy();
		newState.isMove = false;
		Position goalPos = null;
		char goalName = 0;

		// find the goal position
		if (!newState.goals2.contains(goal)) {

			// System.err.println("404: Goal not found. Shit");
			return null;
		} else {
			int goalpriority = Integer.MAX_VALUE;
			for (Goal g : newState.goals2) {
				if (g.name == goal.name && !g.isSatisfied) {
					// pick highest priority goal first!!!!
					if (goalpriority > g.priority*2+DistancesComputer.getDistanceBetween2Positions(new Position(node.agentRow,node.agentCol), g.position)) {
						goalPos = g.position;
						goalpriority = g.priority*2+DistancesComputer.getDistanceBetween2Positions(new Position(node.agentRow,node.agentCol), g.position);
						goalName = g.name;
						System.err.println(g.name);
					}
				}
			}

		}

		// char[][] newGoalState = newState.goals;
		char[][] newGoalState = new char[Node.MAX_ROW][node.MAX_COL];// node.goals;

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

		newGoalState[goalPos.row][goalPos.col] = goalName;

		// put in the newstate the new goals
		newState.goals = newGoalState;

		//// System.err.println("Goal made in: " + goalPos);

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
				blockingBoxes.add(new Box(b));
				//// System.err.println("blockingboxes: " + blockingBoxes);
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

		char goalName = Character.toLowerCase(box.name);

		// find shortest manhattan distance available position

		if (!Heuristic.storageSpace.isEmpty()) {
			for (Position p : Heuristic.storageSpace) {
				if (Position.manhattanDistance(p, box.position) < length && !p.equals(box.position)) {
					length = (Math.abs((p.row - box.position.row) + Math.abs((p.col - box.position.col))));
					shortestPos = p;
				}
			}

		} else {
			// System.err.println("No storage space available");
			// TODO what to do if no space available. Maybe move randomly.
			return null;

		}

		// find the box, set its position

		char[][] newGoalState = new char[Node.MAX_ROW][node.MAX_COL];// node.goals;

		for (int row = 0; row < Node.MAX_ROW; row++) {
			for (int col = 0; col < Node.MAX_COL; col++) {
				newGoalState[row][col] = node.goals[row][col];

			}
		}

		// empty the array
		for (int i = 0; i < Node.MAX_ROW; i++)
			for (int j = 0; j < Node.MAX_COL; j++) {
				if ('a' <= newGoalState[i][j] && newGoalState[i][j] <= 'z') {
					//// System.err.println("moar " + newGoalState[i][j]);
					newGoalState[i][j] = 0;
					//// System.err.println("moar2 " + newGoalState[i][j]);
				}
			}

		// fill it with the new goals bellow

		// boolean flag = false;
		if (!newState.boxes2.contains(box)) {
			// System.err.println("404: Box not found in storebox. Shit");
			return null;
		} else {
			newGoalState[shortestPos.row][shortestPos.col] = goalName;
			//remove spot from storagespace

			Heuristic.storageSpace.remove(shortestPos);
			length = 1000;
		}

		// System.err.println("Goal made in: " + shortestPos);

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
		int chosenPriority = Integer.MAX_VALUE;
		Goal chosen = null;
		System.err.println(" POSITION OF CHOSEN GOAL");
		for(Goal g : thisCurrentState.goals2){
			System.err.println(g.isSatisfied + " : " + g.position);
		}
		for(Goal g : thisCurrentState.goals2){
			System.err.println("LOOK HERE");
			System.err.println(g.position);
			System.err.println(g.assigned);
			System.err.println(g.isSatisfied);
			System.err.println(g.color);
		}
		System.err.println("Ac: " +theAgent.color);
		for(Goal g2 : SearchClient.allGoals){
			for (Goal g : thisCurrentState.goals2) {
				if(g.position.equals(g2.position)){
					if(chosenPriority > g.priority*2+DistancesComputer.getDistanceBetween2Positions(this.agent.position, g.position) && !g.isSatisfied && g2.color.equals(theAgent.color) && !g2.assigned){
						chosenPriority = g.priority*2+DistancesComputer.getDistanceBetween2Positions(this.agent.position, g.position);
						chosen = g;
						System.err.println("LH2");
					}
				}
			}
		}
		if(chosen != null){
			chosen.assigned = true;
			theAgent.assignedChar = chosen.name;
			thisCurrentState.nameOfGoal = chosen.name;
			thisCurrentState.goals2 = new ArrayList<Goal>();
			thisCurrentState.goals2.add(chosen);
			System.err.println("CHOSEN VALUES");
			System.err.println(chosen.position);
			System.err.println(chosen.name);
			System.err.println(agent.name);
		}
		
		
		//System.err.println("all goals in currentstate planner: "+thisCurrentState.goals2);
		//System.err.println("Goal :" + g.name + " ," + g.color + " issatisfied: " + g.isSatisfied);
		//System.println(thisCurrentState.goals2.size());
		if(chosen == null){
			System.err.println("NO CHOSEN FOR : " + theAgent.name);
		}
		else if (!chosen.isSatisfied /*&& chosen.color.equals(theAgent.color)*/) {
				//System.err.println("Goal accepted :" + g.name);

				Box b = null;
				for (Box box : thisCurrentState.boxes2) {
					System.err.println("feeggt" + box.isOnOwnGoal());
					if (box.color.equals(agent.color) && !box.isOnOwnGoal()) {
						b = new Box(box);
						break;
					}

				}

				System.err.println("Box picked: " + b);

//				System.err.println("lort:    " + theAgent.name + "       " + agent.position);
//				if (DistancesComputer.getDistanceBetween2Positions(theAgent.position, b.position) > 1) {
//
//					Position pos = findClosestFreeCelltoBox(b);
//
//					plan.add(MoveToBox(thisCurrentState, pos));
//					plantoPrint.add(Type.MoveToBox);
//
//					// System.err.println("Andreis piece of shit plan
//					// "+MoveToBox(thisCurrentState, new Goal('&', "none", new
//					// Position(1, 1))));
//					// thisCurrentState.agentRow = pos.row;
//					// thisCurrentState.agentCol = pos.col;
//
//				} else {
					theAgent.initialState.goals2 = new ArrayList<Goal>();
					theAgent.initialState.goals2.add(chosen);
					thisCurrentState.goals2 = new ArrayList<Goal>();
					thisCurrentState.goals2.add(chosen);
					System.err.println(MoveBoxToGoal(thisCurrentState,chosen));
							plan.add(MoveBoxToGoal(thisCurrentState, chosen));
					// plan.add(MoveToBox(thisCurrentState, new Goal('&',
					// "none",
					// new Position(g.position.row, 1))));
					plantoPrint.add(Type.MoveBoxToGoal);

				//}

			
		}
		// Object plantoString;
		System.err.println("HighestPlan: " + plantoPrint + " made by agent: " + theAgent);

		/// System.err.println(" ");
		//			for (Node na : plan)
		//			System.err.println("Plan selected: " + na.toString());
		if (plan.size() > 0) {
			//System.err.println(plan.get(0));
			//System.err.println("DERP");
			theAgent.initialState = plan.get(0);
			return plan.get(0);
		} else {
			this.noPlan = true;
			Node temN = currentState.Copy();
			temN.doNoOp = true;
			temN.parent = null;
			return temN;
		}

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
				/*LinkedList<Node> noOpList = new LinkedList<Node>();
				initialNode.doNoOp = true;
				noOpList.add(initialNode);
				return noOpList;*/
			}

			if (strategy.frontierIsEmpty() || strategy.explored.size()>5000) {

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
			// System.err.println("Null agent when going into findClosestBox in
			// Planner");
		}

		Box box = null;
		// Position agentPos = trappedAgent.position;
		float minDistance = 10000;

		for (Box b : node.boxes2) {

			if (b.color == agent.color && b.isBlocking && Position.manhattanDistance(trappedAgent.position, b.position) < minDistance) {
				minDistance = Position.manhattanDistance(trappedAgent.position, b.position);
				box = new Box(b);

				// //System.err.println("Box found for agent "+trappedAgent+"
				// box
				// is: "+box);
			}
		}

		if (box == null)
			System.err.println("No blocking Box found");

		return box;
	}

	private Box findClosestBox(Agent thisAgent, Node theNode, Goal theGoal) // of
																			// same
																			// color
	{

		Node node = null;
		node = theNode.Copy();

		if (thisAgent == null) {
			// System.err.println("Null agent when going into findClosestBox in
			// Planner");
		}

		Box box = null;
		// Position agentPos = trappedAgent.position;
		float minDistance = 10000;

		for (Box b : node.boxes2) {

			// pick box closer to goal instead
			if (b.color.equals(thisAgent.color) && Position.manhattanDistance(theGoal.position, b.position) < minDistance) {
				minDistance = Position.manhattanDistance(theGoal.position, b.position);
				box = new Box(b);
			}
		}

		if (box == null)
			System.err.println("No Box found for agent " + agent);

		//// System.err.println("Box found at "+box);
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
		node.goals2 = new ArrayList<Goal>();
		for(Goal g : SearchClient.allGoals){
			if(g.color.equals(agent.color)){
				node.goals2.add(g);
			}
		}
		//node.goals2 = goals;
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