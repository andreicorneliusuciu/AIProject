package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import searchclient.Command.Type;

public class Node {
	private static final Random RND = new Random(1);

	public static int MAX_ROW;
	public static int MAX_COL;

	public int agentRow;
	public int agentCol;
	public int theAgentName; // = new Agent(0,null);
	public String theAgentColor;
	public Set<Box> myBoxesFinal = new TreeSet<>();


	// public List<Agent> agents;

	public boolean doNoOp = false;

	public char[][] boxes;
	public char[][] goals;

	// alphabetially sorted both lists
	public List<Goal> goals2 = new ArrayList<>();
	public List<Box> boxes2 = new ArrayList<>();

	public List<Agent> agents = SearchClient.agents;

	public List<Box> myBoxes = new ArrayList<>();

	// list of storagepositions filled in heuristics
	public List<Position> storagePos = new ArrayList<Position>();

	public Node parent;
	public Command action;

	private int g;

	private int _hash = 0;

	public Node(Node parent, int maxRow, int maxCol) {

		this.parent = parent;

		MAX_ROW = maxRow;
		MAX_COL = maxCol;
		boxes = new char[MAX_ROW][MAX_COL];
		goals = new char[MAX_ROW][MAX_COL];
		if (parent == null) {
			this.g = 0;
		} else {
			this.g = parent.g() + 1;
		}
	}

	public Node Copy() {

		Node copy = new Node(null, Node.MAX_ROW, Node.MAX_COL);

		copy.parent = this.parent;
		copy.agentRow = this.agentRow;
		copy.agentCol = this.agentCol;
		copy.theAgentName = this.theAgentName;
		copy.theAgentColor = this.theAgentColor;
		// copy.boxes = this.boxes;
		copy.boxes2 = new ArrayList(this.boxes2);
		// copy.goals = this.goals;
		copy.goals2 = new ArrayList(this.goals2);
		copy.myBoxes = new ArrayList(this.myBoxes);
		copy.action = this.action;
		copy.doNoOp = this.doNoOp;
		// copy._hash =this._hash;
		copy.g = this.g;
		for (int row = 0; row < MAX_ROW; row++) {
			System.arraycopy(SearchClient.walls[row], 0, SearchClient.walls[row], 0, MAX_COL);
			System.arraycopy(this.boxes[row], 0, copy.boxes[row], 0, MAX_COL);
			System.arraycopy(this.goals[row], 0, copy.goals[row], 0, MAX_COL);
		}

		return copy;
	}

	public void clearGoals() {

		goals2.clear();
	}

	public void clearBoxes() {

		boxes2.clear();
	}

	public int g() {
		return this.g;
	}

	public boolean isInitialState() {
		return this.parent == null;
	}

	public boolean isGoalState() {
		for (int row = 1; row < MAX_ROW - 1; row++) {
			for (int col = 1; col < MAX_COL - 1; col++) {
				char g = this.goals[row][col];
				char b = Character.toLowerCase(boxes[row][col]);
				if (g > 0 && b != g) {
					return false;
				}
			}
		}
		return true;
	}

