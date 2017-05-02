package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
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

	// The list of initial state for every agent
	// public List<Node> initialStates;
	public static boolean[][] walls;

	// The size of the map
//	public static int levelColSize;
//	public static int levelRowSize;

	// The list of agents. Index represents the agent, the value is the color
	public static List<Agent> agents;

	// List with all the goals.
	public static List<Goal> allGoals;
	// List with all the boxes.
	public static List<Box> allBoxes;

	// Key = Color, Value = List of Box numbers
	public Map<String, List<Integer>> colorToBoxes = new HashMap<>();

	public static Map<Character, String> boxesToColor = new HashMap<>();

	// color to agent map
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
			if (!line.startsWith("+")) { //or space
				noOfActualRowsForTheLevel++;
				String[] s = line.split(":");
				String color = s[0];
				String[] s1 = s[1].split(",");
				s1[0].trim();

				for (int i = 0; i < s1.length; i++) {
					char chr = s1[i].trim().charAt(0);

					// Agent
					if ('0' <= chr && chr <= '9') {
						Agent agent = new Agent(Integer.parseInt("" + chr), color, new Position(), null);
						agents.add(agent);

						// Box
					} else if ('A' <= chr && chr <= 'Z') {

						boxesToColor.put(chr, color);
					} else {
					}
				}
			}
			//careful when the level is narrow and the declarations are larger
			if (line.length() > maxCol) {
				maxCol = line.length();
			}

			lines.add(line);

			line = serverMessages.readLine();
		}

		int row = 0;
		// Create the list of initial states for all the agents
		// Ignore the other agents/boxes
		// this.initialStates = new LinkedList<>();
		// add the node to the list. The index represents the agent.

		
//		this.levelRowSize = levelRowSize;// Keeps track of map size
//		this.levelColSize = levelColumnSize;
    
		//levelRowSize gives the no of rows on the map. levelColumnSize is the no of columns
		levelRowSize = lines.size() - noOfActualRowsForTheLevel;
		levelColumnSize = maxCol;
		
		System.err.println("Row = " + levelRowSize +  "  CCOL  = " + levelColumnSize);
		walls = new boolean[levelRowSize][levelColumnSize];
		map = new int[levelRowSize][levelColumnSize];
		
