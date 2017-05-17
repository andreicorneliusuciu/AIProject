package searchclient;

//TODO !!!!! remove agents without boxes, put them in the side or mvoe them randomly
//-mathias level split thing
//

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.Iterator;

import searchclient.Heuristic.AStar;
import searchclient.Heuristic.Greedy;
import searchclient.Heuristic.WeightedAStar;
import searchclient.Strategy.StrategyBFS;
import searchclient.Strategy.StrategyBestFirst;
import searchclient.Strategy.StrategyDFS;

public class SearchClient {
	// SNAKE WALL
	static List<List<Node>> solutions = new ArrayList<>();
	public static ArrayList<ArrayList<Position>> blockedPositions;

	public static Cell[][] mapOfCell;
	// The list of initial state for every agent
	// public List<Node> initialStates;
	public static boolean[][] walls;

	public static Node uberNode;
	// The size of the map
	// public static int levelColSize;
	// public static int levelRowSize;
	static int[][][] flowFills = new int[10][][];
	// The list of agents. Index represents the agent, the value is the color
	public static List<Agent> agents;

	// List with all the goals.
	public static List<Goal> allGoals;
	// List with all the boxes.
	public static List<Box> allBoxes;

	// Rooms for all agents

	public static int[] agentsRooms = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	// Key = Color, Value = List of Box numbers
	public Map<String, List<Integer>> colorToBoxes = new HashMap<>();

	public static Map<Character, String> boxesToColor = new HashMap<>();

	// color to agent map
	public Map<String, Character> colorToAgent = new HashMap<>();

	public static int replanCounter = 0; // Used for debugging

	// Supernodes. Ask Janus.
	public static ArrayList<SuperNode> superNodes;

	// the map represented as a matrix for computing the shortest distances
	// between all two pair of cells on the map
	public int[][] map;
	public static int levelRowSize;
	public static int levelColumnSize;

	public SearchClient(BufferedReader serverMessages) throws Exception {

		String line = serverMessages.readLine();
		ArrayList<String> lines = new ArrayList<String>();
		agents = new ArrayList<Agent>();
		allGoals = new ArrayList<>();
		int maxCol = 0;

		boolean flagAgents = false;
		boolean flagBoxes = false;
		boxesToColor.put('*', "cellphone");
		boxesToColor.put('-', "cedsllphone");
		int noOfActualRowsForTheLevel = 0;
		while (!line.equals("")) {
			// Read lines specifying colors of the boxes and the agents
			if (Pattern.matches("[a-zA-Z]", line.substring(0, 1))) {
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
						flagAgents = true;
						// Box
					} else if ('A' <= chr && chr <= 'Z') {

						boxesToColor.put(chr, color);
						flagBoxes = true;
					} else {
					}
				}
			}
			// careful when the level is narrow and the declarations are larger
			if (line.length() > maxCol && !Pattern.matches("[a-zA-Z]", line.substring(0, 1))) {
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

		// this.levelRowSize = levelRowSize;// Keeps track of map size
		// this.levelColSize = levelColumnSize;

		// levelRowSize gives the no of rows on the map. levelColumnSize is the
		// no of columns
		levelRowSize = lines.size() - noOfActualRowsForTheLevel;
		levelColumnSize = maxCol;

		// System.err.println("Row = " + levelRowSize + " CCOL = " +
		// levelColumnSize);
		walls = new boolean[levelRowSize][levelColumnSize];
		map = new int[levelRowSize][levelColumnSize];

		// for(int i = 0; i < agents.size(); i++) {
		// initialStates.add(new Node(null, levelRowSize, levelColumnSize));
		// }
		ArrayList<ArrayList<Position>> blockedPositions = new ArrayList<ArrayList<Position>>();
		for (int i = 0; i < agents.size(); i++) {
			agents.get(i).assignInitialState(new Node(null, levelRowSize, levelColumnSize, 0));
			// initialStates.add(new Node(null, levelRowSize, levelColumnSize));

		}

		uberNode = new Node(null, levelRowSize, levelColumnSize, 0);

		int rowA = 0;
		for (String l1 : lines) {
			if (Pattern.matches("[a-zA-Z]", l1.substring(0, 1)) || l1.matches("^\\s*$")) {
				continue;
			}
			for (int col = 0; col < l1.length(); col++) {
				char chr1 = l1.charAt(col);

				if ('0' <= chr1 && chr1 <= '9') { // Agent.
					// if I find an agent with no color I make it blue

					int index = agents.indexOf(new Agent(Integer.parseInt("" + chr1), null));
					if (index == -1) {

						Agent agentT = new Agent(Integer.parseInt("" + chr1), "blue", new Position(rowA, col), new Node(null, levelRowSize, levelColumnSize, 0));
						agentT.initialState.theAgentColor = agentT.color;
						agentT.initialState.theAgentName = agentT.name;
						agentT.initialState.agentCol = agentT.position.col;
						agentT.initialState.agentRow = agentT.position.row;

						agents.add(agentT);

						// agents.add(new Node(null, levelRowSize,
						// levelColumnSize));
					} else {
						// update the position of the agents declared above the
						// map into the input file
						Agent a = agents.get(index);
						a.position.row = rowA;
						a.position.col = col;
						a.initialState.theAgentColor = a.color;
						a.initialState.theAgentName = a.name;
						a.initialState.agentCol = a.position.col;
						a.initialState.agentRow = a.position.row;
						agents.set(index, a);
					}

				}
			}
			rowA++;
		}

		for (String l : lines) {
			if (Pattern.matches("[a-zA-Z]", l.substring(0, 1)) || l.matches("^\\s*$")) {
				continue;
			}
			for (int col = 0; col < l.length(); col++) {
				char chr = l.charAt(col);

				// update the general map => omit the agents and boxes
				if (chr == '+') {
					map[row][col] = -1;
				} else {
					map[row][col] = 0;
				}

				if (chr == '+') { // Wall.
					// this.initialState.walls[row][col] = true;
					walls[row][col] = true;

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

						agents.get(i).initialState.boxes2.add(new Box(chr, boxesToColor.get(chr), new Position(row, col)));

						if (agents.get(i).color.equals(boxesToColor.get(chr))) {
							agents.get(i).initialState.myBoxesFinal.add(new Box(chr, boxesToColor.get(chr), new Position(row, col)));
						}
						// uberNode.boxes2.add(new Box(chr,
						// boxesToColor.get(chr), new Position(row, col)));
						// }
					}
				} else if ('a' <= chr && chr <= 'z') { // Goal.
					// if I find the goal before the corresponding box on the
					// map (I read left to right)
					if (!boxesToColor.containsKey(Character.toUpperCase(chr))) {
						boxesToColor.put(Character.toUpperCase(chr), "blue");
					}

					// System.err.println("Filling allGoals " + chr);

					allGoals.add(new Goal(chr, boxesToColor.get(Character.toUpperCase(chr)), new Position(row, col)));

					// System.err.println("Filling allGoals " + allGoals);

					for (int i = 0; i < agents.size(); i++) {
						// put the goal to the agent map just if they are the
						// same color
						if (agents.get(i).color.equals(boxesToColor.get(Character.toUpperCase(chr)))) {
							agents.get(i).initialState.goals[row][col] = chr;
							// System.err.println("goal made here: " + row + ","
							// + col + " by agent " + agents.get(i));
							agents.get(i).initialState.goals2.add(new Goal(chr, agents.get(i).color, new Position(row, col)));
						}
					}
				} else if (chr == ' ' || ('0' <= chr && chr <= '9')) {

				} else {
					System.err.println("Error, read invalid level character: " + chr);
					System.exit(1);
				}
			}
			row++;
		}