	public ArrayList<Node> getExpandedNodes() {
		// Box theBox = null;

		for (Box b : boxes2) {
			if (b.color.equals(theAgentColor)) {
				myBoxes.add(b);
			}
		}

		ArrayList<Node> expandedNodes = new ArrayList<Node>(Command.EVERY.length); // TODO
																					// this
																					// adds
																					// node
																					// without
																					// agent
																					// in
																					// it.
																					// Check
																					// solved,
																					// its
																					// because
																					// agent
																					// is
																					// on
																					// goal
		for (Command c : Command.EVERY) {
			// Determine applicability of action
			int newAgentRow = this.agentRow + Command.dirToRowChange(c.dir1);
			int newAgentCol = this.agentCol + Command.dirToColChange(c.dir1);

			String boxColor = "";

			if (c.actionType == Type.Move) {
				// Check if there's a wall or box on the cell to which the agent
				// is moving
				if (this.cellIsFree(newAgentRow, newAgentCol)) {
					Node n = this.ChildNode();
					////// System.err.println("Move!!! by: "+theAgentName);

					n.action = c;
					n.agentRow = newAgentRow;
					n.agentCol = newAgentCol;
					expandedNodes.add(n);
					// //////System.err.println("Move \n" + n);
					// //////System.err.println("ITIASS HAPPENING move");

				}
			} else if (c.actionType == Type.Push) {

				//////// System.err.println("MyBoxes: "+myBoxes);
				for (Box b : myBoxes) {

					if (b.name.equals(this.boxes[newAgentRow][newAgentCol])) {
						boxColor = b.color;
						// //////System.err.println("ITIASS HAPPENING pull");
					}
				}

				//System.err.println("ZEBUG: "+theAgentColor);
				if (this.boxAt(newAgentRow, newAgentCol) && theAgentColor.equals(boxColor)) {
					// //////System.err.println("yes it is");

					int newBoxRow = newAgentRow + Command.dirToRowChange(c.dir2);
					int newBoxCol = newAgentCol + Command.dirToColChange(c.dir2);
					// .. and that new cell of box is free.

					if (this.cellIsFree(newBoxRow, newBoxCol)) {

						Node n = this.ChildNode();

						// System.err.println("Push!!!" +
						// boxes[newAgentRow][newAgentCol]+": "+boxColor+" by:
						// "+theAgentName+": "+theAgentColor);
						n.action = c;
						n.agentRow = newAgentRow;
						n.agentCol = newAgentCol;

						n.boxes[newBoxRow][newBoxCol] = this.boxes[newAgentRow][newAgentCol];

						// Update boxes2 in childnode
						for (Box b : this.boxes2) {

							if (b.position.row == newAgentRow && b.position.col == newAgentCol) {

								for (Box childbox : n.boxes2) {
									if (childbox.position.row == b.position.row && childbox.position.col == b.position.col) {

										childbox.position.row = newBoxRow;
										childbox.position.col = newBoxCol;
									}
								}
							}

						}

						n.boxes[newAgentRow][newAgentCol] = 0;
						expandedNodes.add(n);

					}

				}
			} else if (c.actionType == Type.Pull) {
				// Cell is free where agent is going

				if (this.cellIsFree(newAgentRow, newAgentCol)) {
					int boxRow = this.agentRow + Command.dirToRowChange(c.dir2);
					int boxCol = this.agentCol + Command.dirToColChange(c.dir2);
					// .. and there's a box in "dir2" of the agent

					for (Box b : myBoxes) {
						// //////System.err.println("Box: "+b+" nameOfCharBox:
						// "+this.boxes[boxRow][boxCol]);
						if (b.name.equals(this.boxes[boxRow][boxCol])) {
							boxColor = b.color;

						}
					}

					if (this.boxAt(boxRow, boxCol) && theAgentColor.equals(boxColor)) {

						Node n = this.ChildNode();

						// System.err.println("Pull!!!" +
						// boxes[boxRow][boxCol]+": "+boxColor+" by:
						// "+theAgentName+": "+theAgentColor);

						n.action = c;
						n.agentRow = newAgentRow;
						n.agentCol = newAgentCol;
						n.boxes[this.agentRow][this.agentCol] = this.boxes[boxRow][boxCol];
						n.boxes[boxRow][boxCol] = 0;

						for (Box b : this.boxes2) {

							if (b.position.row == newAgentRow && b.position.col == newAgentCol) {

								for (Box childbox : n.boxes2) {
									if (childbox.position.row == b.position.row && childbox.position.col == b.position.col) {

										childbox.position.row = agentRow;
										childbox.position.col = agentCol;

									}
								}
							}

						}
						expandedNodes.add(n);

					}
				}
			}

		}
		// Collections.shuffle(expandedNodes, RND);

		// //////System.err.println("size: "+expandedNodes);
		return expandedNodes;
	}

