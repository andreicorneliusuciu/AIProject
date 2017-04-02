
//ToDO change pull/push in node to check if color is the same. Maybe then all agents can see all boxes without problems.
//toDo store info on agent like: agentNumber,row,col
//reason all agents appear once and as 0 is node.toString, fix needed


package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import searchclient.Heuristic.AStar;
import searchclient.Heuristic.Greedy;
import searchclient.Heuristic.WeightedAStar;
import searchclient.Strategy.StrategyBFS;
import searchclient.Strategy.StrategyBestFirst;
import searchclient.Strategy.StrategyDFS;

public class SearchClient {
	//The list of initial state for every agent
	public List<Node> initialStates;
	public static boolean[][] walls;
	public static char[][] goals; 
	
	//The list of agents. Index represents the agent, the value is the color
	public static Map<Integer,String> agents;
	//agent is implied in position, row,col stored
	public static int[][] agentLocation = new int[10][2];
	
	//Key = Color, Value = List of Box numbers. 
//	public Map<String, List<Integer>> colorToBoxes = new HashMap<>();
//
//	public Map<Integer, String> boxIDToColor = new HashMap<>();
	
	//map containing BoxName char,BoxColor string. Used for learning quickly what color a box is. 
	//(same name same color)
	//We need to store the exact boxes and their positions in other means.
	
	public static Map<Character, String> colorToBoxes = new HashMap<>();

	public static ArrayList<Integer> goalRow;
	public static ArrayList<Integer> goalCol;

