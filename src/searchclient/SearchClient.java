
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import searchclient.Heuristic.AStar;
import searchclient.Heuristic.Greedy;
import searchclient.Heuristic.WeightedAStar;
import searchclient.Strategy.StrategyBFS;
import searchclient.Strategy.StrategyBestFirst;
import searchclient.Strategy.StrategyDFS;
import sun.management.resources.agent;

public class SearchClient {
	//The list of initial state for every agent
	public List<Node> initialStates;
	//TODO: make this 2 matrices LinkedList<Position>
	public static boolean[][] walls;
	public static char[][] goals;
	
	//Initial boxes position (right at the beginning of the level)
	public List<Box> initialBoxes = new ArrayList<>();
	
	public static List<Goal> goalsList = new ArrayList<>();
	
	//The list of agents. Index represents the agent, the value is the color
	//public static Map<Integer,String> agents;
	
	public static List<Agent> agents = new ArrayList<>();
	
	//agent is implied in position, row,col stored
	public static int[][] agentLocation = new int[10][2];
	
	public static Map<Character, String> boxesToColors = new HashMap<>();

	public static ArrayList<Integer> goalRow;
	public static ArrayList<Integer> goalCol;
	

	public SearchClient(BufferedReader serverMessages) throws Exception {

		String line = serverMessages.readLine();
		ArrayList<String> lines = new ArrayList<String>();

		int maxCol = 0;
		while (!line.equals("")) {
			// Read lines specifying colors
			if (!line.startsWith("+")) {
				
				String[] s = line.split(":");
				String color = s[0];
				String[] s1 = s[1].split(",");
				s1[0] = s1[0].trim();

				for (int i = 0; i < s1.length; i++) {
					char chr = s1[i].charAt(0);
					//Agent
					if ('0' <= chr && chr <= '9') {
						//we add all agents explicitly marked here. Blue agents are added when the level is parsed below.
						agents.add(new Agent((int)chr, color, null, null));	
					//Box
					} else if ('A' <= chr && chr <= 'Z') {
							//It is not on the map
							if(!boxesToColors.containsKey(chr)) {
								boxesToColors.put(chr, color);
								//TODO: in the next parsing function make the boxes default.
							} 
					} else {
						//TODO: Catch an exeption.
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
					walls[row][col] = true;
				} else if ('0' <= chr && chr <= '9') { // Agent.

					agentLocation[Character.getNumericValue(chr)][0]= row;
					agentLocation[Character.getNumericValue(chr)][1]= col;
					
					
					if(!agents.contains(Character.getNumericValue(chr))) {
						
						//agents.add(Character.getNumericValue(chr), "blue");
						agents.add(new Agent((int)chr,"blue",new Position(0,0), new Node(null, 0, 0)));			//we add all agents explicitly marked here. Blue agents are added when the level is parsed below.

					}
					
					for(Agent a:agents){
						initialStates.add(new Node(null, lines.size(), maxCol));
					//initialStates.add(new Node(null, lines.size(), maxCol));
					}
					
					
					this.initialStates.get(Integer.parseInt(""+chr)).agentRow = row;
					this.initialStates.get(Integer.parseInt(""+chr)).agentCol = col;
				} else if ('A' <= chr && chr <= 'Z') { // Box.
					//Default color being used
					if(!boxesToColors.containsKey(chr)) {
						initialBoxes.add(new Box(chr, "blue", new Position(row, col)));
						System.err.println("Default color being used");
						
					} else initialBoxes.add(new Box(chr, boxesToColors.get(chr), new Position(row, col)));
					
				} else if ('a' <= chr && chr <= 'z') { // Goal.
					goalsList.add(new Goal(chr, new Position(row, col), boxesToColors.get(Character.toUpperCase(chr))));
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
		
		//TODO: iterate through all the agents and update the initial state for every one of them.
		
		System.err.println("\n\n -- Boxes are: " + initialBoxes);
		System.err.println("\n\n -- Goals are: " + goalsList);
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
