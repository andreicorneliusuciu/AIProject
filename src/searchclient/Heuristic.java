package searchclient;

import java.util.Comparator;
import java.util.Set;
import java.util.List;
import java.lang.Math;

import searchclient.NotImplementedException;
import searchclient.Node;

public abstract class Heuristic implements Comparator<Node> {
    
    //This is the goal state, represented as a list of the final intended positions of the boxes
    public List<Goal> goals;
    public List<Box> boxes;
        
	public Heuristic(Node initialState) {
		// Here's a chance to pre-process the static parts of the level.
		//TODO: I guess I need to make a list of positions in the Node class
        goals = initialState.goals2;
        boxes = initialState.boxes2;
	}

	public int h(Node n) {
       	//Manhattan distance Math.abs(x1-x0) + Math.abs(y1-y0);
       	
       	int result = 0;
		//Manhattan distance between box and its specific goal a -> A, b -> B etc.
//		for(int i = 0; i < Node.ARRAY_LENGHT; i++) {
//			result += Math.abs(n.boxes2.get(i).row - goals.get(i).row) + Math.abs(n.boxes2.get(i).col - goals.get(i).col);
//		}

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