	public SearchClient(BufferedReader serverMessages) throws Exception {

		String line = serverMessages.readLine();
		ArrayList<String> lines = new ArrayList<String>();
		agents = new HashMap<Integer,String>();
		
//		for(int i = 0; i < 10; i++){
//			
//			agents.add("NULL");
//			
//		}
		
		int maxCol = 0;
		while (!line.equals("")) {
			// Read lines specifying colors
			//^[a-z]+:\\s*[0-9A-Z](\\s*,\\s*[0-9A-Z])*\\s*$
			if (!line.startsWith("+")) {
				
				//System.err.println("Yes it does");
				String[] s = line.split(":");
				
				String color = s[0];

				String[] s1 = s[1].split(",");
				s1[0] = s1[0].trim();

				for (int i = 0; i < s1.length; i++) {
					char chr = s1[i].charAt(0);
					//System.err.println("Index = " + chr + " Color = " + color);
					//Agent
					if ('0' <= chr && chr <= '9') {
						//System.err.println("------- > Index = " + Integer.parseInt(""+chr) + " Color = " + color);
						//agents.add(Integer.parseInt(""+chr), color);
						
						agents.put(Integer.parseInt(""+chr), color);
						//we add all agents explicitly marked here. Blue agents are added when the level is parsed below.
					
					//Box
					} else if ('A' <= chr && chr <= 'Z') {
							//It is not on the map
							if(!colorToBoxes.containsKey(chr)){
								//List boxes = new LinkedList<String>();
								//boxes.add(""+chr);
								//colorToBoxes.put(color, boxes);
								colorToBoxes.put(chr, color);
								
							//It is already in the map. Update value
//							} else {
//								List boxes = colorToBoxes.get(color);
//								boxes.add(""+chr);
//								colorToBoxes.put(color, boxes);
							}
					} else {
						
						//agents.put(0, "Blue");
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
		
		//System.err.println("Agents: " + Arrays.asList(agents));
		//System.err.println("Colors to box map: " + colorToBoxes);
		
		//Create the list of initial states for all the agents
		//Ignore the other agents/boxes
		this.initialStates = new LinkedList<>();
		//add the node to the list. The index represents the agent.
		//initialStates.add(new Node(null, lines.size(), maxCol));
		//initialStates.add(new Node(null, lines.size(), maxCol));
		
		
		walls = new boolean[lines.size()][maxCol];
		goals = new char[lines.size()][maxCol];
		goalRow = new ArrayList<Integer>();
		goalCol = new ArrayList<Integer>();
		
		
		
		for (String l : lines) {
			if(!l.startsWith("+")) {
				continue;
			}
			for (int col = 0; col < l.length(); col++) {
				char chr = l.charAt(col);

				if (chr == '+') { // Wall.
					// this.initialState.walls[row][col] = true;
					walls[row][col] = true;
				} else if ('0' <= chr && chr <= '9') { // Agent.

					agentLocation[Character.getNumericValue(chr)][0]= row;
					agentLocation[Character.getNumericValue(chr)][1]= col;

					
					if(!agents.containsKey(Character.getNumericValue(chr))){
						
						agents.put(Character.getNumericValue(chr), "blue");
						
					//	System.err.println("These are the agents/colors of the level" +  Arrays.asList(agents));

						
					}
//					agents.add("null");
//TODO: Modify intial state to have an array of agents 
					initialStates.add(new Node(null, lines.size(), maxCol));

					this.initialStates.get(Integer.parseInt(""+chr)).agentRow = row;
					this.initialStates.get(Integer.parseInt(""+chr)).agentCol = col;
				} else if ('A' <= chr && chr <= 'Z') { // Box.
					this.initialStates.get(0).boxes[row][col] = chr;
					this.initialStates.get(1).boxes[row][col] = chr;
										
					if(!colorToBoxes.containsKey(chr))
					{
						//default everything not explicitly defined to blue
					colorToBoxes.put(chr, "blue");
					}
				} else if ('a' <= chr && chr <= 'z') { // Goal.
					// this.initialState.goals[row][col] = chr;
					goals[row][col] = chr;
					goalRow.add(row);
					goalCol.add(col);
				} else if (chr == ' ') {

				} else {
					System.err.println("Error, read invalid level character: " +  chr);
					System.exit(1);
				}
			}
			row++;
		}
		
		System.err.println("These are the agents/colors of the level" +  Arrays.asList(agents));
		System.err.println("These are the boxes/colors of the level" +  Arrays.asList(colorToBoxes));

		

//		for (Iterator<String> iterator = agents.iterator(); iterator.hasNext();) {
//		    String string = iterator.next();
//		    if (string.equals("NULL")) {
//		        // Remove the current element from the iterator and the list.
//		        iterator.remove();
//		    }
//		}
		
		// AnalLevel anal = new AnalLevel();
		// for (int i = 0; i < goalRow.size(); i++) {
		//
		//
		// anal.analGoals(goalRow.get(i), goalCol.get(i));
		// }

	}

	public LinkedList<Node> Search(Strategy strategy, Node initialNode) throws IOException {
		System.err.format("Search starting with strategy %s.\n", strategy.toString());
		strategy.addToFrontier(initialNode);

		int iterations = 0;
		while (true) {
			if (iterations == 1000) {
				//System.err.println(strategy.searchStatus());
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
			for (Node n : leafNode.getExpandedNodes()) { // The list of expanded
															// nodes is shuffled
															// randomly; see
															// Node.java.
				if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
					strategy.addToFrontier(n);
				}
			}
			iterations++;
		}
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
//			case "-astar":
//				strategy = new StrategyBestFirst(new AStar(client.initialStates.get(0)));
//				break;
//			case "-wastar":
//				// You're welcome to test WA* out with different values, but for
//				// the report you must at least indicate benchmarks for W = 5.
//				strategy = new StrategyBestFirst(new WeightedAStar(client.initialState, 5));
//				break;
//			case "-greedy":
//				strategy = new StrategyBestFirst(new Greedy(client.initialState));
//				break;
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
		LinkedList<Node> sol2;
		try {
			solution = client.Search(strategy, client.initialStates.get(0));
			System.err.println("Found solution for agent 0");
		} catch (Exception ex) {
			//System.err.println("Maximum memory usage exceeded.");
			System.err.println("Problems for agent 0");
			solution = null;
		}
		
		//duplicate
		try {
			sol2 = client.Search(new StrategyBFS(), client.initialStates.get(1));
			System.err.println("Found solution for agent 1");
		} catch (OutOfMemoryError ex) {
			System.err.println("Maximum memory usage exceeded.");
			sol2 = null;
		}
		//end of duplicate

		if (solution == null) {
//			System.err.println(strategy.searchStatus());
			System.err.println("Unable to solve level.");
			
			System.exit(0);
		} else {
//			System.err.println("\nSummary for " + strategy.toString());
//			System.err.println("Found solution of length " + solution.size());
//			System.err.println(strategy.searchStatus());
			//Multi-agent commands

			System.err.println("Sol length for agent 0: " + solution.size());
			System.err.println("Sol length for agent 1: " + sol2.size());

//			System.out.println(currentAction);
				for (int j = 0; j < 100; j++) {
					String act = "NoOp";
					String act1 = "NoOp";
					try {
						act = solution.get(j).action.toString();
						
					} catch(IndexOutOfBoundsException e) {
						


					}
					try {
						act1 = sol2.get(j).action.toString();
						
					} catch(IndexOutOfBoundsException e) {
						System.err.format("Exception in the moves of agent 1");


					}
					
					System.err.println("These are the agent positions of the level" +  Arrays.deepToString(agentLocation));

					String currentAction = "[" + act + ", " + act1 + "]";
					System.out.println(currentAction);
					System.err.println("===== " + currentAction + " ====");
					String response = serverMessages.readLine();
					if (response.contains("false")) {
						System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, act);
						System.err.format("%s was attempted in \n%s\n", act, "Dont care");
						break;
					}

			}
		}
	}
}