	private boolean cellIsFree(int row, int col) {
		return !SearchClient.walls[row][col] && this.boxes[row][col] == 0;
	}

	private boolean boxAt(int row, int col) {
		return this.boxes[row][col] > 0;
	}

	private Node ChildNode() {
		Node copy = new Node(this, MAX_ROW, MAX_COL);

		// aici
		for (int row = 0; row < MAX_ROW; row++) {
			System.arraycopy(SearchClient.walls[row], 0, SearchClient.walls[row], 0, MAX_COL);
			System.arraycopy(this.boxes[row], 0, copy.boxes[row], 0, MAX_COL);
			System.arraycopy(this.goals[row], 0, copy.goals[row], 0, MAX_COL);
		}
		copy.theAgentColor = this.theAgentColor;
		copy.myBoxes = this.myBoxes;
		copy.theAgentName = this.theAgentName;
		copy.goals2 = this.goals2;
		copy.myBoxesFinal = this.myBoxesFinal;
		copy.boxes2 = this.boxes2;
		// //////System.err.println("copy:::::"+copy.myBoxes);

		return copy;
	}

	public LinkedList<Node> extractPlan() {
		LinkedList<Node> plan = new LinkedList<Node>();
		Node n = this;
		//////// System.err.println("initialNodeisinitialstate: " + n.parent +
		//////// n.isGoalState());
		while (!n.isInitialState()) {
			//// System.err.println("plan: " + n);
			plan.addFirst(n);

			n = n.parent;
			//////// System.err.println("parent: " + n);
		}
		//////// System.err.println("the plan" + plan);
		return plan;
	}

	@Override
	public int hashCode() {
		if (this._hash == 0) {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.agentCol;
			result = prime * result + this.agentRow;
			result = prime * result + Arrays.deepHashCode(this.boxes);
			result = prime * result + Arrays.deepHashCode(this.goals);
			result = prime * result + Arrays.deepHashCode(SearchClient.walls);
			this._hash = result;
		}
		return this._hash;
	}

	public void printGoals() {
		System.err.println("__________________________________________________\n");
		for (int i = 0; i < Node.MAX_ROW; i++) {
			System.err.print("\n");
			for (int j = 0; j < Node.MAX_COL; j++) {

				System.err.print(this.goals[i][j]);
			}
		}
		System.err.println("\n__________________________________________________");

	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (this.agentRow != other.agentRow || this.agentCol != other.agentCol)
			return false;
		if (!Arrays.deepEquals(this.boxes, other.boxes))
			return false;
		if (!Arrays.deepEquals(this.goals, this.goals))
			return false;
		if (!Arrays.deepEquals(SearchClient.walls, SearchClient.walls))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("\n");
		for (int row = 0; row < MAX_ROW; row++) {
			if (!SearchClient.walls[row][0]) {
				break;
			}
			for (int col = 0; col < MAX_COL; col++) {
				if (this.boxes[row][col] > 0) {
					s.append(this.boxes[row][col]);
				} else if (this.goals[row][col] > 0) {
					s.append(this.goals[row][col]);
				} else if (SearchClient.walls[row][col]) {
					s.append("+");
				} else if (row == this.agentRow && col == this.agentCol) {
					s.append(Integer.toString(theAgentName));
				} else {
					s.append(" ");
				}
			}
			s.append("\n");
		}
		return s.toString();
	}

