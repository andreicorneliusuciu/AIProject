package searchclient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.lang.Math;

import searchclient.NotImplementedException;
import searchclient.Node;

public abstract class Heuristic implements Comparator<Node> {
    
	//fill by findStorage()
	public static List<Position> storageSpace = new ArrayList<Position>();
	
    //This is the goal state, represented as a list of the final intended positions of the boxes
    public List<Goal> goals;
    public List<Box> boxes;
        

	public Heuristic(Node initialState) {
		// Here's a chance to pre-process the static parts of the level.
		
		
		
		findStorage(initialState);
		
		//these use storage
		findBoxCluster(initialState); //maybe not needed
		findTrappedAgent(initialState);
		///////////////////
		
		findTrickyGoal(initialState); 
	
	}


	public boolean findBoxCluster(Node n)
	{
		
//		char[][] boxes = n.boxes;
//		Set<Character> neighbors = new HashSet<>();
//		Set<Box> clusteredBoxes;
//		
//		
//		for(int row=0; row<Node.MAX_ROW;row++)
//			for(int col=0; col<Node.MAX_COL; col++)
//			{
//				neighbors = getNeighbors(Node.MAX_ROW,Node.MAX_COL,row,col,boxes);
//				
//				//clusteredBoxes = new HashSet<Box>(neighbors);
//				
//				//System.err.println("i'm here toooooo");	
//				//how to print this shit
//				//for (Character s : neighbors) {
//									
//					//}
//				
//			}
		//todo.. Change getneighbors to +1 on boxes[i][j]>0. See toString for example.
		
		//System.err.println("Hashset contains" );
		return false;
	}
	
	
	//////////////methods for finding neighbors in array./////////
	
	///////For any 2D array cellValues[][] of (x,y) dimensions below code can be used for 
	///////getting all 8 neighbors for any cell (i,j). Code will return 0 by default./////
	public static Set<Character> getNeighbors(int i, int j, int x, int y, char[][] cellValues) {
	    Set<Character> neighbors = new HashSet<>();

	    if(isCabin(i, j, x, y)) {
	        if(isCabin(i + 1, j, x, y))
	            neighbors.add(cellValues[i+1][j]);
	        if(isCabin(i - 1, j, x, y))
	            neighbors.add(cellValues[i-1][j]);
	        if(isCabin(i, j + 1, x, y))
	            neighbors.add(cellValues[i][j+1]);
	        if(isCabin(i, j - 1, x, y))
	            neighbors.add(cellValues[i][j-1]);
	        if(isCabin(i - 1, j + 1, x, y))
	            neighbors.add(cellValues[i-1][j+1]);
	        if(isCabin(i + 1, j - 1, x, y))
	            neighbors.add(cellValues[i+1][j-1]);
	        if(isCabin(i + 1, j + 1, x, y))
	            neighbors.add(cellValues[i+1][j+1]);
	        if(isCabin(i - 1, j - 1, x, y))
	            neighbors.add(cellValues[i-1][j-1]);
	    }
	    return neighbors;
	}

	public static boolean isCabin(int i, int j, int x, int y) {
	    boolean flag = false;
	    if (i >= 0 && i <= x && j >= 0 && j <= y) {
	        flag = true;
	    }
	    return flag; 
	}
	
	
	///////////////////////
	
	
	
	
	
	public boolean findStorage(Node initialState)
	{
		//TODO finds cells that could be used as storage. Fills up storageSpace List<>. 
		//Returns true if one or more cells are marked as storage.
		
		return false;
	}
	
	
	public boolean findTrickyGoal(Node initialState)
	{
		//TODO find goals with limited access and set their priority via goal.priority
		//return true if one or more tricky goals have been found
		
		return false;
	}

	
	public boolean findTrappedAgent(Node initialState)
	{
		//TODO find agents with high probability of being trapped. Set agent.isTrapped to true
		//return true if trapped agent found
		
		return false;
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