		Collections.sort(agents);

		System.err.println("\n ------------------------------------ \n");

		int[][] mapForAllDistances = new int[levelRowSize][levelColumnSize];
		for (int i1 = 0; i1 < levelRowSize; i1++) {
			for (int j1 = 0; j1 < levelColumnSize; j1++) {
				mapForAllDistances[i1][j1] = map[i1][j1];
			}
		}
		for (int i = 0; i < agents.size(); i++) {
			System.err.println("For agent " + i + ":");
			System.err.println("**** myBoxesFinal = " + agents.get(i).initialState.myBoxesFinal);
		}

		// Compute all the distances on a NxN map. It does not work for non
		// square maps.
		DistancesComputer distancesComputer = new DistancesComputer(mapForAllDistances);
		distancesComputer.computeAllDist();

		// Test distances function
		// System.err.println("Distance between (5,0) and (7,0) = " +
		// DistancesComputer.getDistanceBetween2Positions(new Position(0,0),
		// new Position(7,0)));

	}

	public void findAllRooms() {

		for (Agent a : agents) {
			flowFills[a.name] = flowFill2(a);

			System.err.println("kusse goal " + a.name);
			for (int i = 0; i < Node.MAX_ROW; i++) {
				for (int j = 0; j < Node.MAX_COL; j++) {

					System.err.print(flowFills[a.name][i][j]);
				}
				System.err.println();
			}
		}

		agentsRooms[0] = 1;
		int rooms = 1;

		for (Agent a : agents) {
			boolean flag = false;

			if (a.name == 0) {
				continue;
			}

			for (Agent a2 : agents) {

				if (a.name == 7 && a2.name == 3) {

					System.err.println("a:  " + "agent   " + a.name + "   " + flowFills[a.name][a.position.row][a.position.col]);
					System.err.println("a2:  " + "agent   " + a2.name + "   " + flowFills[a.name][a2.position.row][a2.position.col]);
					System.err.println("hmm  " + agentsRooms[a2.name]);
				}

				if (flowFills[a.name][a.position.row][a.position.col] == flowFills[a.name][a2.position.row][a2.position.col] && !(a.name == a2.name)) {

					if (agentsRooms[a2.name] != 0) {
						flag = true;

						agentsRooms[a.name] = agentsRooms[a2.name];
					} else
						agentsRooms[a.name] = rooms;

					// System.err.println("agent: a "+a.name);
					// System.err.println("agent: a2 "+a2.name);
					break;
				}

			}
			if (!flag) {
				rooms++;
				agentsRooms[a.name] = rooms;
				System.err.println("pizza");
			}
		}
		System.err.println("pisse" + agentsRooms);
	}

	public static void findReachableBoxesAndGoals() {

		allBoxes = new ArrayList<Box>();
		for (Box b : agents.get(0).initialState.boxes2) {
			allBoxes.add(new Box(b));
		}

		// boolean done = false;
		ArrayList<Position> positions = new ArrayList<Position>();

		for (Agent a : agents) {
			Node pizda = a.initialState.Copy();

			for (Box b : allBoxes) {
				for (Goal g : pizda.goals2) {
					if (g.color.equals(b.color)) {
						// done = true;

						rescueUnit(a, positions, true, g, b);
						// break;
					}
				}

				// if (done)
				// break;
			}
			// done = false;

			System.err.println("kusse box " + a.name);
			for (int i = 0; i < Node.MAX_ROW; i++) {
				for (int j = 0; j < Node.MAX_COL; j++) {

					System.err.print(agents.get(a.name).initialState.goals[i][j]);

				}
				System.err.println();
			}

		}

	}

	public static int[][] flowFill2(Agent agent) {
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

		// System.err.println("0");
		return result; // 0 = Unconnected, 1 = Wall, 2 = Free flow, 3 =
		// Blockage.
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
			System.err.println("Defaulting to BFS search. Use arguments -bfs, -dfs, -astar, -wastar, or -greedy to set the search strategy.");
			break;
		}

		return strategies;
	}

	// private static void printSearchStatus(List<Strategy>
	// strategiesSearchResults, List<List<Node>> solutions) {
	// int i = 0;
	// for (Strategy s : strategiesSearchResults) {
	// System.err.println("[RES] Strategy search result for agent " + i + ".
	// Solution lenght is " + solutions.get(i).size() + ".");
	// System.err.println(s.searchStatus() + "\n");
	// i++;
	// }
	// }

	public static int noOpCount = 0;

	public static boolean patternA(Cell c) {
		// Find pattern where goal is surrounded by three walls
		int wallCount = 0;
		int priorityAdd = 0;
		if (!c.east) {
			wallCount++;
		} else if (mapOfCell[c.position.row][c.position.col + 1].prioritySet) {
			if (mapOfCell[c.position.row][c.position.col + 1].addPriority >= priorityAdd) {
				priorityAdd = mapOfCell[c.position.row][c.position.col + 1].addPriority + 1;
			}
			wallCount++;
		}
		if (!c.west) {
			wallCount++;
		} else if (mapOfCell[c.position.row][c.position.col - 1].prioritySet) {
			if (mapOfCell[c.position.row][c.position.col - 1].addPriority >= priorityAdd) {
				priorityAdd = mapOfCell[c.position.row][c.position.col - 1].addPriority + 1;
			}
			wallCount++;
		}
		if (!c.south) {
			wallCount++;
		} else if (mapOfCell[c.position.row + 1][c.position.col].prioritySet) {
			if (mapOfCell[c.position.row + 1][c.position.col].addPriority >= priorityAdd) {
				priorityAdd = mapOfCell[c.position.row + 1][c.position.col].addPriority + 1;
			}
			wallCount++;
		}
		if (!c.north) {
			wallCount++;
		} else if (mapOfCell[c.position.row - 1][c.position.col].prioritySet) {
			if (mapOfCell[c.position.row - 1][c.position.col].addPriority >= priorityAdd) {
				priorityAdd = mapOfCell[c.position.row - 1][c.position.col].addPriority + 1;
			}
			wallCount++;
		}
		if (wallCount > 2 && !c.prioritySet) {
			mapOfCell[c.position.row][c.position.col].prioritySet = true;
			mapOfCell[c.position.row][c.position.col].priority += priorityAdd;
			mapOfCell[c.position.row][c.position.col].addPriority = priorityAdd;
			System.err.println("Found corner! " + priorityAdd);
			return true;
		}
		return false;
	}

	public static void goalPriority() {// TODO
		for (SuperNode sn : superNodes) {
			if (sn.goalSuperNode) {// Setup baseline priority. High is low, low
										// is high.
				for (Line l : sn.memberLines) {
					for (Position p : l.positions) {
						for (Goal g : allGoals) {
							if (sn.absorbed) {
								mapOfCell[p.row][p.col].priority = 0;
							} else {
								mapOfCell[p.row][p.col].priority = 100;
							}
						}
					}
				}
			}
		}
		// If bugs occur, try commenting out everything after this line.
		boolean done = false;
		int totalFound = 0;
		int baseline = 0;
		int state = 0;
		while (!done) {
			int curFound = new Integer(totalFound);
			for (SuperNode sn : superNodes) {
				if (sn.goalSuperNode) {
					for (Line l : sn.memberLines) {
						for (Position p : l.positions) {
							if (state == 0) {
								if (patternA(mapOfCell[p.row][p.col])) {
									curFound++;
								}
							} else if (state == 1) {
								if (!mapOfCell[p.row][p.col].prioritySet) {
									mapOfCell[p.row][p.col].priority += baseline;
									mapOfCell[p.row][p.col].prioritySet = true;
									curFound++;
								}
							}
						}
					}
				}
			}
			done = true;
			for (Goal g : allGoals) {
				if (!g.priorityGiven) {
					done = false;
				}
			}
			System.err.println(curFound + " " + totalFound);
			if (curFound == totalFound) {
				state++;
				baseline = curFound + state;
				totalFound = curFound;
			} else {
				totalFound = curFound;
			}
			if (state > 1) {
				String print = "";
				for (Goal g : allGoals) {
					g.priority = mapOfCell[g.position.row][g.position.col].priority;
					print += "[" + g.priority + "]\n";
				}
				System.err.println(print);
				break;
			}
		}
	}

	public static void main(String[] args) throws Exception { // TODO second
																	// loop,
																// freakout
		BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));

		// Use stderr to print to console
		System.err.println("SearchClient initializing. I am sending this using the error output stream.");

		// Read level and create the initial state of the problem
		SearchClient client = new SearchClient(serverMessages);

		// TODO update allBoxes and allgoals

		// for (Agent a : agents) { // positions, check if
		allBoxes = new ArrayList<Box>();
		for (Box b : agents.get(0).initialState.boxes2) {
			allBoxes.add(new Box(b));
		}

		boolean done = false;
		ArrayList<Position> positions = new ArrayList<Position>();
		for (Agent a : agents) {
			for (Box b : allBoxes) {

				//	if (flowFills[a.name][a.position.row][a.position.col] == flowFills[a.name][b.position.row][b.position.col]) {

				for (Goal g : a.initialState.goals2) {
					if (g.color.equals(b.color)) {
						done = true;
						positions.add(g.position);

						positions.add(new Position(b.position.row, b.position.col));

						rescueUnit(a, positions, false, g, b);
						break;
					}
				}
				if (done)
					break;

				//				} else
				//					continue;
			}

			done = false;
			positions = new ArrayList<Position>();
		}

		//System.err.println("these are my boxes: " + agents.get(1).initialState.myBoxesFinal);
		System.err.println("these are all boxes: " + allBoxes);

		// fill up the central node
		// uberNode.goals2 = allGoals;
		for (Goal g : allGoals) {
			uberNode.goals2.add(g);
		}

		for (Box b : allBoxes) {
			uberNode.boxes2.add(new Box(b));
		}
		// uberNode.boxes2 = allBoxes;

		for (Goal g : uberNode.goals2) {

			uberNode.goals[g.position.row][g.position.col] = g.name;
		}

		for (Box b : uberNode.boxes2) {

			uberNode.boxes[b.position.row][b.position.col] = b.name;
		}

		uberNode.agents = agents;

		// storageAnalysis(uberNode);
		// goalPriority();

		// for(){

		// }

		//////////////////////////////////////////

		//TURN ALL USELESS BOXES TO WALLS
		List<Box> usefulBoxes = new ArrayList<Box>();

		//set useless boxes to walls
		for (Agent a : agents) {
			for (Box b : allBoxes) {

				if (a.color.equals(b.color)) {
					usefulBoxes.add(b);
				}

			}
		}

		List<Box> uselessBoxes = new ArrayList<Box>();

		for (Box b : allBoxes) {
			if (!usefulBoxes.contains(b)) {

				walls[b.position.row][b.position.col] = true;
				uselessBoxes.add(b);
			}
		}

		System.err.println("AllBoxes before : " + allBoxes);

		for (Box b : uselessBoxes) {
			allBoxes.remove(b);
		}

		System.err.println("AllBoxes after : " + allBoxes);

		//GET THE STRATEGY

		List<Strategy> strategies = null;

		if (args.length > 0) {
			strategies = client.getStrategies(args[0]);
		} else {
			strategies = new ArrayList<>(agents.size());
			for (int i = 0; i < agents.size(); i++)
				strategies.add(new StrategyBFS());
			System.err.println("Defaulting to BFS search. Use arguments -bfs, -dfs, -astar, -wastar, or -greedy to set the search strategy.");
		}

		while (true) {

			Planner plan = null;

			solutions = new ArrayList<List<Node>>();

			LinkedList<Node> solution = new LinkedList<Node>();

			blockedPositions = new ArrayList<ArrayList<Position>>();

			List<Agent> usefulAgents = new ArrayList<Agent>();
			List<Agent> uselessAgents = new ArrayList<Agent>();

			//Find agents that have no boxes/goals

			for (Agent a : agents) {
				for (Goal g : allGoals) {

					if (g.color.equals(a.color)) {

						usefulAgents.add(a);
					} 
				}

			}
			
			for(Agent a : agents)
			{
				if(!usefulAgents.contains(a))
				{
					uselessAgents.add(a);
				}
			}

			System.err.println("Useless agents: "+uselessAgents);
			
			Collections.sort(usefulAgents);
			Collections.sort(uselessAgents);
			
			for(Agent a : uselessAgents)
			{
				a.hide = true;
			}

			System.err.println("Agents "+agents);
			
			boolean isGoalState = true;
			for (Goal g : allGoals) {
				if (!g.isSatisfied) {
					isGoalState = false;
					break;

				}
			}

			// TODO it crashes here. Sth wrong with goals. Sth wrong with the
			// initialstate of the agent that did not stop.
			if (isGoalState) {
				System.err.println("Success!!!");
				break;
			}

			ArrayList<Position> poswall = new ArrayList<Position>();
			ArrayList<Position> temppie = new ArrayList<Position>();
			for (Agent atemp : agents) {
				poswall.add(new Position(atemp.position.row, atemp.position.col));
			}
			temppie.add(new Position(0, 0));
			blockedPositions.add(poswall);
			blockedPositions.add(temppie);

			// System.err.println("Initializing agents with initial state: /n" +
			// agents);

			// Node updatedNode = new Node(null, Node.MAX_ROW, Node.MAX_COL);
			int agentIndex = 0;
			// Node copy = new Node(null, Node.MAX_ROW, Node.MAX_COL);
			for (Agent a : agents) {

				//if the agent is not useless
				if (!a.isTrapped) {

					// System.err.println("Initializing planner for " + a.name +
					// "with initial state: /n" + a.initialState);
					a.initialState.blockGoalsMode = true;

					a.initialState.assignBlocked(blockedPositions);
					plan = new Planner(a);//TODO: If plan fails, try to plan without blockGoalsMode
					//if plan is not trapped
					if (!plan.noPlan) {

						if (agents.size() == 1 && allGoals.size() >= 55) {
							strategies.clear();
							strategies.add(new StrategyBFS());
						}
						solution = plan.findSolution(strategies.get(agentIndex));

						solutions.add(solution);
						//if plan is trapped, break out of if statement

					}
				}

				if (a.isTrapped && !a.initialState.isGoalState()) { //
					// if
					// agent
					// trapped

					agents.get(a.name).initialState.assignBlocked(blockedPositions);
					LinkedList<Node> tempList = new LinkedList<Node>();
					Node tempInitialState = a.initialState.Copy();
					// System.err.println("InitialState for trapped agent " +
					// a.name + " with initialState " + a.initialState);
					tempInitialState.doNoOp = true; // append noop in
					// joinedaction
					tempList.add(tempInitialState);
					solutions.add(tempList);

					a.isTrapped = false;
				} else if (a.initialState.isGoalState()) {

					agents.get(a.name).initialState.assignBlocked(blockedPositions);
					Node tnode = a.initialState.Copy();
					tnode.doNoOp = true;
					tnode.parent = null;
					tnode.action = null;
					List<Node> tlist = new ArrayList<Node>();
					tlist.add(tnode);
					solutions.add(tlist);
				} else if (noOpCount > 10) {
					// TODO change the plan
				}

				agentIndex++;

				// TODO: Turn plans into arraylistarraylistposition
				ArrayList<Position> twall = new ArrayList<Position>();
				twall.add(new Position(agents.get(a.name).initialState.agentRow, agents.get(a.name).initialState.agentCol));
				if (solutions.size() == 1) {
					blockedPositions.add(twall);
				} else {
					blockedPositions.get(0).add(twall.get(0));
				}
				int i2 = 1;
				for (Node n : solution) {
					twall = new ArrayList<Position>();
					if (n.action != null) {
						Command temp = n.action;
						int rowPosition = n.agentRow;// +
														// temp.dirToRowChange(temp.dir1);
						int colPosition = n.agentCol;// +
														// temp.dirToColChange(temp.dir1);
						twall.add(new Position(rowPosition, colPosition));
						int rowPosition2 = -1;
						int colPosition2 = -1;
						if (temp.dir2 != null) {
							if (temp.actionType.equals(Command.Type.Push)) {
								rowPosition2 = rowPosition + temp.dirToRowChange(temp.dir2);
								colPosition2 = colPosition + temp.dirToColChange(temp.dir2);
								twall.add(new Position(rowPosition2, colPosition2));
							} else {

								rowPosition2 = rowPosition - temp.dirToRowChange(temp.dir1);
								twall.add(new Position(rowPosition2, colPosition2));
							}
						}
						if (blockedPositions.size() <= i2) {
							blockedPositions.add(twall);
						} else {
							for (Position p : twall) {
								blockedPositions.get(i2).add(p);
							}
						}
						i2++;
					}
				}

			}

			if (solutions.isEmpty())

			{
				// System.err.println(strategy.searchStatus());
				System.err.println("Unable to solve level.");

				System.exit(0);

			} else {
				System.err.println("Found solution of max length " + solutions.size());

				int maxSol = 0;
				int m1;
				for (int i = 0; i < solutions.size(); i++) {
					m1 = solutions.get(i).size();
					if (m1 > maxSol) {
						maxSol = m1;

						// Create row objects

					}
				}

				System.err.println("final boxes: " + agents.get(0).initialState.myBoxesFinal);

				// TODO: empty the same string builder object
				for (int i = 0; i < maxSol; i++) {

					StringBuilder jointAction = new StringBuilder();

					jointAction.append('[');
					// if (!solutions.isEmpty()) {
					// for every agent
					for (int j = 0; j < solutions.size(); j++) {
						Node n = new Node(null, Node.MAX_ROW, Node.MAX_COL, 0);
						try {

							n = solutions.get(j).get(i);

							if (!n.doNoOp) {
								jointAction.append(n.action.toString() + ",");
							} else {

								noOpCount++;

								jointAction.append("NoOp,");
								// agents.get(j).initialState = n;
								// uberNode.updateUberNode(agents);

								// agent here is trapped this round

							}
						} catch (IndexOutOfBoundsException e) {
							jointAction.append("NoOp,");
							Node atempInitialState = solutions.get(j).get(solutions.get(j).size() - 1).Copy();
							atempInitialState.parent = solutions.get(j).get(solutions.get(j).size() - 1);
						}

					}
					// replace the last comma with ']'
					jointAction.setCharAt(jointAction.length() - 1, ']');

					System.out.println(jointAction.toString());
					System.err.println("===== " + jointAction.toString() + " ====");

					String response = serverMessages.readLine();
					while (response.length() < 2) {
						response = serverMessages.readLine();
					}
					System.err.println(response + " " + response.length());
					String simplify = response.substring(1, response.length() - 1);// Cuts
																					// off
																					// '['
																					// ']'.
					String[] commandAnalyze = simplify.split(",");
					int j2 = 0;

					boolean replan = false;
					for (String s : commandAnalyze) {
						if (s.contains("false")) {
							// Don't update ubernode, replan
							replan = true;
						} else if (s.contains("true")) {
							// Update ubernode
							Node n;
							if (solutions.get(j2).size() > i) {
								n = solutions.get(j2).get(i);

							} else {
								n = null;
							}
							if (n == null) {
								j2++;
								continue;// Noop
							}
							Command temp = n.action;

							if (temp != null) {
								System.err.println(new Position(n.agentRow - Command.dirToRowChange(temp.dir1), n.agentCol - Command.dirToColChange(temp.dir1)));

							}
							if (temp == null) {

							} else if (temp.actionType.equals(Command.Type.Move)) {
								// Update one agent
								for (Agent a : agents) {
									if (a.position.equals(new Position(n.agentRow - Command.dirToRowChange(temp.dir1), n.agentCol - +Command.dirToColChange(temp.dir1)))) {
										a.position = new Position(n.agentRow, n.agentCol);
										a.initialState = n;
										break;
									}
								}
							} else if (temp.actionType.equals(Command.Type.Push)) {
								for (Agent a : agents) {
									if (a.position.equals(new Position(n.agentRow - Command.dirToRowChange(temp.dir1), n.agentCol - Command.dirToColChange(temp.dir1)))) {
										a.position = new Position(n.agentRow, n.agentCol);
										for (Box b : allBoxes) {
											if (b.position.equals(new Position(a.position.row, a.position.col))) {
												b.position = new Position(b.position.row + Command.dirToRowChange(temp.dir2), b.position.col + Command.dirToColChange(temp.dir2));
												a.initialState = n;
												break;
											}
										}
										break;
									}
								}
							} else if (temp.actionType.equals(Command.Type.Pull)) {
								for (Agent a : agents) {
									if (a.position.equals(new Position(n.agentRow - Command.dirToRowChange(temp.dir1), n.agentCol - Command.dirToColChange(temp.dir1)))) {
										Position tempPos = new Position(n.agentRow - Command.dirToRowChange(temp.dir1), n.agentCol - Command.dirToColChange(temp.dir1));
										for (Box b : allBoxes) {
											if (b.position.equals(new Position(tempPos.row + Command.dirToRowChange(temp.dir2), tempPos.col + Command.dirToColChange(temp.dir2)))) {
												b.position = new Position(tempPos.row, tempPos.col);
												a.initialState = n;
												break;
											}
										}
										a.position = new Position(n.agentRow, n.agentCol);
										break;
									}
								}
							}
						}
						j2++;
						noOpCount = 0;// TODO: Belongs somewhere else???
					}
					if (replan) {
						break;
					}

				}
				// recalculate agent freedom
				boolean done2 = false;
				ArrayList<Position> positions2 = new ArrayList<Position>();
				for (Agent a : agents) {
					for (Box b : allBoxes) {

						//	if (flowFills[a.name][a.position.row][a.position.col] == flowFills[a.name][b.position.row][b.position.col]) {

						for (Goal g : a.initialState.goals2) {
							if (g.color == b.color) {
								done2 = true;
								positions2.add(g.position);
								positions2.add(new Position(b.position.row, b.position.col));
								rescueUnit(a, positions2, false, g, b);
								break;
							}
						}
						if (done2)
							break;
						//						} else
						//							continue;
					}
					done2 = false;
					positions2 = new ArrayList<Position>();
				}
				for (Goal g : SearchClient.allGoals) {
					g.assigned = false;
				}
				uberNode.updateUberNode(agents);
				System.err.println("agent initialstate after: " + agents.get(0).initialState);

				//findReachableBoxesAndGoals();
				System.err.println("agent initialstate after2: " + agents.get(0).initialState);

				System.err.println("");
				System.err.println("///////////////////////////////////////////Round complete/////////////////////////////////////////////////////////////////");
				System.err.println("FINAL agents: " + agents);
				System.err.println("FINAL uberNode: " + uberNode);
				System.err.println("Replanning initiated with above agents");

				// USEFUL DEBUGGING TOOL! DON'T REMOVE!
				replanCounter++;
				if (replanCounter >= 100) {
					serverMessages.close();
					return;
				}
			}

			// initialize and reset variables

		}
	}

	public static void serverAnalyze(Node n) {

	}

	public static String[] formattedServerResponse(String s) {

		String[] split = s.split(",");

		split[0] = split[0].substring(1);

		split[split.length - 1] = split[split.length - 1].substring(1, split[split.length - 1].length() - 1);

		for (int i = 0; i < split.length; i++) {

			split[i] = split[i].trim();

		}

		return split;
	}

	public static int[][] flowFill(Agent agent, List<Box> allBoxes2) {
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

		for (int i = 0; i < allBoxes2.size(); i++) {
			if (allBoxes2.get(i).name != '\u0000') {
				matrix[allBoxes2.get(i).position.row][allBoxes2.get(i).position.col] = boxesToColor.get(allBoxes2.get(i).name).hashCode();
				// System.err.println(boxesToColor.get(node.boxes[i][i2]).hashCode());
				// System.err.println(agent.color.hashCode());
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

		// System.err.println("0");
		return result; // 0 = Unconnected, 1 = Wall, 2 = Free flow, 3 =
		// Blockage.
	}

	public static void rescueUnit(Agent agent, ArrayList<Position> positions, boolean onlywalls, Goal goal, Box box) {// Analyse
		// all
		// the
		// agents!
		int[][] matrix = null;

		if (onlywalls) {

			matrix = flowFill2(agent);

			// for (int i = 0; i < matrix.length; i++) {
			// for (int j = 0; j < Node.MAX_COL; j++) {
			// System.err.print(matrix[i][j]);
			// }
			// System.err.println();
			// }

			if (matrix[goal.position.row][goal.position.col] != 2) {

				agents.get(agent.name).initialState.goals2.remove(goal);

				agents.get(agent.name).initialState.goals[goal.position.row][goal.position.col] = 0;

			}

			if (matrix[box.position.row][box.position.col] != 2) {
				agents.get(agent.name).initialState.myBoxesFinal.remove(box);
				agents.get(agent.name).initialState.boxes2.remove(box);
				agents.get(agent.name).initialState.boxes[box.position.row][box.position.col] = 0;

			}

		} else if (!onlywalls) {

			matrix = flowFill(agent, allBoxes);

			for (Position p : positions) {
				if (matrix[p.row][p.col] != 2) {// Blockage! Oh no!
					agent.isTrapped = true;
					System.err.println("Agent blocking found and is: " + agent.name);
					for (int i = 0; i < levelRowSize; i++) {
						for (int i2 = 0; i2 < levelColumnSize; i2++) {
							if (matrix[i][i2] == 3) {
								if (matrix[i - 1][i2] == 0 || matrix[i + 1][i2] == 0 || matrix[i][i2 - 1] == 0 || matrix[i][i2 + 1] == 0) {
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
				} else {
					agent.isTrapped = false;
				}
			}
		}
	}

	public static void storageAnalysis(Node node) {// Swap out input with other
														// types
													// of data?
													// Cell[][]
		mapOfCell = new Cell[levelRowSize][levelColumnSize];
		superNodes = new ArrayList<SuperNode>();

		// Initialize cell map
		for (int i = 0; i < levelRowSize; i++) {
			for (int i2 = 0; i2 < levelColumnSize; i2++) {
				mapOfCell[i][i2] = new Cell(i, i2);

				if (walls[i][i2]) {
					mapOfCell[i][i2].type = 0;// Wall
				} else {
					mapOfCell[i][i2].type = 1;// Free
				}
			}
		}
		for (Goal g : allGoals) {
			// map[g.position.row][g.position.col] = new Cell(g.position.row,
			// g.position.col);
			mapOfCell[g.position.row][g.position.col].type = 2; // Goal

		}

		// Link up cells
		for (int i = 0; i < levelRowSize; i++) {
			for (int i2 = 0; i2 < levelColumnSize; i2++) {
				if (mapOfCell[i][i2].type >= 1) {
					if (i + 1 < levelRowSize) {
						if (mapOfCell[i + 1][i2].type >= 1) {// + is south,
																	// right?
							mapOfCell[i][i2].south = true;
						}
					}
					if (i - 1 >= 0) {
						if (mapOfCell[i - 1][i2].type >= 1) {
							mapOfCell[i][i2].north = true;
						}
					}
					if (i2 + 1 < levelColumnSize) {
						if (mapOfCell[i][i2 + 1].type >= 1) {
							mapOfCell[i][i2].east = true;
						}
					}
					if (i2 - 1 >= 0) {
						if (mapOfCell[i][i2 - 1].type >= 1) {
							mapOfCell[i][i2].west = true;

						}
					}
				}
			}
		}

		// Create row objects
		ArrayList<Line> rows = new ArrayList<Line>();
		for (int i = 0; i < levelRowSize; i++) {
			for (int i2 = 0; i2 < levelColumnSize;) {
				if (mapOfCell[i][i2].type == 1) {

					boolean connected = true;
					Line row = new Line();
					ArrayList<Position> positions = new ArrayList<Position>();
					while (connected) {
						if (i2 < levelColumnSize) {
							if (mapOfCell[i][i2].type == 1) {
								mapOfCell[i][i2].rowID = rows.size();
								positions.add(mapOfCell[i][i2].position);

								i2++;
							} else {
								connected = false;
							}
						} else {
							connected = false;

						}
					}
					row.positions = positions;
					rows.add(row);
				}
				if (mapOfCell[i][i2].type == 2) {

					boolean connected = true;
					Line row = new Line();
					row.goalLine = true;
					ArrayList<Position> positions = new ArrayList<Position>();
					while (connected) {
						if (mapOfCell[i][i2].type == 2) {
							mapOfCell[i][i2].rowID = rows.size();
							positions.add(mapOfCell[i][i2].position);

							i2++;
						} else {
							connected = false;
						}
					}
					row.positions = positions;
					rows.add(row);
				}

				if (mapOfCell[i][i2].type == 0) {
					i2++;
				}
			}
		}

		// Create col objects
		ArrayList<Line> cols = new ArrayList<Line>();
		for (int i = 0; i < levelColumnSize; i++) {
			for (int i2 = 0; i2 < levelRowSize;) {
				if (mapOfCell[i2][i].type == 1) {// White
					boolean connected = true;
					Line col = new Line();
					ArrayList<Position> positions = new ArrayList<Position>();
					while (connected) {
						if (mapOfCell[i2][i].type == 1) {
							mapOfCell[i2][i].colID = cols.size();
							positions.add(mapOfCell[i2][i].position);
							i2++;
						} else {
							connected = false;
						}
					}
					col.positions = positions;
					cols.add(col);
				}
				if (mapOfCell[i2][i].type == 2) {// Goal
					boolean connected = true;
					Line col = new Line();
					col.goalLine = true;
					ArrayList<Position> positions = new ArrayList<Position>();
					while (connected) {
						if (mapOfCell[i2][i].type == 2) {
							mapOfCell[i2][i].colID = cols.size();
							positions.add(mapOfCell[i2][i].position);
							i2++;
						} else {
							connected = false;
						}
					}
					col.positions = positions;
					cols.add(col);

				}
				if (mapOfCell[i2][i].type == 0) {
					i2++;
				}
			}
		}
		// Connect them
		for (int i = 1; i < levelRowSize; i++) {
			for (int i2 = 1; i2 < levelColumnSize; i2++) {
				int curRow = mapOfCell[i][i2].rowID;
				int curCol = mapOfCell[i][i2].colID;
				if (mapOfCell[i][i2].east) {
					int nextRow = mapOfCell[i][i2 + 1].rowID;
					int nextCol = mapOfCell[i][i2 + 1].colID;

					if (nextRow != curRow && nextRow >= 0) {
						rows.get(curRow).east.add(nextRow);
					}
					if (nextCol != curCol && nextCol >= 0) {
						cols.get(curCol).east.add(nextCol);
					}
				}
				if (mapOfCell[i][i2].north) {
					int nextRow = mapOfCell[i - 1][i2].rowID;
					int nextCol = mapOfCell[i - 1][i2].colID;

					if (nextRow != curRow && nextRow >= 0) {
						rows.get(curRow).north.add(nextRow);
					}
					if (nextCol != curCol && nextCol >= 0) {
						cols.get(curCol).north.add(nextCol);
					}
				}
				if (mapOfCell[i][i2].west) {
					int nextRow = mapOfCell[i][i2 - 1].rowID;
					int nextCol = mapOfCell[i][i2 - 1].colID;

					if (nextRow != curRow && nextRow >= 0) {
						rows.get(curRow).west.add(nextRow);
					}
					if (nextCol != curCol && nextCol >= 0) {
						cols.get(curCol).west.add(nextCol);
					}
				}
				if (mapOfCell[i][i2].south) {
					int nextRow = mapOfCell[i + 1][i2].rowID;
					int nextCol = mapOfCell[i + 1][i2].colID;

					if (nextRow != curRow && nextRow >= 0) {
						rows.get(curRow).south.add(nextRow);
					}
					if (nextCol != curCol && nextCol >= 0) {
						cols.get(curCol).south.add(nextCol);
					}
				}
			}
		}

		// TEST
		String debug = "";
		for (int i = 0; i < rows.size(); i++) {
			debug += "East: !" + rows.get(i).east.isEmpty() + "\n";
			debug += "West: !" + rows.get(i).west.isEmpty() + "\n";
			debug += "North: !" + rows.get(i).north.isEmpty() + "\n";
			debug += "South: !" + rows.get(i).south.isEmpty() + "\n";
			for (Position p : rows.get(i).positions) {
				debug += p.toString();
			}
			debug += "\n";
		}

		String debug2 = "";
		for (int i = 0; i < levelRowSize; i++) {
			for (int i2 = 0; i2 < levelColumnSize; i2++) {
				debug2 += mapOfCell[i][i2].rowID;
			}
			debug2 += "\n";
		}
		String printout = "";
		for (Line l : rows) {
			printout += l.goalLine + "\n";
		}
		// System.err.println(debug2);

		// Draw virtual line
		/*
		 * ArrayList<Position> deadEnds = new ArrayList<Position>(); boolean
		 * done = false; while(!done){ int curSize = deadEnds.size(); for(Line l
		 * : rows){ int connectionCount = 0; if(l.east.size()>0){
		 * connectionCtoun } } }
		 */

		// Prioritize cells based on dead ends
		boolean done = true;
		while (!done) {
			for (Goal g : allGoals) {
				// If g isn't a dead end...
				// And all neighbors are walls and dead ends except one
				// This is a dead end. Priority should be lower than innermost
				// dead end.
			}
		}
		// Supernode setup
		int superID = 0;
		for (int i = 0; i < rows.size(); i++) {
			if (!rows.get(i).isInSuperNode) {
				// Make new supernode
				boolean localDone = false;
				Set<Line> superLines = new HashSet<Line>();
				rows.get(i).isInSuperNode = true;
				rows.get(i).superNodeID = superID;
				superLines.add(rows.get(i));
				Set<Integer> east = new HashSet<Integer>();
				Set<Integer> west = new HashSet<Integer>();
				Set<Integer> south = new HashSet<Integer>();
				Set<Integer> north = new HashSet<Integer>();
				boolean goal = false;
				while (!localDone) {
					Set<Line> superLinesTemp = new HashSet<Line>();
					int curSize = superLines.size();
					for (Line l : superLines) {
						superLinesTemp.add(l);
					}

					Iterator<Line> it = superLines.iterator();
					while (it.hasNext()) {
						Line l = it.next();
						if (l.goalLine) {
							goal = true;
						}
						for (int id : l.east) {
							east.add(id);
						}
						for (int id : l.west) {
							west.add(id);
						}
						for (int id : l.south) {
							if (rows.get(id).goalLine && l.goalLine || !rows.get(id).goalLine && !l.goalLine) {// If
																													// they're
																												// both
																												// goals
																												// or
																												// not
																												// goals
								superLinesTemp.add(rows.get(id));
								rows.get(id).isInSuperNode = true;
								rows.get(id).superNodeID = superID;
							} else {
								south.add(id);
							}
						}
						for (int id : l.north) {
							if (rows.get(id).goalLine && l.goalLine || !rows.get(id).goalLine && !l.goalLine) {// If
																													// they're
																												// both
																												// goals
																												// or
																												// not
																												// goals
								superLinesTemp.add(rows.get(id));
								rows.get(id).isInSuperNode = true;
								rows.get(id).superNodeID = superID;
							} else {
								north.add(id);
							}
						}
					}
					superLines = superLinesTemp;

					if (superLines.size() == curSize) {
						// System.err.println(superLines.size() + " = " +
						// curSize + " = " + superLinesTemp.size());
						localDone = true;
					}
				}
				// Create the SuperNode
				SuperNode tempSuperNode = new SuperNode();
				tempSuperNode.east = east;
				tempSuperNode.west = west;
				tempSuperNode.south = south;
				tempSuperNode.north = north;
				tempSuperNode.memberLines = superLines;
				tempSuperNode.goalSuperNode = goal;
				superNodes.add(tempSuperNode);
				superID++;
			}
		}
		// Connect the supernodes
		for (SuperNode sn : superNodes) {
			for (Integer id : sn.east) {
				sn.connectedID.add(rows.get(id).superNodeID);
			}
			for (Integer id : sn.west) {
				sn.connectedID.add(rows.get(id).superNodeID);
			}
			for (Integer id : sn.south) {
				sn.connectedID.add(rows.get(id).superNodeID);
			}
			for (Integer id : sn.north) {
				sn.connectedID.add(rows.get(id).superNodeID);
			}
		}
		// If a goal supernode is ONLY connected to ONE superNode, absorb it
		// into that superNode.
		Iterator<SuperNode> it = superNodes.iterator();
		// for(SuperNode sn : superNodes){
		// if(sn.connectedID.size() == 1 && sn.goalSuperNode){
		// for(Integer id : sn.connectedID){
		// superNodes.get(id).internalGoalNodes.add(sn);
		// }
		// }
		// }

		while (it.hasNext()) {
			SuperNode sn = it.next();
			if (sn.connectedID.size() == 1 && sn.goalSuperNode) {
				for (Integer id : sn.connectedID) {
					superNodes.get(id).internalGoalNodes.add(sn);
					sn.absorbed = true;
					// System.err.println("It gets here! WOO!");
				}
			}
			// System.err.println(sn.connectedID.size());
		}

		// Those who have absorbed false have LOWER priority than those who have
		// absorbed true.
	}

	public void findNarrowCorridorsForStorage() {

		for (int row = 0; row < walls.length; row++) {
			for (int col = 0; col < walls.length; col++) {

			}
		}

	}

}
