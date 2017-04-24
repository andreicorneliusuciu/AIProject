package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

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
	
	//The list of agents. Index represents the agent, the value is the color
	public static List<Agent> agents;
	
	//List with all the goals.
	public static List<Goal> allGoals;
	
	//Key = Color, Value = List of Box numbers
	public Map<String, List<Integer>> colorToBoxes = new HashMap<>();
	
	public Map<Character, String> boxesToColor = new HashMap<>();
	
	//color to agent map
	public Map<String, Character> colorToAgent = new HashMap<>();
	
	//the map represented as a matrix for computing the shortest distances
	//between all two pair of cells on the map
	public int[][] map;
	public static int levelRowSize;
	public static int levelColumnSize;

	public SearchClient(BufferedReader serverMessages) throws Exception {

		String line = serverMessages.readLine();
		ArrayList<String> lines = new ArrayList<String>();
		agents = new ArrayList<Agent>();
		allGoals = new ArrayList<>();
		
		int maxCol = 0;
		int noOfActualRowsForTheLevel = 0;
		while (!line.equals("")) {
			// Read lines specifying colors of the boxes and the agents
			if (!line.startsWith("+")) {
				noOfActualRowsForTheLevel++;
				String[] s = line.split(":");
				String color = s[0];
				String[] s1 = s[1].split(",");
				s1[0].trim();

				for (int i = 0; i < s1.length; i++) {
					char chr = s1[i].trim().charAt(0);
					
					//Agent
					if ('0' <= chr && chr <= '9') {
						Agent agent = new Agent(Integer.parseInt(""+chr), color, new Position(), null);
						agents.add(agent);
					
					//Box
					} else if ('A' <= chr && chr <= 'Z') {
							//It is not on the map
//							if(!colorToBoxes.containsKey(color)){
//								List boxes = new LinkedList<String>();
//								boxes.add(""+chr);
//								colorToBoxes.put(color, boxes);
//							//It is already in the map. Update value
//							} else {
//								List boxes = colorToBoxes.get(color);
//								boxes.add(""+chr);
//								colorToBoxes.put(color, boxes);
//							}
						//TODO: in the second loop, put the default ones
						boxesToColor.put(chr, color);
					} else {
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

//		for(int i = 0; i < agents.size(); i++) {
//			initialStates.add(new Node(null, lines.size(), maxCol));
//		}
		
		//lines.size() gives the no of rows on the map. maxCol is the no of columns
		levelRowSize = lines.size() - noOfActualRowsForTheLevel;
		levelColumnSize = maxCol;
		System.err.println("Row = " + levelRowSize +  "  CCOL  = " + levelColumnSize);
		walls = new boolean[levelRowSize][maxCol];
		map = new int[levelRowSize][maxCol];
		
		for(int i = 0; i < agents.size(); i++) {
			initialStates.add(new Node(null, levelRowSize, maxCol));
		}

		for (String l : lines) {
			if(!l.startsWith("+")) {
				continue;
			}
			for (int col = 0; col < l.length(); col++) {
				char chr = l.charAt(col);
				
				//update the general map => omit the agents and boxes
				if(chr == '+') {
					map[row][col] = -1;
				} else {
					map[row][col] = 0;
				}
				
				if (chr == '+') { // Wall.
					// this.initialState.walls[row][col] = true;
					walls[row][col] = true;
				} else if ('0' <= chr && chr <= '9') { // Agent.
					//if I find an agent with no color I make it blue
					//TODO
					int index = agents.indexOf(new Agent(Integer.parseInt(""+chr), null));
					if(index == -1) {
						agents.add(new Agent(Integer.parseInt(""+chr), "blue", new Position(row, col), null));
						initialStates.add(new Node(null, lines.size(), maxCol));
					} else {
						//update the position of the agents declared above the map into the input file
						Agent a = agents.get(index);
						a.position.row = row;
						a.position.col = col;
					}
					this.initialStates.get(Integer.parseInt(""+chr)).agentRow = row;
					this.initialStates.get(Integer.parseInt(""+chr)).agentCol = col;
					
				} else if ('A' <= chr && chr <= 'Z') { // Box.
					if(!boxesToColor.containsKey(chr)) {
						boxesToColor.put(chr, "blue");
					}
					for(int i = 0; i < agents.size(); i++) {
						//if the color of the box is the same as the agent => put it into the agent's initial map
						if(boxesToColor.get(chr).equals(agents.get(i).color)) {
							this.initialStates.get(i).boxes[row][col] = chr;
							this.initialStates.get(i).boxes2.add(new Box(chr, boxesToColor.get(chr), new Position(row, col)));
						}
					}
				} else if ('a' <= chr && chr <= 'z') { // Goal.
					//if I find the goal before the corresponding box on the map (I read left to right)
					if(!boxesToColor.containsKey(Character.toUpperCase(chr))) {
						boxesToColor.put(Character.toUpperCase(chr), "blue");
					}
					
					allGoals.add(new Goal(chr, boxesToColor.get(Character.toUpperCase(chr)), new Position(row, col)));
					
					//TODO Andrei: solve the default case??? when agent is blue
					for(int i = 0; i < agents.size(); i++) {
						//put the goal to the agent map just if they are the same color
						if(agents.get(i).color.equals(boxesToColor.get(Character.toUpperCase(chr)))) {
							this.initialStates.get(i).goals[row][col] = chr;
							this.initialStates.get(i).goals2.add(new Goal(chr, agents.get(i).color, new Position(row, col)));
						}
					}
				} else if (chr == ' ') {

				} else {
					System.err.println("Error, read invalid level character: " +  chr);
					System.exit(1);
				}
			}
			row++;
		}
		
		System.err.println(" + Agents: " + agents);
		System.err.println(" + Boxes: " + boxesToColor);
		//TODO Andrei: sort the allGoals list alphabetically
		System.err.println(" + Goals: " + allGoals);
		System.err.println("\n ------------------------------------ \n");
		int i = 0;
		for (Node n : initialStates) {
			
			agents.get(i).initialState = n;
			i++;
			System.err.println("\n $ Goals: " + n.goals2 + " Boxes: "  +n.boxes2); 
		}
		
		int[][] mapWithoutBorders = new int[levelRowSize-2][levelColumnSize-2];
		for(int i1 = 1; i1 < levelRowSize-1; i1++) {
			for(int j1 = 1; j1 < levelColumnSize-1; j1++) {
				mapWithoutBorders[i1-1][j1-1] = map[i1][j1];
			}
		}
		
		System.err.println("\n ------------------------------------");
		System.err.println("^^^^^^^^ THE MAP Without Borders: ^^^^^^^");
		
		for(int i1 = 0; i1 <  levelRowSize-2; i1++) {
			for (int j = 0; j < levelColumnSize-2; j++) {
				System.err.print(mapWithoutBorders[i1][j]);
			}
			System.err.println("");
		}
		
		System.err.println(" ^^^^^^^^ THE MAP END ^^^^^^^");
		
		//Compute all the distances on a NxN map. It does not work for non square maps.
		DistancesComputer distancesComputer = new DistancesComputer(mapWithoutBorders);
		distancesComputer.computeDistanceBetweenTwoPoints(new Position(0,0),
				new Position(levelRowSize-3,levelColumnSize-3));
		
		//Test distances function
		System.err.println("Distance between (5,0) and (7,0) = " +  
		DistancesComputer.getDistanceBetween2Positions(new Position(5,0),
				new Position(7,0)));
		
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
			case "-astar":
				strategy = new StrategyBestFirst(new AStar(client.initialStates.get(0)));
				break;
			case "-wastar":
				// You're welcome to test WA* out with different values, but for
				// the report you must at least indicate benchmarks for W = 5.
				strategy = new StrategyBestFirst(new WeightedAStar(client.initialStates.get(0), 5));
				break;
			case "-greedy":
				strategy = new StrategyBestFirst(new Greedy(client.initialStates.get(0)));
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
		
		//List containing all the solutions for every agent
		List<List<Node>> solutions = new ArrayList<>();
			
		for(int i = 0; i < agents.size(); i++) {
			try {
				solution = client.Search(new StrategyBFS(), client.initialStates.get(i));
				//add the partial solution to the list of total solutions
				solutions.add(solution);
					
				System.err.println("Found solution for agent " + i + " of size " + solution.size());			
			} catch (Exception ex) {
				//System.err.println("Maximum memory usage exceeded.");
				System.err.println("Problems for agent " + i + " when solving the level");
				solutions = null;
			}	
		}
	

		if (solutions == null) {
			System.err.println(strategy.searchStatus());
			System.err.println("Unable to solve level.");
			
			System.exit(0);
			
		} else {
			System.err.println("\nSummary for " + strategy.toString());
//			System.err.println("Found solution of length " + solutions.size());
			System.err.println(strategy.searchStatus());
			//Multi-agent commands
			
			int maxSol = 0;
			int m;
			for (int i = 0; i < solutions.size(); i++) {
				m = solutions.get(i).size();
				if (m > maxSol) {
					maxSol = m;
				}
			}
			
			//TODO: empty the same string builder object
			for(int i = 0; i < maxSol; i++) {
			
				StringBuilder jointAction = new StringBuilder();
				
				jointAction.append('[');
				for(int j = 0; j < solutions.size(); j++) {
					Node n = null;
					try {
						n = solutions.get(j).get(i);
						jointAction.append(n.action.toString() + ",");
					} catch(IndexOutOfBoundsException e) {
						jointAction.append("NoOp,");
					}
				}
				//replace the last comma with ']'
				jointAction.setCharAt(jointAction.length() - 1, ']');
				System.out.println(jointAction.toString());
				System.err.println("===== " + jointAction.toString() + " ====");
				String response = serverMessages.readLine();
				if (response.contains("false")) {
					System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, jointAction.toString());
					System.err.format("%s was attempted in \n%s\n", jointAction.toString(), "Problems with the moves");
					break;
				}
			}
		}
	}
}
/*
 * TODO Andrei: update the non square maps with wall on the empty spaces
 * - update the boxes2 and the goals list in the Node class
 * - call the heuristic function with the distances*/