// 		for(int i = 0; i < agents.size(); i++) {
// 			initialStates.add(new Node(null, levelRowSize, levelColumnSize));
// 		}
		for (int i = 0; i < agents.size(); i++) {
			agents.get(i).assignInitialState(new Node(null, levelRowSize, levelColumnSize));
			// initialStates.add(new Node(null, levelRowSize, levelColumnSize));
		}

		for (String l : lines) {
			if(!l.startsWith("+")){// && l.charAt(0) == ' ') {
//				if(l.startsWith(" ")) {
//					for (int col = 0; col < l.length(); col++) {
//						char chr = l.charAt(col);
//						
//						//if we have spaces or actual wall, we fill it with "walls"
//						if(chr == ' ') {
//							map[row][col] = -1;
//						} else if(chr == '+') {
//							map[row][col] = -1;
//							walls[row][col] = true;
//						}else {
//							map[row][col] = 0;
//						}
//					}
//					row++;
//				} else if(l.endsWith(" ")) { //fill with walls on the right part of the level
//					
//				} else
//					continue;
//			}
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
					// if I find an agent with no color I make it blue

					int index = agents.indexOf(new Agent(Integer.parseInt("" + chr), null));
					if (index == -1) {
						Agent agentT = new Agent(Integer.parseInt("" + chr), "blue", new Position(row, col),
								new Node(null, levelRowSize, levelColumnSize));
						agentT.initialState.theAgentColor = agentT.color;
						agentT.initialState.theAgentName = agentT.name;
						agentT.initialState.agentCol = agentT.position.col;
						agentT.initialState.agentRow = agentT.position.row;

						agents.add(agentT);

						// agents.add(new Node(null, levelRowSize, levelColumnSize));
					} else {
						// update the position of the agents declared above the
						// map into the input file
						Agent a = agents.get(index);
						a.position.row = row;
						a.position.col = col;
						a.initialState.theAgentColor = a.color;
						a.initialState.theAgentName = a.name;
						a.initialState.agentCol = a.position.col;
						a.initialState.agentRow = a.position.row;
						agents.set(index, a);
					}

				} else if ('A' <= chr && chr <= 'Z') { // Box.
					if (!boxesToColor.containsKey(chr)) {
						boxesToColor.put(chr, "blue");
					}
					for (int i = 0; i < agents.size(); i++) {
						// if the color of the box is the same as the agent =>
						// put it into the agent's initial map
						// if(boxesToColor.get(chr).equals(agents.get(i).color))
						// {
						agents.get(i).initialState.boxes[row][col] = chr;
						agents.get(i).initialState.boxes2
								.add(new Box(chr, boxesToColor.get(chr), new Position(row, col)));
						if(agents.get(i).color.equals(boxesToColor.get(chr))) {
							agents.get(i).initialState.myBoxesFinal
							.add(new Box(chr, boxesToColor.get(chr), new Position(row, col)));
						}
						// }
					}
				} else if ('a' <= chr && chr <= 'z') { // Goal.
					// if I find the goal before the corresponding box on the
					// map (I read left to right)
					if (!boxesToColor.containsKey(Character.toUpperCase(chr))) {
						boxesToColor.put(Character.toUpperCase(chr), "blue");
					}

					allGoals.add(new Goal(chr, boxesToColor.get(Character.toUpperCase(chr)), new Position(row, col)));

					for (int i = 0; i < agents.size(); i++) {
						// put the goal to the agent map just if they are the
						// same color
						if (agents.get(i).color.equals(boxesToColor.get(Character.toUpperCase(chr)))) {
							agents.get(i).initialState.goals[row][col] = chr;
							agents.get(i).initialState.goals2
									.add(new Goal(chr, agents.get(i).color, new Position(row, col)));
						}
					}
				} else if (chr == ' ') {

				} else {
					System.err.println("Error, read invalid level character: " + chr);
					System.exit(1);
				}
			}
			row++;
		}

		System.err.println(" + Agents: " + agents);
		Collections.sort(agents);
		System.err.println(" + Agents: " + agents);
		System.err.println(" + Boxes: " + boxesToColor);
		//TODO Andrei: sort the allGoals list alphabetically
		System.err.println(" + Goals: " + allGoals);
		System.err.println("\n ------------------------------------ \n");
		
		for (int i = 0; i < agents.size(); i++) {
			System.err.println("For agent " + i + ":");
			System.err.println("**** myBoxesFinal = " + agents.get(i).initialState.myBoxesFinal);
			System.err.println("&&&& Goals2 = " + agents.get(i).initialState.goals2);
			System.err.println(" --------------------- ");
		}
//		for (int i = 0 ; i < agents.size(); i++) {
//			
//			agents.get(i).initialState = n;
//			System.err.println("\n $ Goals: " + n.goals2 + " Boxes: "  +n.boxes2); 
//		}
		
