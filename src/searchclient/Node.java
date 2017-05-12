package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
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
	public int level = 0;
	
	public boolean doNoOp = false;
	static Goal gAux;

	public char[][] boxes;
	public char[][] goals;

	// alphabetially sorted both lists
	public Queue<Goal> goals2 = new PriorityQueue<>(gAux);
	public List<Box> boxes2 = new ArrayList<>();
	
	//This should be computed only once
	public List<Box> myBoxes = new ArrayList<>();
	
	public Set<Box> myBoxesFinal = new TreeSet<>();

	// list of storagepositions filled in heuristics
	public List<Position> storagePos = new ArrayList<Position>();

	public Node parent;
	public Command action;

	private int g;

	private int _hash = 0;

	public int expandedNodeId = 0;

	public Node(Node parent, int maxRow, int maxCol) {
		//boxes
		
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

	public boolean isGoalState(Node goalState) {
		for (int row = 1; row < MAX_ROW - 1; row++) {
			for (int col = 1; col < MAX_COL - 1; col++) {
				char g = goalState.goals[row][col];
				char b = Character.toLowerCase(goalState.boxes[row][col]);
				if (g > 0 && b != g) {
					return false;
				}
			}
		}
		return true;

	}

	public ArrayList<Node> getExpandedNodes(List<List<Node>> previousSolutions) {
		// Box theBox = null;
		
		ArrayList<Node> expandedNodes = new ArrayList<Node>(Command.EVERY.length); 
		//the first agent
		if(previousSolutions == null) {
			for (Box b : boxes2) {
				if (b.color.equals(theAgentColor)) {
					myBoxes.add(b);
				}
			}

			
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
						n.action = c;
						n.agentRow = newAgentRow;
						n.agentCol = newAgentCol;
						
						expandedNodes.add(n);

					} 
					
				} else if (c.actionType == Type.Push) {
					for (Box b : myBoxes) {

						if (b.name.equals(this.boxes[newAgentRow][newAgentCol])) {
							boxColor = b.color;
						}
					}

					if (this.boxAt(newAgentRow, newAgentCol) && theAgentColor.equals(boxColor)) {

						int newBoxRow = newAgentRow + Command.dirToRowChange(c.dir2);
						int newBoxCol = newAgentCol + Command.dirToColChange(c.dir2);
						// .. and that new cell of box is free.

						if (this.cellIsFree(newBoxRow, newBoxCol)) {

							Node n = this.ChildNode();
							n.action = c;
							n.agentRow = newAgentRow;
							n.agentCol = newAgentCol;

							n.boxes[newBoxRow][newBoxCol] = this.boxes[newAgentRow][newAgentCol];
							//Update the new position of the box
							//n.boxes2.set((int)(n.boxes[newBoxRow][newBoxCol] - 'A'), new Position(newBoxRow, newBoxCol));
							//here determine the box iondex and set it. The boxes2 needs to be sorted
							//n.boxes2.set(arg0, arg1);
							//TODO: this 0 is not ok here. needs to be the agent's number
							
							//get the box I want to move from the specific index
//							System.err.println(n.boxes[newBoxRow][newBoxCol] - 'A');
//							Box b = n.myBoxes.get(n.boxes[newBoxRow][newBoxCol] - 'A');
//							//update its position in the same index
//							b.position.row = newBoxRow;
//							b.position.col = newBoxCol;
//							n.myBoxes.set((int)(n.boxes[newBoxRow][newBoxCol] - 'A'), b);
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

								// //////System.err.println("ITIASS HAPPENING
								// pull");
							}
						}
						// //////System.err.println(boxColor+" <- boxColor,
						// agentColor-->"+theAgentColor);
						// "+theAgentColor);
						// //////System.err.println("Pull!!! Box color: "+boxColor+"
						// agentColoragentColor-->"+theAgentName+theAgentColor);

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
							
//							//get the box I want to move from the specific index
//							Box b = n.myBoxes.get(n.boxes[this.agentRow][this.agentCol] - 'A');
//							//update its position in the same index
//							b.position.row = this.agentRow;
//							b.position.col = this.agentCol;
//							n.myBoxes.set((int)(n.boxes[this.agentRow][this.agentCol] - 'A'), b);
							// //////System.err.println("Pull \n" + n);
							expandedNodes.add(n);

						}
					}
				}

			}

		} else {
			for (Box b : boxes2) {
				if (b.color.equals(theAgentColor)) {
					myBoxes.add(b);
				}
			}
			
			//System.err.println("Level " + level + " for agent " + theAgentName);
			List<Node> otherAgentsSolutionsAtCurrentPoint = new ArrayList<>();
			List<Node> otherAgentsSolutionsAtNextPoint = new ArrayList<>();
			List<Node> otherAgentsSolutionsAtNextNextPoint = new ArrayList<>();
			for(List<Node> l:previousSolutions) {
				try{
					otherAgentsSolutionsAtCurrentPoint.add(l.get(level));
					otherAgentsSolutionsAtNextPoint.add(l.get(level+1));
					otherAgentsSolutionsAtNextNextPoint.add(l.get(level+2));
					//System.err.println(l.get(level).toString());
				} catch (Exception e) {
					otherAgentsSolutionsAtCurrentPoint.add(l.get(l.size()-1));
					otherAgentsSolutionsAtNextPoint.add(l.get(l.size()-1));
					otherAgentsSolutionsAtNextNextPoint.add(l.get(l.size()-1));
					//System.err.println(l.get(l.size()-1).toString());
				}
			}
			
			for (Command c : Command.EVERY) {
				// Determine applicability of action
				int newAgentRow = this.agentRow + Command.dirToRowChange(c.dir1);
				int newAgentCol = this.agentCol + Command.dirToColChange(c.dir1);

				String boxColor = "";

				if (c.actionType == Type.Move) {
					// Check if there's a wall or box on the cell to which the agent
					// is moving
					if (this.cellIsFree(newAgentRow, newAgentCol) && !conflictsWithOtherAgents(otherAgentsSolutionsAtCurrentPoint,
							otherAgentsSolutionsAtNextPoint, otherAgentsSolutionsAtNextNextPoint, newAgentRow, newAgentCol)) {
						Node n = this.ChildNode();
						////// System.err.println("Move!!! by: "+theAgentName);

						n.action = c;
						n.agentRow = newAgentRow;
						n.agentCol = newAgentCol;
						expandedNodes.add(n);
						// //////System.err.println("Move \n" + n);
						// //////System.err.println("ITIASS HAPPENING move");

					} 
					else {
						this.expandedNodeId = -1;
						expandedNodes.add(this);
						
					}
				} else if (c.actionType == Type.Push) {

					//////// System.err.println("MyBoxes: "+myBoxes);
					for (Box b : myBoxes) {

						if (b.name.equals(this.boxes[newAgentRow][newAgentCol])) {
							boxColor = b.color;
							// //////System.err.println("ITIASS HAPPENING pull");
						}
					}

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
							//Update the new position of the box
							//n.boxes2.set((int)(n.boxes[newBoxRow][newBoxCol] - 'A'), new Position(newBoxRow, newBoxCol));
							//here determine the box iondex and set it. The boxes2 needs to be sorted
							//n.boxes2.set(arg0, arg1);
							//TODO: this 0 is not ok here. needs to be the agent's number
							
							//get the box I want to move from the specific index
//							System.err.println(n.boxes[newBoxRow][newBoxCol] - 'A');
//							Box b = n.myBoxes.get(n.boxes[newBoxRow][newBoxCol] - 'A');
//							//update its position in the same index
//							b.position.row = newBoxRow;
//							b.position.col = newBoxCol;
//							n.myBoxes.set((int)(n.boxes[newBoxRow][newBoxCol] - 'A'), b);
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

								// //////System.err.println("ITIASS HAPPENING
								// pull");
							}
						}
						// //////System.err.println(boxColor+" <- boxColor,
						// agentColor-->"+theAgentColor);
						// "+theAgentColor);
						// //////System.err.println("Pull!!! Box color: "+boxColor+"
						// agentColoragentColor-->"+theAgentName+theAgentColor);

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
							
//							//get the box I want to move from the specific index
//							Box b = n.myBoxes.get(n.boxes[this.agentRow][this.agentCol] - 'A');
//							//update its position in the same index
//							b.position.row = this.agentRow;
//							b.position.col = this.agentCol;
//							n.myBoxes.set((int)(n.boxes[this.agentRow][this.agentCol] - 'A'), b);
							// //////System.err.println("Pull \n" + n);
							expandedNodes.add(n);

						}
					}
				}

			}

		}
				// Collections.shuffle(expandedNodes, RND);

		// //////System.err.println("size: "+expandedNodes);
		return expandedNodes;
	}

	private boolean conflictsWithOtherAgents(List<Node> otherAgentsSolutionsAtCurrentPoint,
													List<Node> otherAgentsSolutionsAtNextPoint,
													List<Node> otherAgentsSolutionsAtNextNextPoint,
																					int row, int col) {
		for(Node n:otherAgentsSolutionsAtCurrentPoint) {
			if(n.boxAt(row, col)) {
				//System.err.println("Conflict for agent " + this.theAgentName + " with box from " + n.theAgentName + " on position (" + row + ", " + col +").");
				return true;
			}
			if(n.agentAt(row, col)) {
				//System.err.println("Conflict for agent " + this.theAgentName + " with agent " + n.theAgentName + " on position (" + row + ", " + col +").");
				return true;
			}
		}
		
		for(Node n:otherAgentsSolutionsAtNextPoint) {
			if(n.boxAt(row, col)) {
				//System.err.println("Conflict for agent " + this.theAgentName + " with box from " + n.theAgentName + " on position (" + row + ", " + col +").");
				return true;
			}
			if(n.agentAt(row, col)) {
				//System.err.println("Conflict for agent " + this.theAgentName + " with agent " + n.theAgentName + " on position (" + row + ", " + col +").");
				return true;
			}
		}
		
		for(Node n:otherAgentsSolutionsAtNextNextPoint) {
			if(n.boxAt(row, col)) {
				//System.err.println("Conflict for agent " + this.theAgentName + " with box from " + n.theAgentName + " on position (" + row + ", " + col +").");
				return true;
			}
			if(n.agentAt(row, col)) {
				//System.err.println("Conflict for agent " + this.theAgentName + " with agent " + n.theAgentName + " on position (" + row + ", " + col +").");
				return true;
			}
		}
		
		return false;
	}

	private boolean cellIsFree(int row, int col) {
		return !SearchClient.walls[row][col] && this.boxes[row][col] == 0;
	}

	private boolean boxAt(int row, int col) {
		return this.boxes[row][col] > 0;
	}
	
	private boolean agentAt(int row, int col) {
		return this.agentRow == row && this.agentCol == col;
	}

	private Node ChildNode() {
		Node copy = new Node(this, MAX_ROW, MAX_COL);
		
		copy.level = this.level + 1;
		//System.err.println("Level " + copy.level);
		// aici
		for (int row = 0; row < MAX_ROW; row++) {
			//System.arraycopy(SearchClient.walls[row], 0, SearchClient.walls[row], 0, MAX_COL);
			System.arraycopy(this.boxes[row], 0, copy.boxes[row], 0, MAX_COL);
			System.arraycopy(this.goals[row], 0, copy.goals[row], 0, MAX_COL);
		}
		copy.theAgentColor = this.theAgentColor;
		copy.myBoxes = this.myBoxes;
		copy.expandedNodeId = this.expandedNodeId + 1;
		copy.theAgentName = this.theAgentName;
		copy.myBoxesFinal = this.myBoxesFinal;
		copy.goals2 = this.goals2;
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

}