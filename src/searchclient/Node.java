package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import searchclient.Command.Type;

public class Node {
	//WALL SNAKE METHOD
	public ArrayList<Position> tempWalls;
	public ArrayList<ArrayList<Position>> blockedPositions;
	public int blockedPositionsID = 0;
	public ArrayList<Integer> priorAgentIDs;
	//WALL SNAKE METHOD
	
	private static final Random RND = new Random(1);

	public static int MAX_ROW;
	public static int MAX_COL;

	public int agentRow;
	public int agentCol;
	public int theAgentName; // = new Agent(0,null);
	public String theAgentColor;
	
	public boolean doNoOp = false;


	public char[][] boxes;
	public char[][] goals;

	// alphabetially sorted both lists
	public List<Goal> goals2 = new ArrayList<>();
	public List<Box> boxes2 = new ArrayList<>();

	public List<Box> myBoxes = new ArrayList<>();

	// list of storagepositions filled in heuristics
	public List<Position> storagePos = new ArrayList<Position>();

	public Node parent;
	public Command action;

	private int g;

	private int _hash = 0;

	/*public Node(Node parent, int maxRow, int maxCol) {

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
	}*/
	public void assignBlocked(ArrayList<ArrayList<Position>> positions){
		blockedPositions = positions;
		if(this.blockedPositions.size() > blockedPositionsID){
			for(Position p : this.blockedPositions.get(blockedPositionsID)){
				this.boxes[p.row][p.col] = '*';//TODO: Set '*' to be an imaginary color
			}
			if(this.blockedPositions.size() > blockedPositionsID+1){
				for(Position p : this.blockedPositions.get(blockedPositionsID+1)){
					this.boxes[p.row][p.col] = '*';
				}
				if(this.blockedPositions.size() > blockedPositionsID+2){
					for(Position p : this.blockedPositions.get(blockedPositionsID+2)){
						this.boxes[p.row][p.col] = '*';
					}
					String tester = "TESTAH "+ theAgentName + "\n";
					for(int j = 0; j<SearchClient.levelRowSize; j++){
						for(int j2 = 0; j2<SearchClient.levelColumnSize; j2++){
							tester += this.boxes[j][j2];
						}
						tester += "\n";
					}
					//System.err.println(tester);
				}
			}
		} else {
			//TODO: We've passed last case
			for(List<Node> n : SearchClient.solutions){
				Position derpp = new Position(n.get(n.size()-1).agentRow,n.get(n.size()-1).agentCol);
				this.boxes[derpp.row][derpp.col] = '*';
			}
		}
		
		if(this.blockedPositions.size() > this.blockedPositionsID-1 && this.blockedPositionsID-1>= 0 && SearchClient.solutions != null){
			//Fog of war
			if(SearchClient.solutions.size()>0 && SearchClient.solutions.get(SearchClient.solutions.size()-1).size()>this.blockedPositionsID-1){//Don't get data from prior if no prior exists
				for(Position p : this.blockedPositions.get(this.blockedPositionsID-1)){
					//Get data from agent just prior to this one.
					this.boxes[p.row][p.col] = SearchClient.solutions.get(SearchClient.solutions.size()-1).get(this.blockedPositionsID-1).boxes[p.row][p.col];
				}
			}
		} else if(this.blockedPositions.size() <= this.blockedPositionsID-1 && SearchClient.solutions != null){//We're over the edge

			if(SearchClient.solutions.size()>0 && SearchClient.solutions.get(SearchClient.solutions.size()-1).size()>this.blockedPositionsID-1){//Don't get data from prior if no prior exists
				for(Position p : this.blockedPositions.get(this.blockedPositions.size()-1)){
					//Get data from agent just prior to this one.
					this.boxes[p.row][p.col] = SearchClient.solutions.get(SearchClient.solutions.size()-1).get(blockedPositionsID-1).boxes[p.row][p.col];
				}
			}
		}
	}
	
	public void assignPriorAgents(ArrayList<Integer> priorAgents){
		this.priorAgentIDs = priorAgents;
	}
	
