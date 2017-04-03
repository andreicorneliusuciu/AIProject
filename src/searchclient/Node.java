package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID;

import searchclient.Command.Type;

public class Node {
	private static final Random RND = new Random(1);

	public static int MAX_ROW;
	public static int MAX_COL;
//agentlocation agentRow,agentCol,0->max agents

	public int agentRow;
	public int agentCol;
	
	public char[][] boxes;
	public List<Position> boxesList;
	
	public Node parent;
	public Command action;

	private int g;
	
	private int _hash = 0;

	public Node(Node parent,int maxRow,int maxCol) {
		this.parent = parent;
		MAX_ROW = maxRow;
		MAX_COL = maxCol;
		//TODO: get rid of the boxes matrix
		boxes = new char[MAX_ROW][MAX_COL];
		this.boxesList = new ArrayList<>();
		
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
				char g = SearchClient.goals[row][col];
				char b = Character.toLowerCase(boxes[row][col]);
				if (g > 0 && b != g) {
					return false;
				} 
			}
		}
		return true;
	}

	public ArrayList<Node> getExpandedNodes() {
		
		//expands all possible nodes/states from present state
		
		ArrayList<Node> expandedNodes = new ArrayList<Node>(Command.EVERY.length);
		for (Command c : Command.EVERY) {
			// Determine applicability of action
			int newAgentRow = this.agentRow + Command.dirToRowChange(c.dir1);
			int newAgentCol = this.agentCol + Command.dirToColChange(c.dir1);
			
			int agentNumber = 404;
			
			
			
			
			
			String boxColor = ""; 
			String agentColor = ""; 
			
			//mistake here?
			for(int i=0; i<SearchClient.agents.size();i++)
			{
				//System.err.println("lul "+SearchClient.agents.size());
				//System.err.println("lul "+SearchClient.agentLocation[i][0]+", "+SearchClient.agentLocation[i][1]+" this.AgentLoc"+this.agentRow+", "+this.agentCol); 
				if(SearchClient.agentLocation[i][0]== this.agentRow && SearchClient.agentLocation[i][1]== this.agentCol)
				{
					
					agentNumber = i;
					agentColor = SearchClient.agents.get(i).color;
					//System.err.println("lul "+agentColor); 

				}
				
			}
			
			
			
			//System.err.println("before ifs "+agentColor+" agentlocation" + SearchClient.agentLocation[0][0] +","+SearchClient.agentLocation[0][1]+" agentlocation1 " + SearchClient.agentLocation[1][0]+ "," +SearchClient.agentLocation[1][1] + " this.agntloc" +this.agentRow +","+this.agentCol+"   agentColor " +agentColor); 
			if (c.actionType == Type.Move) {
				// Check if there's a wall or box on the cell to which the agent is moving
				if (this.cellIsFree(newAgentRow, newAgentCol)) {
					Node n = this.ChildNode();
					n.action = c;
					n.agentRow = newAgentRow;
					n.agentCol = newAgentCol;
					expandedNodes.add(n);
					
					
					//update agent locations
//					SearchClient.agentLocation[agentNumber][0] = newAgentRow;
//					SearchClient.agentLocation[agentNumber][1] = newAgentCol;
//					
					//print all moves, toWork:add to all moves
					//System.err.println(n.toString());

				}
				
			} else if (c.actionType == Type.Push) {
				//System.err.println("Try push "+agentColor); 
				// Make sure that there's actually a box to move
				// Make sure their are the same color
				
				//agentColor = SearchClient.agents.get(boxes[newAgentRow][newAgentCol]);
				//boxColor = SearchClient.colorToBoxes.get(boxes[newAgentRow][newAgentCol]);
				

				
				boxColor = SearchClient.colorToBoxes.get(boxes[newAgentRow][newAgentCol]);

			//System.err.println("I tried to Push: Box Color "+boxColor+" Agent Color "+agentColor); 

				if (this.boxAt(newAgentRow, newAgentCol) ) {

					int newBoxRow = newAgentRow + Command.dirToRowChange(c.dir2);
					int newBoxCol = newAgentCol + Command.dirToColChange(c.dir2);
					// .. and that new cell of box is free
					if (this.cellIsFree(newBoxRow, newBoxCol)) {
						
						//System.err.println("Cell was free " + boxColor+" agent: "+ agentColor); 

						
//						if(boxColor.equals(agentColor))
//						{
						System.err.println("Push: Box Color "+boxColor+", Agent Color " +agentColor); 

						Node n = this.ChildNode();
						n.action = c;
						n.agentRow = newAgentRow;
						n.agentCol = newAgentCol;
						
						n.boxes[newBoxRow][newBoxCol] = this.boxes[newAgentRow][newAgentCol];
						n.boxes[newAgentRow][newAgentCol] = 0;
						expandedNodes.add(n);
						

						//update agent locations
//						SearchClient.agentLocation[agentNumber][0] = newAgentRow;
//						SearchClient.agentLocation[agentNumber][1] = newAgentCol;
//						
//						}
					}
				}
			} else if (c.actionType == Type.Pull) {
				// Cell is free where agent is going
				//ToDo if agent and box are same color
				
				if (this.cellIsFree(newAgentRow, newAgentCol) ) {
					int boxRow = this.agentRow + Command.dirToRowChange(c.dir2);
					int boxCol = this.agentCol + Command.dirToColChange(c.dir2);
					// .. and there's a box in "dir2" of the agent
					
					

					if (this.boxAt(boxRow, boxCol)) {
						
						boxColor = SearchClient.colorToBoxes.get(boxes[boxRow][boxCol]);
						
						//System.err.println("Pull: Box Color "+boxColor+" Agent Color "+agentColor); 
						//if(boxColor.equals(agentColor))
						//{
							
							
						//System.err.println("Pull: Box Color "+boxColor+" Agent Color "+agentColor); 

						
						
						//System.err.println("Pull: Box Color "+boxes[boxRow][boxCol]); 
						
						Node n = this.ChildNode();
						n.action = c;
						n.agentRow = newAgentRow;
						n.agentCol = newAgentCol;
						n.boxes[this.agentRow][this.agentCol] = this.boxes[boxRow][boxCol];
						n.boxes[boxRow][boxCol] = 0;
						expandedNodes.add(n);
						
						
						
//						//update agent locations
//						SearchClient.agentLocation[agentNumber][0] = newAgentRow;
//						SearchClient.agentLocation[agentNumber][1] = newAgentCol;
//						
//						
						
						
						//}
						
					}
				}
			}

		}
		Collections.shuffle(expandedNodes, RND);

		return expandedNodes;
	}

	private boolean cellIsFree(int row, int col) {
		return !SearchClient.walls[row][col] && this.boxes[row][col] == 0;
	}

	private boolean boxAt(int row, int col) {
		return this.boxes[row][col] > 0;
	}

	private Node ChildNode() {
		Node copy = new Node(this,MAX_ROW,MAX_COL);
		for (int row = 0; row < MAX_ROW; row++) {
			System.arraycopy(SearchClient.walls[row], 0, SearchClient.walls[row], 0, MAX_COL);
			System.arraycopy(this.boxes[row], 0, copy.boxes[row], 0, MAX_COL);
			System.arraycopy(SearchClient.goals[row], 0, SearchClient.goals[row], 0, MAX_COL);
		}
		return copy;
	}

	public LinkedList<Node> extractPlan() {
		LinkedList<Node> plan = new LinkedList<Node>();
		Node n = this;
		while (!n.isInitialState()) {
			plan.addFirst(n);
			n = n.parent;
		}
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
			result = prime * result + Arrays.deepHashCode(SearchClient.goals);
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
		if (!Arrays.deepEquals(SearchClient.goals, SearchClient.goals))
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
				} else if (SearchClient.goals[row][col] > 0) {
					s.append(SearchClient.goals[row][col]);
				} else if (SearchClient.walls[row][col]) {
					s.append("+");
				} else if (row == this.agentRow && col == this.agentCol) {
					//this is the reason why all agents appear once and only as 0
					s.append("0");
				} else {
					s.append(" ");
				}
			}
			s.append("\n");
		}
		return s.toString();
	}

}