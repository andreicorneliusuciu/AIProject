package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
	public int[][] flowFill(Agent agent, Node node){
		int[][] matrix = new int[rowSize][colSize];
		
		for(int i = 0; i<rowSize; i++){
			for(int i2 = 0; i2<colSize; i2++){
				if(walls[i][i2]){
					matrix[i][i2] = Integer.MAX_VALUE;//Wall!
				}
			}
		}
		
		for(int i = 0; i<node.boxes2.size(); i++){
			matrix[node.boxes2.get(i).position.row][node.boxes2.get(i).position.col] = node.boxes2.get(i).name;
		}
		
		//flowfill algorithm
		ArrayList<Position> flow = new ArrayList<Position>();
		flow.add(agent.position);
		
		return null;
	}
	
	//The list of initial state for every agent
	//public List<Node> initialStates;
	public static boolean[][] walls;
	
	//The size of the map
	public static int colSize;
	public static int rowSize;
	
	//The list of agents. Index represents the agent, the value is the color
	public static List<Agent> agents;
	
	//List with all the goals.
	public static List<Goal> allGoals;
	
	//Key = Color, Value = List of Box numbers
	public Map<String, List<Integer>> colorToBoxes = new HashMap<>();
	
	public Map<Character, String> boxesToColor = new HashMap<>();
	
	//color to agent map
	public Map<String, Character> colorToAgent = new HashMap<>();

	public SearchClient(BufferedReader serverMessages) throws Exception {

		String line = serverMessages.readLine();
		ArrayList<String> lines = new ArrayList<String>();
		agents = new ArrayList<Agent>();
		allGoals = new ArrayList<>();
		
		int maxCol = 0;
		while (!line.equals("")) {
			// Read lines specifying colors of the boxes and the agents
			if (!line.startsWith("+")) {
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
		//this.initialStates = new LinkedList<>();
		//add the node to the list. The index represents the agent.

		for(int i = 0; i < agents.size(); i++) {
			agents.get(i).assignInitialState(new Node(null, lines.size(), maxCol));
			//initialStates.add(new Node(null, lines.size(), maxCol));
		}
		
		walls = new boolean[lines.size()][maxCol];
		this.rowSize = lines.size();//Keeps track of map size
		this.colSize = maxCol;

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
					//if I find an agent with no color I make it blue
					//TODO
					int index = agents.indexOf(new Agent(Integer.parseInt(""+chr), null));
					if(index == -1) {
						agents.add(new Agent(Integer.parseInt(""+chr), "blue", new Position(row, col), new Node(null, lines.size(), maxCol)));
						//agents.add(new Node(null, lines.size(), maxCol));
					} else {
						//update the position of the agents declared above the map into the input file
						Agent a = agents.get(index);
						a.position.row = row;
						a.position.col = col;
					}
					agents.get(Integer.parseInt(""+chr)).initialState.agentRow = row;
					agents.get(Integer.parseInt(""+chr)).initialState.agentCol = col;
					
				} else if ('A' <= chr && chr <= 'Z') { // Box.
					if(!boxesToColor.containsKey(chr)) {
						boxesToColor.put(chr, "blue");
					}
					for(int i = 0; i < agents.size(); i++) {
						//if the color of the box is the same as the agent => put it into the agent's initial map
						if(boxesToColor.get(chr).equals(agents.get(i).color)) {
							agents.get(i).initialState.boxes[row][col] = chr;
							agents.get(i).initialState.boxes2.add(new Box(chr, boxesToColor.get(chr), new Position(row, col)));
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
							agents.get(i).initialState.goals[row][col] = chr;
							agents.get(i).initialState.goals2.add(new Goal(chr, agents.get(i).color, new Position(row, col)));
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
		System.err.println(" + Goals: " + allGoals);
		System.err.println("\n ------------------------------------ \n");
		int i = 0;
		for (Agent a : agents) {
			
			//agents.get(i).initialState = n;
			//i++;
			System.err.println("\n $ Goals: " + a.initialState.goals2 + " Boxes: "  +a.initialState.boxes2); 
		}
		System.err.println("\n ------------------------------------ \n");
	
	
	
    if(agents.get(0)!=null)
    {
//        System.err.println("\n Not null"+agents.get(1));
        Planner plan = new Planner(agents.get(0).initialState);
        
    }

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
		
		//List containing all the solutions for every agent
		List<List<Node>> solutions = new ArrayList<>();
			
		for(int i = 0; i < agents.size(); i++) {
			try {
				solution = client.Search(new StrategyBFS(), SearchClient.agents.get(i).initialState);
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
//			System.err.println("\nSummary for " + strategy.toString());
//			System.err.println("Found solution of length " + solution.size());
//			System.err.println(strategy.searchStatus());
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