	public Node(Node parent, int maxRow, int maxCol, ArrayList<ArrayList<Position>> blockedPositions, int blockedPositionsID) {
		this.blockedPositionsID = blockedPositionsID;
		this.blockedPositions = blockedPositions;
		if(this.blockedPositions.size()>blockedPositionsID){
			this.tempWalls = this.blockedPositions.get(this.blockedPositionsID);
		} //Else, the other agent finished his plan.
		this.priorAgentIDs = new ArrayList<Integer>();
		
		
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

						//here determine the box iondex and set it. The boxes2 needs to be sorted
						//n.boxes2.set(arg0, arg1);
						//TODO: this 0 is not ok here. needs to be the agent's number
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

						// //////System.err.println("Pull \n" + n);
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
		Node copy = new Node(this, MAX_ROW, MAX_COL, blockedPositions, blockedPositionsID+1);
		//System.err.println("This is the thing maaaan| " +copy.blockedPositionsID);
		// aici
		for (int row = 0; row < MAX_ROW; row++) {
			System.arraycopy(SearchClient.walls[row], 0, SearchClient.walls[row], 0, MAX_COL);
			System.arraycopy(this.boxes[row], 0, copy.boxes[row], 0, MAX_COL);
			System.arraycopy(this.goals[row], 0, copy.goals[row], 0, MAX_COL);
		}
		for(int j = 0; j<SearchClient.levelRowSize; j++){
			for(int j2 = 0; j2<SearchClient.levelColumnSize; j2++){
				if(copy.boxes[j][j2] == '*'){
					copy.boxes[j][j2] = ' ';
				}
			}
		}
		copy.theAgentColor = this.theAgentColor;
		copy.myBoxes = this.myBoxes;
		copy.theAgentName = this.theAgentName;
		if(copy.blockedPositions.size() > blockedPositionsID){
			for(Position p : copy.blockedPositions.get(blockedPositionsID)){
				copy.boxes[p.row][p.col] = '*';//TODO: Set '*' to be an imaginary color
			}
			if(copy.blockedPositions.size() > blockedPositionsID+1){
				for(Position p : copy.blockedPositions.get(blockedPositionsID+1)){
					copy.boxes[p.row][p.col] = '*';
				}
				if(copy.blockedPositions.size() > blockedPositionsID+2){
					for(Position p : copy.blockedPositions.get(blockedPositionsID+2)){
						copy.boxes[p.row][p.col] = '*';
					}
					String tester = "TESTAH "+ theAgentName + "\n";
					for(int j = 0; j<SearchClient.levelRowSize; j++){
						for(int j2 = 0; j2<SearchClient.levelColumnSize; j2++){
							tester += copy.boxes[j][j2];
						}
						tester += "\n";
					}
					//System.err.println(tester);
				}
			}
		} else {
			//TODO: We've passed last case
			for(List<Node> n : SearchClient.solutions){
				Position derpp = new Position(n.get(n.size()-1).agentRow,n.get(n.size()-1).agentCol);
				copy.boxes[derpp.row][derpp.col] = '*';
			}
		}
		
		if(copy.blockedPositions.size() > copy.blockedPositionsID-1 && copy.blockedPositionsID-1>= 0 && SearchClient.solutions != null){
			//Fog of war
			if(SearchClient.solutions.size()>0 && SearchClient.solutions.get(SearchClient.solutions.size()-1).size()>copy.blockedPositionsID-1){//Don't get data from prior if no prior exists
				for(Position p : copy.blockedPositions.get(copy.blockedPositionsID-1)){
					//Get data from agent just prior to this one.
					copy.boxes[p.row][p.col] = SearchClient.solutions.get(SearchClient.solutions.size()-1).get(copy.blockedPositionsID-1).boxes[p.row][p.col];
				}
			}
		} else if(copy.blockedPositions.size() <= copy.blockedPositionsID-1 && SearchClient.solutions != null){//We're over the edge

			if(SearchClient.solutions.size()>0 && SearchClient.solutions.get(SearchClient.solutions.size()-1).size()>copy.blockedPositionsID-1){//Don't get data from prior if no prior exists
				for(Position p : copy.blockedPositions.get(copy.blockedPositions.size()-1)){
					//Get data from agent just prior to this one.
					copy.boxes[p.row][p.col] = SearchClient.solutions.get(SearchClient.solutions.size()-1).get(blockedPositionsID-1).boxes[p.row][p.col];
				}
			}
		}
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