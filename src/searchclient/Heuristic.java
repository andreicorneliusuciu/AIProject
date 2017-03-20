package searchclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import searchclient.NotImplementedException;

public abstract class Heuristic implements Comparator<Node> {

	
	ArrayList<Integer> boxRow;
	ArrayList<Integer> boxCol;
	ArrayList<Integer> boxGoaldistList;
	ArrayList<Integer> boxAgentdistList;
	int aCol;
	int aRow ;
	
	public Heuristic(Node initialState) {
		// Here's a chance to pre-process the static parts of the level.
	}

	public int boxGoalDist(int lastEleCol,int lastEleRow, int g){
		
		int boxgoaldist = Math.abs(boxCol.get(lastEleCol) - SearchClient.goalCol.get(g))
				+ Math.abs(boxRow.get(lastEleRow) - SearchClient.goalRow.get(g));
		
		int boxagent = Math.abs(boxCol.get(lastEleCol)-aCol)+Math.abs(boxRow.get(lastEleCol)-aRow);
		
		 return boxgoaldist*2+boxagent;
		
	}
	
	
	
	public int h(Node n) {

		aCol = n.agentCol;
		aRow = n.agentRow;

		 boxRow = new ArrayList<Integer>();
		 boxCol = new ArrayList<Integer>();
		boxGoaldistList = new ArrayList<Integer>();
		int max=0;
		char[][] boxes = n.boxes;
		
		for (int row = 0; row < Node.MAX_ROW; row++) {

			for (int col = 0; col < Node.MAX_COL; col++) {
				if (boxes[row][col] > 0) {
					boxRow.add(row);
					boxCol.add(col);

					for (int g = 0; g < SearchClient.goalCol.size(); g++) {

						Integer gRow = SearchClient.goalRow.get(g);
						Integer gCol = SearchClient.goalCol.get(g);

						 
						if (Character.toLowerCase(boxes[row][col]) == SearchClient.goals[gRow][gCol]) {

							
							
							
							boxGoaldistList.add(boxGoalDist(boxCol.size() - 1,boxRow.size() - 1,g));
							
						//	boxAgentdistList.add(boxAgentDist(boxCol.size() - 1,boxRow.size() - 1,g));
							
								
							
						//	System.err.println("HEJ MED DIG");
						}

					}

				}
			}
		}
		
//		for(int i=0; i<boxGoaldistList.size();i++){
			
		//	System.err.println(boxGoaldistList.get(i));
			
//		}
		
		Integer min = Collections.min(boxGoaldistList);
		
		

	//	System.err.println("box Col/row: " + boxes[boxRow.get(0)][boxCol.get(0)] + "       ( " + boxCol.get(0) + ","
	//			+ boxRow.get(0) + " )" + " goal State: "
	//			+ SearchClient.goals[SearchClient.goalRow.get(1)][SearchClient.goalCol.get(1)]);
		return max;
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
