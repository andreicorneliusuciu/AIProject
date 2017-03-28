package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import searchclient.Memory;
import searchclient.Strategy.*;
import searchclient.Heuristic.*;

public class SearchClient {
	public Node initialState;
	public static boolean[][] walls;
	public static char[][] goals; 
	public static List<String> agents; // clear the null characters. 

	public Map<String, List<Integer>> colorToBoxes = new HashMap<>();

	public Map<Integer, String> boxIDToColor = new HashMap<>();

	public static ArrayList<Integer> goalRow;
	public static ArrayList<Integer> goalCol;

	public SearchClient(BufferedReader serverMessages) throws Exception {

		String line = serverMessages.readLine();
		ArrayList<String> lines = new ArrayList<String>();
		agents = new ArrayList<String>();
		
		for(int i=0;i<10;i++){
			
			agents.add("NULL");
			
		}
		
		int maxCol = 0;
		while (!line.equals("")) {
			// Read lines specifying colors
			if (line.matches("^[a-z]+:\\s*[0-9A-Z](\\s*,\\s*[0-9A-Z])*\\s*$")) {
				String[] s = line.split(":");

				String color = s[0];

				String[] s1 = s[1].split(",");
				s1[0].trim();

				for (int i = 0; i < s1.length; i++) {
					char chr = s1[i].charAt(0);
					if ('0' <= chr && chr <= '9') {

						agents.add(Integer.parseInt(""+chr),color);
						
					} else if ('A' <= chr && chr <= 'Z') { //Box 
							if(!colorToBoxes.containsKey(color)){
								List boxes = new LinkedList<String>();
								boxes.add(""+chr);
								 colorToBoxes.put(color, boxes);
							
							}else{
								List boxes = colorToBoxes.get(color);
								boxes.add(""+chr);
								colorToBoxes.put(color, boxes);
							}
					} else {
						System.exit(1);
						// WRONG ERROR
					}

				}

			}

			if (line.length() > maxCol) {
				maxCol = line.length();
			}

			lines.add(line);

			line = serverMessages.readLine();
		}

		int row = 0;
		boolean agentFound = false;
		this.initialState = new Node(null, lines.size(), maxCol);
		walls = new boolean[lines.size()][maxCol];
		goals = new char[lines.size()][maxCol];
		goalRow = new ArrayList<Integer>();
		goalCol = new ArrayList<Integer>();

		for (String l : lines) {
			for (int col = 0; col < l.length(); col++) {
				char chr = l.charAt(col);

				if (chr == '+') { // Wall.
					// this.initialState.walls[row][col] = true;
					walls[row][col] = true;
				} else if ('0' <= chr && chr <= '9') { // Agent.

					agents.add("");

					this.initialState.agentRow = row;
					this.initialState.agentCol = col;
				} else if ('A' <= chr && chr <= 'Z') { // Box.
					this.initialState.boxes[row][col] = chr;
				} else if ('a' <= chr && chr <= 'z') { // Goal.
					// this.initialState.goals[row][col] = chr;
					goals[row][col] = chr;
					goalRow.add(row);
					goalCol.add(col);
				} else if (chr == ' ') {
					// Free space.
				} else {
					System.err.println("Error, read invalid level character: " + (int) chr);
					System.exit(1);
				}
			}
			row++;
		}
		// AnalLevel anal = new AnalLevel();
		// for (int i = 0; i < goalRow.size(); i++) {
		//
		//
		// anal.analGoals(goalRow.get(i), goalCol.get(i));
		// }

	}

	public LinkedList<Node> Search(Strategy strategy) throws IOException {
		System.err.format("Search starting with strategy %s.\n", strategy.toString());
		strategy.addToFrontier(this.initialState);

		int iterations = 0;
		while (true) {
			if (iterations == 1000) {
				System.err.println(strategy.searchStatus());
				iterations = 0;
			}

			if (strategy.frontierIsEmpty()) {
				return null;
			}

			Node leafNode = strategy.getAndRemoveLeaf();

			if (leafNode.isGoalState()) {
				return leafNode.extractPlan();
			}

			strategy.addToExplored(leafNode);
			
			for (Node n : leafNode.getExpandedNodes(3,4)) { // hmm ja ii desuyo neeeeee!  The list of expanded
															// nodes is shuffled
															// randomly; see
															// Node.java.
				if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
					strategy.addToFrontier(n);
				}
			}
			
			//agent creates plans from HTN functions
			//Searchclient conflict resolves
			iterations++;
		}
	}
	
	public LinkedList<Node> moveAgentToBox(int agent,Box box){
		return null;
	}
	public LinkedList<Node> moveBoxToGoal(int agent,Box box,int xgoal,int ygoal){
		return null;
	}
	public LinkedList<Node> moveBoxTo(int agent,Box box,int x,int y){
		return null;
	}
	
	public LinkedList<Node> freeAgent(int agent,int lockedAgent){
		return null;
	}
	
	public LinkedList<Node> storeBox(int agent,Box box){
		return null;
	}
	
	public LinkedList<Node> conflictResolution(int highAgent,int lowAgent, LinkedList<Node> highPlan, LinkedList<Node> lowPlan){
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));

		// Use stderr to print to console
		System.err.println("SearchClient initializing. I am sending this using the error output stream.");

		// Read level and create the initial state of the problem
		SearchClient client = new SearchClient(serverMessages);

		Strategy strategy;
		if (args.length > 0) {
			switch (args[0].toLowerCase()) {
			case "-bfs":
				strategy = new StrategyBFS();
				break;
			case "-dfs":
				strategy = new StrategyDFS();
				break;
			case "-astar":
				strategy = new StrategyBestFirst(new AStar(client.initialState));
				break;
			case "-wastar":
				// You're welcome to test WA* out with different values, but for
				// the report you must at least indicate benchmarks for W = 5.
				strategy = new StrategyBestFirst(new WeightedAStar(client.initialState, 5));
				break;
			case "-greedy":
				strategy = new StrategyBestFirst(new Greedy(client.initialState));
				break;
			default:
				strategy = new StrategyBFS();
				System.err.println(
						"Defaulting to BFS search. Use arguments -bfs, -dfs, -astar, -wastar, or -greedy to set the search strategy.");
			}
		} else {
			strategy = new StrategyBFS();
			System.err.println(
					"Defaulting to BFS search. Use arguments -bfs, -dfs, -astar, -wastar, or -greedy to set the search strategy.");
		}

		LinkedList<Node> solution;
		try {
			solution = client.Search(strategy);
		} catch (OutOfMemoryError ex) {
			System.err.println("Maximum memory usage exceeded.");
			solution = null;
		}

		if (solution == null) {
			System.err.println(strategy.searchStatus());
			System.err.println("Unable to solve level.");
			System.exit(0);
		} else {
			System.err.println("\nSummary for " + strategy.toString());
			System.err.println("Found solution of length " + solution.size());
			System.err.println(strategy.searchStatus());

			for (Node n : solution) {
				String act = n.action.toString();
				System.out.println(act);
				String response = serverMessages.readLine();
				if (response.contains("false")) {
					System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, act);
					System.err.format("%s was attempted in \n%s\n", act, n.toString());
					break;
				}
			}
		}
	}
}