//		int[][] mapWithoutBorders = new int[levelRowSize-2][levelColumnSize-2];
//		for(int i1 = 1; i1 < levelRowSize-1; i1++) {
//			for(int j1 = 1; j1 < levelColumnSize-1; j1++) {
//				mapWithoutBorders[i1-1][j1-1] = map[i1][j1];
//			}
//		}
		
		int[][] mapWithoutBorders = new int[levelRowSize][levelColumnSize];
		for(int i1 = 0; i1 < levelRowSize; i1++) {
			for(int j1 = 0; j1 < levelColumnSize; j1++) {
				mapWithoutBorders[i1][j1] = map[i1][j1];
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
		distancesComputer.computeDistanceBetweenTwoPoints(new Position(1,1),
				new Position(levelRowSize-1,levelColumnSize-1));
		
		//Test distances function
//		System.err.println("Distance between (5,0) and (7,0) = " +  
//		DistancesComputer.getDistanceBetween2Positions(new Position(0,0),
//				new Position(7,0)));
		//Test Independent goals
		
	}

	public LinkedList<Node> Search(Strategy strategy, Node initialNode) throws IOException {
		System.err.format("Search starting with strategy %s.\n", strategy.toString());
		strategy.addToFrontier(initialNode);

		int iterations = 0;
		while (true) {
			if (iterations == 1000) {
				// System.err.println(strategy.searchStatus());
				iterations = 0;
			}

			if (strategy.frontierIsEmpty()) {

				LinkedList<Node> noOpList = new LinkedList<Node>();
				initialNode.doNoOp = true;
				noOpList.add(initialNode);
				System.err.println("The frontier is empty");
				return noOpList;

			}

			Node leafNode = strategy.getAndRemoveLeaf();
			// System.err.println("Leafn" + leafNode + leafNode.parent);

			if (leafNode.isGoalState()) {
				//System.err.println("Returns" + leafNode.extractPlan());
				return leafNode.extractPlan();
			}

			strategy.addToExplored(leafNode);
			for (Node n : leafNode.getExpandedNodes()) { // The list of expanded
															// nodes is shuffled
															// randomly; see
															// Node.java.
				if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
					// System.err.println("Adding to frontier:
					// "+n.theAgentName+" "+n);
					strategy.addToFrontier(n);
				}
			}
			iterations++;
		}
	}

	public void storageAnalysis(Node node) {// Swap out input with other types
		// of data?
		Cell[][] map = new Cell[levelRowSize][levelColumnSize];

		// Initialize cell map
		for (int i = 0; i < levelRowSize; i++) {
			for (int i2 = 0; i2++ < levelColumnSize; i2++) {
				map[i][i2] = new Cell(i, i2);
				if (walls[i][i2]) {
					map[i][i2].type = 0;// Wall
				} else {
					map[i][i2].type = 1;// Free
				}
			}
		}
		for (Goal g : this.allGoals) {
			map[g.position.row][g.position.col].type = 2; // Goal
		}

		// Link up cells
		for (int i = 0; i < levelRowSize; i++) {
			for (int i2 = 0; i2++ < levelColumnSize; i2++) {
				if (map[i][i2].type >= 1) {
					if (map[i + 1][i2].type >= 1) {// + is south, right?
						map[i][i2].south = true;
					}
					if (map[i - 1][i2].type >= 1) {
						map[i][i2].north = true;
					}
					if (map[i][i2 + 1].type >= 1) {
						map[i][i2].east = true;
					}
					if (map[i][i2 - 1].type >= 1) {
						map[i][i2].west = true;
					}
				}
			}
		}

		// Create row objects
		ArrayList<Line> rows = new ArrayList<Line>();
		for (int i = 0; i < levelRowSize; i++) {
			for (int i2 = 0; i2++ < levelColumnSize; i2++) {
				if (map[i][i2].type == 1) {
					boolean connected = true;
					Line row = new Line();
					ArrayList<Position> positions = new ArrayList<Position>();
					while (connected) {
						if (map[i][i2].type == 1) {
							positions.add(map[i][i2].position);
							i2++;
						} else {
							connected = false;
						}
					}
					row.positions = positions;
					rows.add(row);
				} else if (map[i][i2].type == 2) {
					boolean connected = true;
					Line row = new Line();
					row.goalLine = true;
					ArrayList<Position> positions = new ArrayList<Position>();
					while (connected) {
						if (map[i][i2].type == 2) {
							positions.add(map[i][i2].position);
							i2++;
						} else {
							connected = false;
						}
					}
					row.positions = positions;
					rows.add(row);
				}
			}
		}

		// Create col objects
		ArrayList<Line> cols = new ArrayList<Line>();
		for (int i = 0; i < levelColumnSize; i++) {
			for (int i2 = 0; i2++ < levelRowSize; i2++) {
				if (map[i2][i].type == 1) {
					boolean connected = true;
					Line col = new Line();
					ArrayList<Position> positions = new ArrayList<Position>();
					while (connected) {
						if (map[i2][i].type == 1) {
							positions.add(map[i2][i].position);
							i2++;
						} else {
							connected = false;
						}
					}
					col.positions = positions;
					cols.add(col);
				} else if (map[i2][i].type == 2) {
					boolean connected = true;
					Line col = new Line();
					col.goalLine = true;
					ArrayList<Position> positions = new ArrayList<Position>();
					while (connected) {
						if (map[i2][i].type == 2) {
							positions.add(map[i2][i].position);
							i2++;
						} else {
							connected = false;
						}
					}
					col.positions = positions;
					cols.add(col);
				}
			}
		}

		// Connect them

		// Supernodes?
	}

	public static int[][] flowFill(Agent agent, Node node) {
		int[][] matrix = new int[levelRowSize][levelColumnSize];
		int[][] result = new int[levelRowSize][levelColumnSize];

		for (int i = 0; i < levelRowSize; i++) {
			for (int i2 = 0; i2 < levelColumnSize; i2++) {
				if (walls[i][i2]) {
					matrix[i][i2] = Integer.MAX_VALUE;// Wall!
					result[i][i2] = 1;// Wall
				}
			}
		}

		for (int i = 0; i < node.MAX_ROW; i++) {
			for (int i2 = 0; i2 < node.MAX_COL; i2++) {
				if (node.boxes[i][i2] != '\u0000') {
					matrix[i][i2] = boxesToColor.get(node.boxes[i][i2]).hashCode();
					// System.err.println(boxesToColor.get(node.boxes[i][i2]).hashCode());
					// System.err.println(agent.color.hashCode());
				}
			}
		}
		// Find all boxes colors and put them into Matrix

		// flowfill algorithm
		Set<Position> flow = new HashSet<Position>();
		Set<Position> blockage = new HashSet<Position>();
		flow.add(agent.position);
		boolean found = true;
		while (found) {
			int curSize = flow.size();
			Set<Position> tempFlow = new HashSet<Position>();
			for (Position p : flow) {
				if (p.row - 1 >= 0) {
					if (matrix[p.row - 1][p.col] == 0 || matrix[p.row - 1][p.col] == agent.color.hashCode()) {
						tempFlow.add(new Position(p.row - 1, p.col));
					} else if (matrix[p.row - 1][p.col] != Integer.MAX_VALUE) {// It
						// isn't
						// a
						// wall
						// or
						// itself,
						// so
						// it
						// must
						// be
						// a
						// different
						// colored
						// box
						blockage.add(new Position(p.row - 1, p.col));
					}
				}
				if (p.col - 1 >= 0) {
					if (matrix[p.row][p.col - 1] == 0 || matrix[p.row][p.col - 1] == agent.color.hashCode()) {
						tempFlow.add(new Position(p.row, p.col - 1));
					} else if (matrix[p.row][p.col - 1] != Integer.MAX_VALUE) {// It
						// isn't
						// a
						// wall
						// or
						// itself,
						// so
						// it
						// must
						// be
						// a
						// different
						// colored
						// box
						blockage.add(new Position(p.row, p.col - 1));
					}
				}
				if ((p.col + 1) < levelColumnSize) {
					if (matrix[p.row][p.col + 1] == 0 || matrix[p.row][p.col + 1] == agent.color.hashCode()) {
						tempFlow.add(new Position(p.row, p.col + 1));
					} else if (matrix[p.row][p.col + 1] != Integer.MAX_VALUE) {// It
						// isn't
						// a
						// wall
						// or
						// itself,
						// so
						// it
						// must
						// be
						// a
						// different
						// colored
						// box
						blockage.add(new Position(p.row, p.col + 1));
					}
				}
				if ((p.row + 1) < levelRowSize) {
					if (matrix[p.row + 1][p.col] == 0 || matrix[p.row + 1][p.col] == agent.color.hashCode()) {
						tempFlow.add(new Position(p.row + 1, p.col));
					} else if (matrix[p.row + 1][p.col] != Integer.MAX_VALUE) {// It
						// isn't
						// a
						// wall
						// or
						// itself,
						// so
						// it
						// must
						// be
						// a
						// different
						// colored
						// box
						blockage.add(new Position(p.row + 1, p.col));
					}
				}
			}
			for (Position p : tempFlow) {
				flow.add(p);
			}

			if (flow.size() == curSize) {
				found = false;
			}
		}
		for (Position p : flow) {
			result[p.row][p.col] = 2; // Free flow!
		}
		for (Position p : blockage) {
			result[p.row][p.col] = 3; // Blockage!
		}

		System.err.println("0");
		return result; // 0 = Unconnected, 1 = Wall, 2 = Free flow, 3 =
		// Blockage.
	}

	public static void rescueUnit(Agent agent, ArrayList<Position> positions, Node node) {// Analyse
		// all
		// the
		// agents!
		int[][] matrix = flowFill(agent, node);
		for (Position p : positions) {
			if (matrix[p.row][p.col] != 2) {// Blockage! Oh no!
				agent.isTrapped = true;
				for (int i = 0; i < levelRowSize; i++) {
					for (int i2 = 0; i2 < levelColumnSize; i2++) {
						if (matrix[i][i2] == 3) {
							if (matrix[i - 1][i2] == 0 || matrix[i + 1][i2] == 0 || matrix[i][i2 - 1] == 0
									|| matrix[i][i2 + 1] == 0) {
								for (Box b : allBoxes) {
									if (b.position.equals(new Position(i, i2))) {
										b.isBlocking = true;
										System.err.println("Box blocking found and is: " + b);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public List<Strategy> getStrategies(String str) {
		
		List<Strategy> strategies = new ArrayList<>(agents.size());
		
		
		switch (str.toLowerCase()) {
			case "-bfs":
				for (int i = 0; i < agents.size(); i++)
					strategies.add(new StrategyBFS());
				break;
			case "-dfs":
				for (int i = 0; i < agents.size(); i++)
					strategies.add(new StrategyDFS());
				System.err.println("DFS Strategy.");
				break;
			case "-astar":
				for (int i = 0; i < agents.size(); i++)
					strategies.add(new StrategyBestFirst(new AStar(agents.get(i).initialState)));
				System.err.println("A* Strategy.");
				break;
			case "-wastar":
				for (int i = 0; i < agents.size(); i++)
					strategies.add(new StrategyBestFirst(new WeightedAStar(agents.get(i).initialState, 5)));
				System.err.println("WA* Strategy.");
				break;
			case "-greedy":
				for (int i = 0; i < agents.size(); i++)
					strategies.add(new StrategyBestFirst(new Greedy(agents.get(i).initialState)));
				System.err.println("Greedy Best First Strategy.");
				break;
			default:
				for (int i = 0; i < agents.size(); i++)
					strategies.add(new StrategyBFS());
				System.err.println(
						"Defaulting to BFS search. Use arguments -bfs, -dfs, -astar, -wastar, or -greedy to set the search strategy.");
				break;
		}
		
		
		return strategies;
	}

	public static void main(String[] args) throws Exception {
		BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));

		// Use stderr to print to console
		System.err.println("SearchClient initializing. I am sending this using the error output stream.");

		// Read level and create the initial state of the problem
		SearchClient client = new SearchClient(serverMessages);

		allBoxes = agents.get(0).initialState.boxes2; // TODO update allBoxes
														// positions, check if
														// they are updating

		// this is preprocessing shit
		boolean done = false;
		ArrayList<Position> positions = new ArrayList<Position>();
		for (Agent a : agents) {
			for (Box b : allBoxes) {
				for (Goal g : a.initialState.goals2) {
					if (g.color == b.color) {
						done = true;
						positions.add(g.position);
						positions.add(b.position);
						rescueUnit(a, positions, a.initialState);
						break;
					}
				}
				if (done)
					break;
			}
			done = false;
			positions = new ArrayList<Position>();
		}
		//////////////////////////////////////////

		// read the input
		List<Strategy> strategies = null;
		
		if (args.length > 0) {
			strategies = client.getStrategies(args[0]);
		} else {
			strategies = new ArrayList<>(agents.size());
			for (int i = 0; i < agents.size(); i++)
				strategies.add(new StrategyBFS());
			System.err.println(
					"Defaulting to BFS search. Use arguments -bfs, -dfs, -astar, -wastar, or -greedy to set the search strategy.");
		}
		///////////////////////////////// readinput end

		LinkedList<Node> solution;

		// List containing all the solutions for every agent
		List<List<Node>> solutions = new ArrayList<>();

		for (int i = 0; i < agents.size(); i++) {
			try {
				solution = client.Search(strategies.get(i), SearchClient.agents.get(i).initialState);
				solutions.add(solution);
			} catch (Exception ex) {
				System.err.println("Problems for agent " + i + " when solving the level");
				ex.printStackTrace();
				solutions = null;
			}
		}

		if (solutions == null) {
			printSearchStatus(strategies, solutions);
			System.err.println("Unable to solve level.");

			System.exit(0);

		} else {

			int maxSol = 0;
			int m;
			for (int i = 0; i < solutions.size(); i++) {
				m = solutions.get(i).size();
				if (m > maxSol) {
					maxSol = m;
				}
			}
			// DEBUG ALGORITHMS
			//int maxSol =0;
			//List containing all the solutions for every agent
			//List<List<Node>> solutions = new ArrayList<>();
			
			//call planner for all agents, fill solutions, repeat. Pass strategy to planner.
			Planner plan = null;
			//for(Agent a : agents){
			
			plan = new Planner(agents.get(0)); 
			//solutions.add(plan.solution);
			
			//}
			printSearchStatus(strategies, solutions);

			int m1;
			for (int i = 0; i < solutions.size(); i++) {
				m1 = solutions.get(i).size();
				if (m1 > maxSol) {
					maxSol = m1;
				}
			}
			
			//TODO: empty the same string builder object
			for(int i = 0; i < maxSol; i++) {
			
				StringBuilder jointAction = new StringBuilder();

				jointAction.append('[');
				// if (!solutions.isEmpty()) {
				for (int j = 0; j < solutions.size(); j++) {
					Node n = null;
					try {

						n = solutions.get(j).get(i);
						if (!n.doNoOp) {
							jointAction.append(n.action.toString() + ",");
						} else {
							jointAction.append("NoOp,");
						}
					} catch (IndexOutOfBoundsException e) {
						jointAction.append("NoOp,");
					}
				}
				// }
				// } else {
				// jointAction.append("NoOp,"); //TODO and this..check it out
				// }
				// replace the last comma with ']'
				jointAction.setCharAt(jointAction.length() - 1, ']');
				System.out.println(jointAction.toString());
				System.err.println("===== " + jointAction.toString() + " ====");
				String response = serverMessages.readLine();
				if (response.contains("false")) {
					System.err.format("Server responsed with %s to the inapplicable action: %s\n", response,
							jointAction.toString());
					System.err.format("%s was attempted in \n%s\n", jointAction.toString(), "Problems with the moves");
					break;
				}
			}
		}
	}

	private static void printSearchStatus(List<Strategy> strategiesSearchResults, List<List<Node>> solutions) {
		int i = 0;
		for(Strategy s: strategiesSearchResults) {
			System.err.println("[RES] Strategy search result for agent " + i + ". Solution lenght is " + solutions.get(i).size() + ".");
			System.err.println(s.searchStatus() + "\n");
			i++;
		}
	}
}
/*
 * TODO Andrei: update the non square maps with wall on the empty spaces
 * - update the boxes2 and the goals list in the Node class
 * - compute the distance from the closest to the furtheres point (now is [1,1] -> [levelRow-1, levCol-1])
 * - Make the agents (when more of the same color) to pick the closest obj to the goal and then goal is satisfied NoOp
 * - A* heuristics make it to work
 * - more boxes of tjhe same color but just one (or less ) goals
 * - Add distance from angent to the box in the heuristic as well
 * - call the heuristic function with the distances
 * - if starts with space and there is a wall, complete everithinh with walls
 * - the annoying bug when parsing the level and the agents are not in the good position in the rows*/
