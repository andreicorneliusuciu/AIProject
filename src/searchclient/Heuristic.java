package searchclient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.lang.Math;

import searchclient.NotImplementedException;
import searchclient.Node;

public abstract class Heuristic implements Comparator<Node> {

	// This is the goal state, represented as a list of the final intended
	// positions of the boxes
	public List<Goal> goals;
	public List<Box> boxes;

	// list of storage positions to be updated in FindStorage() during
	// preprocessing
	public static List<Position> storageSpace = new ArrayList<Position>();

	public Heuristic(Node initialState) {
		// Here's a chance to pre-process the static parts of the level.
		// TODO: I guess I need to make a list of positions in the Node class
		goals = initialState.goals2;
		boxes = initialState.boxes2;

		// TODO: preprocessing methods that do:
		// findStorage(); and add the cells in the static storageSpace variable
		// findTrappedAgent(); and do agent.isTrapped = true and mark the
		// trapping box with box.isBlocking = true
		// findTrickyGoal(initialState); finds hard goals, lowers the
		// goal.priority using the getter

		storageSpace = findStorage(initialState);

	}

	public static List<Position> findStorage(Node initialState) {
		List<Position> theList = new ArrayList<Position>();

		boolean[][] wallSpace = SearchClient.walls;

		ArrayList<Boolean> neighbors = new ArrayList<Boolean>();

		int x = wallSpace.length;
		int y = wallSpace[0].length;
		int wallCount = 0;

		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {

				int count = 0;
				neighbors = getNeighbors(i, j, x, y, wallSpace);

				if (isCabin(i - 1, j - 1, x, y) && isCabin(i - 1, j, x, y) && isCabin(i - 1, j + 1, x, y) && isCabin(i + 1, j - 1, x, y) && isCabin(i + 1, j, x, y) && isCabin(i + 1, j + 1, x, y) && isCabin(i, j + 1, x, y) && isCabin(i, j - 1, x, y)) {
					if (!wallSpace[i][j] && !wallSpace[i - 1][j - 1] && !wallSpace[i - 1][j] && wallSpace[i - 1][j + 1] && !wallSpace[i][j - 1] && wallSpace[i][j + 1] && !wallSpace[i + 1][j - 1] && !wallSpace[i + 1][j] && wallSpace[i + 1][j + 1]) {
						theList.add(new Position(i, j));

					}
				}

				if (isCabin(i - 1, j - 1, x, y) && isCabin(i - 1, j, x, y) && isCabin(i - 1, j + 1, x, y) && isCabin(i + 1, j - 1, x, y) && isCabin(i + 1, j, x, y) && isCabin(i + 1, j + 1, x, y) && isCabin(i, j + 1, x, y) && isCabin(i, j - 1, x, y)) {

					if (!wallSpace[i][j] && !wallSpace[i - 1][j - 1] && !wallSpace[i - 1][j] && !wallSpace[i - 1][j + 1] && !wallSpace[i][j - 1] && !wallSpace[i][j + 1] && wallSpace[i + 1][j - 1] && wallSpace[i + 1][j] && wallSpace[i + 1][j + 1]) {
						theList.add(new Position(i, j));

					}
				}

				if (isCabin(i - 1, j - 1, x, y) && isCabin(i - 1, j, x, y) && isCabin(i - 1, j + 1, x, y) && isCabin(i + 1, j - 1, x, y) && isCabin(i + 1, j, x, y) && isCabin(i + 1, j + 1, x, y) && isCabin(i, j + 1, x, y) && isCabin(i, j - 1, x, y)) {
					if (!wallSpace[i][j] && wallSpace[i - 1][j - 1] && !wallSpace[i - 1][j] && !wallSpace[i - 1][j + 1] && wallSpace[i][j - 1] && !wallSpace[i][j + 1] && wallSpace[i + 1][j - 1] && !wallSpace[i + 1][j] && !wallSpace[i + 1][j + 1]) {
						theList.add(new Position(i, j));

					}
				}

				if (isCabin(i - 1, j - 1, x, y) && isCabin(i - 1, j, x, y) && isCabin(i - 1, j + 1, x, y) && isCabin(i + 1, j - 1, x, y) && isCabin(i + 1, j, x, y) && isCabin(i + 1, j + 1, x, y) && isCabin(i, j + 1, x, y) && isCabin(i, j - 1, x, y)) {
					if (!wallSpace[i][j] && wallSpace[i - 1][j - 1] && wallSpace[i - 1][j] && wallSpace[i - 1][j + 1] && !wallSpace[i][j - 1] && !wallSpace[i][j + 1] && !wallSpace[i + 1][j - 1] && !wallSpace[i + 1][j] && !wallSpace[i + 1][j + 1]) {
						theList.add(new Position(i, j));

					}
				}

				/////////

				// No walls around the position
				if (isCabin(i - 1, j - 1, x, y) && isCabin(i - 1, j, x, y) && isCabin(i - 1, j + 1, x, y) && isCabin(i + 1, j - 1, x, y) && isCabin(i + 1, j, x, y) && isCabin(i + 1, j + 1, x, y) && isCabin(i, j + 1, x, y) && isCabin(i, j - 1, x, y)) {
					if (!wallSpace[i][j] && !wallSpace[i - 1][j - 1] && !wallSpace[i - 1][j] && !wallSpace[i - 1][j + 1] && !wallSpace[i][j - 1] && !wallSpace[i][j + 1] && !wallSpace[i + 1][j - 1] && !wallSpace[i + 1][j] && !wallSpace[i + 1][j + 1]) {
						theList.add(new Position(i, j));

					}
				}

				if (isCabin(i - 1, j - 1, x, y) && isCabin(i - 1, j, x, y) && isCabin(i - 1, j + 1, x, y) && isCabin(i + 1, j - 1, x, y) && isCabin(i + 1, j, x, y) && isCabin(i + 1, j + 1, x, y) && isCabin(i, j + 1, x, y) && isCabin(i, j - 1, x, y)) {

					if (!wallSpace[i][j] && !wallSpace[i - 1][j - 1] && !wallSpace[i - 1][j] && !wallSpace[i - 1][j + 1] && wallSpace[i][j - 1] && wallSpace[i][j + 1] && wallSpace[i + 1][j]) {
						theList.add(new Position(i, j));

					}
				}

				if (isCabin(i - 1, j - 1, x, y) && isCabin(i - 1, j, x, y) && isCabin(i - 1, j + 1, x, y) && isCabin(i + 1, j - 1, x, y) && isCabin(i + 1, j, x, y) && isCabin(i + 1, j + 1, x, y) && isCabin(i, j + 1, x, y) && isCabin(i, j - 1, x, y)) {
					if (!wallSpace[i][j] && wallSpace[i - 1][j] && !wallSpace[i - 1][j + 1] && wallSpace[i][j - 1] && !wallSpace[i][j + 1] && wallSpace[i + 1][j] && !wallSpace[i + 1][j + 1]) {
						theList.add(new Position(i, j));

					}
				}

				if (isCabin(i - 1, j - 1, x, y) && isCabin(i - 1, j, x, y) && isCabin(i - 1, j + 1, x, y) && isCabin(i + 1, j - 1, x, y) && isCabin(i + 1, j, x, y) && isCabin(i + 1, j + 1, x, y) && isCabin(i, j + 1, x, y) && isCabin(i, j - 1, x, y)) {
					if (!wallSpace[i][j] && wallSpace[i - 1][j] && wallSpace[i][j - 1] && wallSpace[i][j + 1] && !wallSpace[i + 1][j - 1] && !wallSpace[i + 1][j] && !wallSpace[i + 1][j + 1]) {
						theList.add(new Position(i, j));

					}
				}

				if (isCabin(i - 1, j - 1, x, y) && isCabin(i - 1, j, x, y) && isCabin(i - 1, j + 1, x, y) && isCabin(i + 1, j - 1, x, y) && isCabin(i + 1, j, x, y) && isCabin(i + 1, j + 1, x, y) && isCabin(i, j + 1, x, y) && isCabin(i, j - 1, x, y)) {
					if (!wallSpace[i][j] && !wallSpace[i - 1][j - 1] && wallSpace[i - 1][j] && !wallSpace[i][j - 1] && wallSpace[i][j + 1] && !wallSpace[i + 1][j - 1] && wallSpace[i + 1][j]) {
						theList.add(new Position(i, j));

					}
				}

				// } catch (IndexOutOfBoundsException e) {
				// //system.err.println(e);
				// }

			}
		}