	public boolean isConflict(Node node2) {

		if (this.agentRow == node2.agentRow && this.agentCol == node2.agentCol) {
			System.err.println("Agent conflict! woo");
			return true;
		}

		for (int i = 0; i < Node.MAX_ROW; i++) {
			// if agent wants to move into box the other agent pushed in his
			// path

			for (int j = 0; j < Node.MAX_COL; j++) {
				if (this.boxes[i][j] > 0 && node2.agentRow == i && node2.agentCol == j) {

					for (Box b : this.boxes2) {
						if (b.name == this.boxes[i][j] && b.color.equals(node2.theAgentColor)) {

							break;
						} else {

							System.err.println("conflict boxes of node1 agent of node2:" + this.toString());
							System.err.println("conflict in boxes of node1 agent of node2:" + node2.toString());
							return true;
						}

					}

				}

				if (node2.boxes[i][j] > 0 && this.agentRow == i && this.agentCol == j) {
					for (Box b : node2.boxes2) {
						if (b.name == node2.boxes[i][j] && b.color.equals(this.theAgentColor)) {
							break;
						} else {

							System.err.println("conflict boxes of node1 agent of node2:" + this.toString());
							System.err.println("conflict in boxes of node1 agent of node2:" + node2.toString());
							return true;
						}

					}
				}
			}
		}

		System.err.println("No conflicts in this move");
		return false;
	}

	public void updateUberNode(List<Agent> agents) // update the boxes of the
													// same color as the
													// smallNode in uberNode
	{

		System.err.println("Updating UberNode and agents initiated");
		// List<Agent> tempAgents = agents;
		System.err.println("Agents before initialized: " + agents);

		// we have to update the boxes2 locations!!! check if they a re updated
		// cannot update those, use boxes[][] instead

		// TODO update boxes2 based on last nodes of all agents and moves that
		// are stored in their initialState

		this.boxes2.clear();
		for (int i = 0; i < Node.MAX_ROW; i++) {
			for (int j = 0; j < Node.MAX_COL; j++) {
				this.boxes[i][j] = 0;
			}
		}

		//handling two same colored agents
		List<String> colors = new ArrayList<String>();
		List<Agent> dublicateColorAgents = new ArrayList<Agent>();

		for (Agent a : agents) {
			if (colors.contains(a.color)) {
				dublicateColorAgents.add(a);

			} else {

				colors.add(a.color);
			}
		}

		
		
		// read the boxes for all agents, add only the boxes of their own color
		for (Agent agent : agents) {
			if (!dublicateColorAgents.contains(agent)) {  //BIG problem when agents have the same color
				
				// fix this for many agents of same color
				//TODO
				for (int i = 0; i < Node.MAX_ROW; i++) {
					for (int j = 0; j < Node.MAX_COL; j++) {
						if (agent.initialState.boxes[i][j] >= 'A' && agent.initialState.boxes[i][j] <= 'Z') {

							//same colored agents have problems, add requirement to be next to the box for it to get added

							if (agent.color.equals(SearchClient.boxesToColor.get(agent.initialState.boxes[i][j]))) {

								this.boxes2.add(new Box(agent.initialState.boxes[i][j], SearchClient.boxesToColor.get(agent.initialState.boxes[i][j]), new Position(i, j)));
								this.boxes[i][j] = agent.initialState.boxes[i][j];

							}
						}

					}
				}
			}
		}
		// now update the states of the agents

		for (Agent a : agents) {

			Node newInitialState = this.Copy();

			newInitialState.parent = null;
			newInitialState.agentRow = a.initialState.agentRow; // agent must
			newInitialState.agentCol = a.initialState.agentCol;
			newInitialState.theAgentColor = a.initialState.theAgentColor;

			// erase goals2
			newInitialState.goals2.clear();
			newInitialState.theAgentName = a.name;
			newInitialState.doNoOp = false;

			// erase goals
			for (int i = 0; i < Node.MAX_ROW; i++) {
				for (int j = 0; j < Node.MAX_COL; j++) {
					newInitialState.goals[i][j] = 0;
				}
			}

			for (Goal g : this.goals2) {

				if (g.color.equals(a.color)) {
					newInitialState.goals2.add(g);
					newInitialState.goals[g.position.row][g.position.col] = g.name;
				}
			}
			a.initialState = newInitialState;

		}

		//System.err.println("Uberboxes after refill: " + this.boxes2);

		System.err.println("Ubernode after refill: " + this.toString());

		System.err.println("Agents after refill: " + agents);
		//System.err.println("Agents after refill in searchclient: " + SearchClient.agents);

	}

}