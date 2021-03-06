package searchclient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
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

				if (isCabin(i + 1, j, x, y) && isCabin(i + 1, j - 1, x, y) && isCabin(i + 1, j + 1, x, y)) {
					if (wallSpace[i + 1][j] && wallSpace[i + 1][j - 1] && wallSpace[i + 1][j + 1]) {
						count = 4; // top
					}
				}
				if (isCabin(i, j - 1, x, y) && isCabin(i + 1, j - 1, x, y) && isCabin(i - 1, j - 1, x, y)) {
					if (wallSpace[i][j - 1] && wallSpace[i + 1][j - 1] && wallSpace[i - 1][j - 1]) {
						count = 1; // left
					}
				}
				if (isCabin(i, j + 1, x, y) && isCabin(i + 1, j + 1, x, y) && isCabin(i - 1, j + 1, x, y)) {
					if (wallSpace[i][j + 1] && wallSpace[i + 1][j + 1] && wallSpace[i - 1][j + 1]) {
						count = 2; // right c
					}
				}
				if (isCabin(i - 1, j - 1, x, y) && isCabin(i - 1, j, x, y) && isCabin(i - 1, j + 1, x, y)) {
					if (wallSpace[i - 1][j - 1] && wallSpace[i - 1][j] && wallSpace[i - 1][j + 1]) {
						count = 3; // bottom
					}
				}
				// } catch (IndexOutOfBoundsException e) {
				// System.err.println(e);
				// }

				for (Boolean b : neighbors) {
					if (b) {
						wallCount++;
					}
				}
				if (count >= 1 && count <= 4 && wallCount <= 3) {
					theList.add(new Position(i, j));
				}

				wallCount = 0;
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
		// Manhattan distance Math.abs(x1-x0) + Math.abs(y1-y0);

		int result = 0;
		// Manhattan distance between box and its specific goal a -> A, b -> B
		// etc.
		// for(int i = 0; i < Node.ARRAY_LENGHT; i++) {
		// result += Math.abs(n.boxes2.get(i).row - goals.get(i).row) +
		// Math.abs(n.boxes2.get(i).col - goals.get(i).col);
		// }

		return result;
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