		return theList;
	}

	public static List<Position> findTempStorage(Node initialState) {
		List<Position> theList = new ArrayList<Position>();

		boolean[][] wallSpace = SearchClient.walls;

		ArrayList<Boolean> neighbors = new ArrayList<Boolean>();

		int x = wallSpace.length;
		int y = wallSpace[0].length;

		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				neighbors = getNeighbors(i, j, x, y, wallSpace);

				if (isCabin(i - 1, j - 1, x, y) && isCabin(i - 1, j, x, y) && isCabin(i - 1, j + 1, x, y) && isCabin(i + 1, j - 1, x, y) && isCabin(i + 1, j, x, y) && isCabin(i + 1, j + 1, x, y) && isCabin(i, j + 1, x, y) && isCabin(i, j - 1, x, y)) {
					if (!wallSpace[i][j] && !wallSpace[i - 1][j - 1] && !wallSpace[i - 1][j] && !wallSpace[i][j - 1]) {
						theList.add(new Position(i, j));

					}
				}

				if (isCabin(i - 1, j - 1, x, y) && isCabin(i - 1, j, x, y) && isCabin(i - 1, j + 1, x, y) && isCabin(i + 1, j - 1, x, y) && isCabin(i + 1, j, x, y) && isCabin(i + 1, j + 1, x, y) && isCabin(i, j + 1, x, y) && isCabin(i, j - 1, x, y)) {

					if (!wallSpace[i][j] && !wallSpace[i - 1][j + 1] && !wallSpace[i][j - 1] && !wallSpace[i][j + 1]) {
						theList.add(new Position(i, j));

					}
				}

				if (isCabin(i - 1, j - 1, x, y) && isCabin(i - 1, j, x, y) && isCabin(i - 1, j + 1, x, y) && isCabin(i + 1, j - 1, x, y) && isCabin(i + 1, j, x, y) && isCabin(i + 1, j + 1, x, y) && isCabin(i, j + 1, x, y) && isCabin(i, j - 1, x, y)) {

					if (!wallSpace[i][j] && !wallSpace[i][j + 1] && !wallSpace[i + 1][j - 1] && !wallSpace[i + 1][j]) {
						theList.add(new Position(i, j));

					}
				}

				if (isCabin(i - 1, j - 1, x, y) && isCabin(i - 1, j, x, y) && isCabin(i - 1, j + 1, x, y) && isCabin(i + 1, j - 1, x, y) && isCabin(i + 1, j, x, y) && isCabin(i + 1, j + 1, x, y) && isCabin(i, j + 1, x, y) && isCabin(i, j - 1, x, y)) {

					if (!wallSpace[i][j] && !wallSpace[i - 1][j - 1] && !wallSpace[i + 1][j] && !wallSpace[i + 1][j + 1]) {
						theList.add(new Position(i, j));

					}
				}
			}
		}

		return theList;
	}


	private boolean[][] invertBooleanArray(boolean[][] arr) {
		boolean[][] walls = arr;
		for (int i = 0; i < walls.length; i++)
			for (int j = 0; j < walls[0].length; j++)
				walls[i][j] = !walls[i][j];

		return walls;
	}

	// check neighbors of array, (x,y) dimensions below code can be used for
	// getting all 8 neighbors for any cell (i,j). Code will return 0 by
	// default.
	public static ArrayList<Boolean> getNeighbors(int i, int j, int x, int y, boolean[][] cellValues) {
		ArrayList<Boolean> neighbors = new ArrayList<>();

		if (isCabin(i, j, x, y)) {
			try {
				if (isCabin(i + 1, j, x, y))
					neighbors.add(cellValues[i + 1][j]);
				if (isCabin(i - 1, j, x, y))
					neighbors.add(cellValues[i - 1][j]);
				if (isCabin(i, j + 1, x, y))
					neighbors.add(cellValues[i][j + 1]);
				if (isCabin(i, j - 1, x, y))
					neighbors.add(cellValues[i][j - 1]);
				if (isCabin(i - 1, j + 1, x, y))
					neighbors.add(cellValues[i - 1][j + 1]);
				if (isCabin(i + 1, j - 1, x, y))
					neighbors.add(cellValues[i + 1][j - 1]);
				if (isCabin(i + 1, j + 1, x, y))
					neighbors.add(cellValues[i + 1][j + 1]);
				if (isCabin(i - 1, j - 1, x, y))
					neighbors.add(cellValues[i - 1][j - 1]);
			} catch (IndexOutOfBoundsException e) {
				return neighbors;
			}
		}
		return neighbors;
	}

	public static boolean isCabin(int i, int j, int x, int y) {
		boolean flag = false;
		if (i >= 0 && i < x && j >= 0 && j < y) {
			flag = true;
		}
		return flag;
	}
	////////////////////

	public int h(Node n) {
		/*int result = 0;
		Set<Box> boxesOrderedAlphabetically = getBoxesPosition(n);
		int i = 0;
		Iterator<Goal> it1 = n.goals2.iterator();
		Iterator<Box> it2 = boxesOrderedAlphabetically.iterator();
		
		while (it1.hasNext() && it2.hasNext()) {

			Box b = it2.next();
			Goal g = it1.next();
			result += DistancesComputer.
					getDistanceBetween2Positions(b.position, g.position);//here???
			//System.err.println("Dist between " + b.position + " and " + g.position + " is " + result);
//			System.err.println("HEURISTIC} goals2 has size = " + n.goals2.size());
//			System.err.println("HEURISTIC} boxesOrderedAlphabetically has size = " + boxesOrderedAlphabetically.size());
			result += DistancesComputer.
					getDistanceBetween2Positions(b.position, new Position(n.agentRow, n.agentCol));
		}*/
		Agent chosenA = null;
		for(Agent a : SearchClient.agents){
			if(n.theAgentName == a.name){
				chosenA = a;
				break;
			}
		}
		
		Box chosenB = null;
		for(Box b : n.boxes2){
			char temp = Character.toLowerCase(b.name);
			if(chosenA.assignedChar == temp){
				//if(!b.isOnOwnGoal()){
					chosenB = b;
					break;
				//}
			}
		}
		if(chosenB == null){
			System.err.println("It happened");
			System.err.println(n);
			//return 0;
		}
		int chooseVal = Integer.MAX_VALUE;
		Goal chosenG = null;
		for(Goal g : SearchClient.allGoals){
			if(g.name == chosenA.assignedChar){
				int calc = DistancesComputer.getDistanceBetween2Positions(g.position, chosenA.position);
				if(chooseVal > calc){
					chooseVal = calc;
					chosenG = new Goal(g);
				}
			}
		}
		//System.err.println("A: " + chosenA.position);
	//	System.err.println(" B: " + chosenB.position);
//		System.err.println(" G: " + chosenG.position);
		//System.err.println(new Position(n.agentRow,n.agentCol));
		//System.err.println("[H] Heuristic result = " + result);
		//System.err.println("Distance: " + DistancesComputer.getDistanceBetween2Positions(chosenB.position, chosenG.position) + " ChosenB: " + chosenB.position + " " + chosenB.name + " chosenG: " + chosenG.name + " " + chosenG.position + " ChosenA: " + chosenA.assignedChar);
		return DistancesComputer.getDistanceBetween2Positions(chosenB.position, chosenG.position)*4 + DistancesComputer.getDistanceBetween2Positions(chosenB.position, new Position(n.agentRow,n.agentCol))*3 + DistancesComputer.getDistanceBetween2Positions(chosenG.position, new Position(n.agentRow,n.agentCol))*2;
	}
	
	public Set<Box> getBoxesPosition(Node n) {
		Set<Box> boxesPosition = new TreeSet<>();
		for(int i = 0; i <  DistancesComputer.levelRowSize -2 ; i++) {
			for(int j = 0; j < DistancesComputer.levelColSize -2 ; j++) {
				
				if('A' <= n.boxes[i][j] && n.boxes[i][j] <= 'Z' && n.myBoxesFinal.contains(new Box(n.boxes[i][j], n.theAgentColor, new Position(i, j)))) {
					boxesPosition.add(
							new Box(n.boxes[i][j], n.theAgentColor, new Position(i, j)));
				}			
			}		
		}
		
		return boxesPosition;
	}

	public abstract int f(Node n);

	@Override
	public int compare(Node n1, Node n2) {

		return this.f(n1) - this.f(n2);
	}

	public static class AStar extends Heuristic {
		public AStar(Node initialState) {
			super(initialState);
		}

		@Override
		public int f(Node n) {
			return n.g() + this.h(n);
		}

		@Override
		public String toString() {
			return "A* evaluation";
		}
	}

	public static class WeightedAStar extends Heuristic {
		private int W;

		public WeightedAStar(Node initialState, int W) {
			super(initialState);
			this.W = W;
		}

		@Override
		public int f(Node n) {
			return n.g() + this.W * this.h(n);
		}

		@Override
		public String toString() {
			return String.format("WA*(%d) evaluation", this.W);
		}
	}

	public static class Greedy extends Heuristic {
		public Greedy(Node initialState) {
			super(initialState);
		}

		@Override
		public int f(Node n) {
			return this.h(n);
		}

		@Override
		public String toString() {
			return "Greedy evaluation";
		}
	}
}
