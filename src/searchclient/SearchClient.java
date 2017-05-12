package searchclient;

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

	public static Node uberNode;
	// The size of the map
	// public static int levelColSize;
	// public static int levelRowSize;

	// The list of agents. Index represents the agent, the value is the color
	public static List<Agent> agents;

	// List with all the goals.
	public static List<Goal> allGoals;
	// List with all the boxes.
	public static List<Box> allBoxes;

	public static ArrayList<SuperNode> superNodes;

	// Key = Color, Value = List of Box numbers
	public Map<String, List<Integer>> colorToBoxes = new HashMap<>();

	public static Map<Character, String> boxesToColor = new HashMap<>();

	// color to agent map
	public Map<String, Character> colorToAgent = new HashMap<>();

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

						// Box
					} else if ('A' <= chr && chr <= 'Z') {

						boxesToColor.put(chr, color);
					} else {
					}
				}
			}
			// careful when the level is narrow and the declarations are larger
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
		for (int i = 0; i < agents.size(); i++) {
			agents.get(i).assignInitialState(new Node(null, levelRowSize, levelColumnSize));
			// initialStates.add(new Node(null, levelRowSize, levelColumnSize));
		}
		uberNode = new Node(null, levelRowSize, levelColumnSize);

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
				} else if ('0' <= chr && chr <= '9') { // Agent.
					// if I find an agent with no color I make it blue

					int index = agents.indexOf(new Agent(Integer.parseInt("" + chr), null));
					if (index == -1) {
						Agent agentT = new Agent(Integer.parseInt("" + chr), "blue", new Position(row, col), new Node(null, levelRowSize, levelColumnSize));
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
						agents.get(i).initialState.boxes2.add(new Box(chr, boxesToColor.get(chr), new Position(row, col)));
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
				} else if (chr == ' ') {

				} else {
					System.err.println("Error, read invalid level character: " + chr);
					System.exit(1);
				}
			}
			row++;
		}

		// System.err.println(" + Agents: " + agents);
		Collections.sort(agents);

		System.err.println(" + Agents : " + agents);
		System.err.println(" + Boxes : " + boxesToColor);
		// TODO Andrei: sort the allGoals list alphabetically
		System.err.println(" + Goals: " + allGoals);
		System.err.println("\n ------------------------------------ \n");

		// for (int i = 0 ; i < agents.size(); i++) {
		//
		// agents.get(i).initialState = n;
		// System.err.println("\n $ Goals: " + n.goals2 + " Boxes: " +n.boxes2);
		// }

		int[][] mapWithoutBorders = new int[levelRowSize - 2][levelColumnSize - 2];
		for (int i1 = 1; i1 < levelRowSize - 1; i1++) {
			for (int j1 = 1; j1 < levelColumnSize - 1; j1++) {
				mapWithoutBorders[i1 - 1][j1 - 1] = map[i1][j1];
			}
		}

		// System.err.println("\n ------------------------------------");
		// System.err.println("^^^^^^^^ THE MAP Without Borders: ^^^^^^^");
		//
		// for (int i1 = 0; i1 < levelRowSize - 2; i1++) {
		// for (int j = 0; j < levelColumnSize - 2; j++) {
		// System.err.print(mapWithoutBorders[i1][j]);
		// }
		// System.err.println("");
		// }

		// System.err.println(" ^^^^^^^^ THE MAP END ^^^^^^^");

		// Compute all the distances on a NxN map. It does not work for non
		// square maps.
		DistancesComputer distancesComputer = new DistancesComputer(mapWithoutBorders);
		distancesComputer.computeDistanceBetweenTwoPoints(new Position(0, 0), new Position(levelRowSize - 3, levelColumnSize - 3));

		// Test distances function
		// System.err.println("Distance between (5,0) and (7,0) = " +
		// DistancesComputer.getDistanceBetween2Positions(new Position(0,0),
		// new Position(7,0)));
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

//	private static void printSearchStatus(List<Strategy> strategiesSearchResults, List<List<Node>> solutions) {
//		int i = 0;
//		for (Strategy s : strategiesSearchResults) {
//			System.err.println("[RES] Strategy search result for agent " + i + ". Solution lenght is " + solutions.get(i).size() + ".");
//			System.err.println(s.searchStatus() + "\n");
//			i++;
//		}
//	}

	public static int noOpCount = 0;

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
		allBoxes = agents.get(0).initialState.boxes2;

		boolean done = false;
		ArrayList<Position> positions = new ArrayList<Position>();
		for (Agent a : agents) {
			for (Box b : allBoxes) {
				for (Goal g : a.initialState.goals2) {
					if (g.color == b.color) {
						done = true;
						positions.add(g.position);
						positions.add(b.position);
						rescueUnit(a, positions);
						break;
					}
				}
				if (done)
					break;
			}
			done = false;
			positions = new ArrayList<Position>();
		}

		// fill up the central node
		// uberNode.goals2 = allGoals;
		for (Goal g : allGoals) {
			uberNode.goals2.add(g);
		}

		for (Box b : allBoxes) {
			uberNode.boxes2.add(b);
		}
		// uberNode.boxes2 = allBoxes;

		for (Goal g : uberNode.goals2) {

			uberNode.goals[g.position.row][g.position.col] = g.name;
		}

		for (Box b : uberNode.boxes2) {

			uberNode.boxes[b.position.row][b.position.col] = b.name;
		}

		//System.err.println("The node uber alles: " + uberNode);

		uberNode.agents = agents;

		//////////////////////////////////////////

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

			List<List<Node>> solutions = new ArrayList<List<Node>>();
			LinkedList<Node> solution = new LinkedList<Node>();

			System.err.println("The goals: " + allGoals);

			//update boxes status in all agents

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

			System.err.println("Initializing agents with initial state: /n" + agents);

			//	Node updatedNode = new Node(null, Node.MAX_ROW, Node.MAX_COL);
			int agentIndex = 0;
			//	Node copy = new Node(null, Node.MAX_ROW, Node.MAX_COL);
			for (Agent a : agents) {

				if (!a.isTrapped && !a.initialState.isGoalState()) {

					// System.err.println("Initializing planner for " + a.name +
					// "with initial state: /n" + a.initialState);

					plan = new Planner(agents.get(a.name));
					//if plan is not trapped
					solution = plan.findSolution(strategies.get(agentIndex));
					solutions.add(solution);
					//if plan is trapped, break out of if statement
				}
				if (a.isTrapped && !a.initialState.isGoalState()) { //
					// if
					// agent
					// trapped

					LinkedList<Node> tempList = new LinkedList<Node>();
					Node tempInitialState = a.initialState.Copy();
					// System.err.println("InitialState for trapped agent " +
					// a.name + " with initialState " + a.initialState);
					tempInitialState.doNoOp = true; // append noop in
					// joinedaction
					tempList.add(tempInitialState);
					solutions.add(tempList);

					a.isTrapped = false;
				}
								 else if (a.initialState.isGoalState()) {
				
									Node tnode = a.initialState.Copy();
									tnode.doNoOp = true;
									tnode.parent = null;
									tnode.action = null;
									List<Node> tlist = new ArrayList<Node>();
									tlist.add(tnode);
									solutions.add(tlist);
								}
				else if (noOpCount > 10) {
					//TODO change the plan
				}

				agentIndex++;
			}

			if (solutions.isEmpty()) {
				//System.err.println(strategy.searchStatus());
				System.err.println("Unable to solve level.");

				System.exit(0);

			} else {
				System.err.println("Found solution of max length " + solutions.size());

			//	printSearchStatus(strategies, solutions);

				int maxSol = 0;
				int m1;
				for (int i = 0; i < solutions.size(); i++) {
					m1 = solutions.get(i).size();
					if (m1 > maxSol) {
						maxSol = m1;
					}
				}

				// TODO: empty the same string builder object
				for (int i = 0; i < maxSol; i++) {

					StringBuilder jointAction = new StringBuilder();

					jointAction.append('[');
					// if (!solutions.isEmpty()) {
					// for every agent
					for (int j = 0; j < solutions.size(); j++) {
						Node n = new Node(null, Node.MAX_ROW, Node.MAX_COL);
						try {

							n = solutions.get(j).get(i);

							if (!n.doNoOp) {
								jointAction.append(n.action.toString() + ",");

								// simply state the last state as the agents
								// initialState

								agents.get(j).initialState = n;
								//	uberNode.updateUberNode(agents);

								//agent here is not trapped this round

							} else {

								noOpCount++;

								jointAction.append("NoOp,");
								agents.get(j).initialState = n;
								//uberNode.updateUberNode(agents);

								//agent here is trapped this  round

							}
						} catch (IndexOutOfBoundsException e) {
							jointAction.append("NoOp,");
							//make a dummy initialstate for consistency
							
							//System.err.println("memeow size for agent: "+agents.get(j).name+" : "+solutions.get(j).size());
							Node atempInitialState = solutions.get(j).get(solutions.get(j).size()-1).Copy();
							atempInitialState.parent = solutions.get(j).get(solutions.get(j).size()-1);
							//atempInitialState.doNoOp = true; // append noop in
							//System.err.println("Agent :" +agents.get(j).name+" got initialState due to noop exception :"+atempInitialState);
							agents.get(j).initialState=atempInitialState;
							//solutions.get(j).add(atempInitialState);
							
						}

					}
					// replace the last comma with ']'
					jointAction.setCharAt(jointAction.length() - 1, ']');

					System.out.println(jointAction.toString());
					System.err.println("===== " + jointAction.toString() + " ====");
					System.err.println("ZeAgents now are: /n"+agents);

					String response = serverMessages.readLine();
					if (response.contains("false")) {

						// reset initialstates to previous node before conflict
						for (Agent a : agents) {
							if (a.initialState.parent != null) {
								a.initialState = a.initialState.parent;
							} else {

								System.err.println("InitialState parent was null for agent " + a);

								// keep same? do something maybe?
							}
						}

						// update everyones initialStates to the relevant for
						// them current state

						// TODO: replan! simple conflict resoltuion. To be used
						// if snake fails

						System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, jointAction.toString());
						System.err.format("%s was attempted in \n%s\n", jointAction.toString(), "Snake Failed, Hard Replanning initiated!!!");

						noOpCount++;
						//uberNode.updateUberNode(agents);

						break;
					} else {

						noOpCount = 0;
					}
				}
				uberNode.updateUberNode(agents);

				allBoxes = agents.get(0).initialState.boxes2;

				//recalculate agent freedom
				boolean done2 = false;
				ArrayList<Position> positions2 = new ArrayList<Position>();
				for (Agent a : agents) {
					for (Box b : allBoxes) {
						for (Goal g : a.initialState.goals2) {
							if (g.color == b.color) {
								done2 = true;
								positions2.add(g.position);
								positions2.add(b.position);
								rescueUnit(a, positions2);
								break;
							}
						}
						if (done2)
							break;
					}
					done2 = false;
					positions2 = new ArrayList<Position>();
				}

				System.err.println("///////////////////////////////////////////Round complete/////////////////////////////////////////////////////////////////");
				System.err.println("FINAL agents: " + agents);
				System.err.println("FINAL uberNode: " + uberNode);
				System.err.println("Replanning initiated with above agents");
				//								System.err.println("Resetting trapped boxes");
				//								for (Box b : allBoxes) {
				//									b.isBlocking = false;
				//								}

			}

			// initialize and reset variables

		}
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

	public static void rescueUnit(Agent agent, ArrayList<Position> positions) {// Analyse
		// all
		// the
		// agents!
		int[][] matrix = flowFill(agent, allBoxes);
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

	public static void storageAnalysis(Node node) {// Swap out input with other types
		// of data?
		Cell[][] map = new Cell[levelRowSize][levelColumnSize];

		// Initialize cell map
		for (int i = 0; i < levelRowSize; i++) {
			for (int i2 = 0; i2 < levelColumnSize; i2++) {
				map[i][i2] = new Cell(i, i2);
				if (walls[i][i2]) {
					map[i][i2].type = 0;// Wall
				} else {
					map[i][i2].type = 1;// Free
				}
			}
		}
		for (Goal g : allGoals) {
			//map[g.position.row][g.position.col] = new Cell(g.position.row, g.position.col);
			map[g.position.row][g.position.col].type = 2; // Goal
		}

		// Link up cells
		for (int i = 0; i < levelRowSize; i++) {
			for (int i2 = 0; i2 < levelColumnSize; i2++) {
				if (map[i][i2].type >= 1) {
					if (i + 1 < levelRowSize) {
						if (map[i + 1][i2].type >= 1) {// + is south, right?
							map[i][i2].south = true;
						}
					}
					if (i - 1 >= 0) {
						if (map[i - 1][i2].type >= 1) {
							map[i][i2].north = true;
						}
					}
					if (i2 + 1 < levelColumnSize) {
						if (map[i][i2 + 1].type >= 1) {
							map[i][i2].east = true;
						}
					}
					if (i2 - 1 >= 0) {
						if (map[i][i2 - 1].type >= 1) {
							map[i][i2].west = true;
						}
					}
				}
			}
		}

		// Create row objects
		ArrayList<Line> rows = new ArrayList<Line>();
		for (int i = 0; i < levelRowSize; i++) {
			for (int i2 = 0; i2 < levelColumnSize;) {
				if (map[i][i2].type == 1) {
					boolean connected = true;
					Line row = new Line();
					ArrayList<Position> positions = new ArrayList<Position>();
					while (connected) {
						if (i2 < levelColumnSize) {
							if (map[i][i2].type == 1) {
								map[i][i2].rowID = rows.size();
								positions.add(map[i][i2].position);
								i2++;
							} else {
								connected = false;
							}
						}
					}
					row.positions = positions;
					rows.add(row);
				}
				if (map[i][i2].type == 2) {
					boolean connected = true;
					Line row = new Line();
					row.goalLine = true;
					ArrayList<Position> positions = new ArrayList<Position>();
					while (connected) {
						if (map[i][i2].type == 2) {
							map[i][i2].rowID = rows.size();
							positions.add(map[i][i2].position);
							i2++;
						} else {
							connected = false;
						}
					}
					row.positions = positions;
					rows.add(row);
				}
				if (map[i][i2].type == 0) {
					i2++;
				}
			}
		}

		// Create col objects
		ArrayList<Line> cols = new ArrayList<Line>();
		for (int i = 0; i < levelColumnSize; i++) {
			for (int i2 = 0; i2 < levelRowSize;) {
				if (map[i2][i].type == 1) {//White
					boolean connected = true;
					Line col = new Line();
					ArrayList<Position> positions = new ArrayList<Position>();
					while (connected) {
						if (map[i2][i].type == 1) {
							map[i2][i].colID = cols.size();
							positions.add(map[i2][i].position);
							i2++;
						} else {
							connected = false;
						}
					}
					col.positions = positions;
					cols.add(col);
				}
				if (map[i2][i].type == 2) {//Goal
					boolean connected = true;
					Line col = new Line();
					col.goalLine = true;
					ArrayList<Position> positions = new ArrayList<Position>();
					while (connected) {
						if (map[i2][i].type == 2) {
							map[i2][i].colID = cols.size();
							positions.add(map[i2][i].position);
							i2++;
						} else {
							connected = false;
						}
					}
					col.positions = positions;
					cols.add(col);
				}
				if (map[i2][i].type == 0) {
					i2++;
				}
			}
		}
		// Connect them
		for (int i = 1; i < levelRowSize; i++) {
			for (int i2 = 1; i2 < levelColumnSize; i2++) {
				int curRow = map[i][i2].rowID;
				int curCol = map[i][i2].colID;
				if (map[i][i2].east) {
					int nextRow = map[i][i2 + 1].rowID;
					int nextCol = map[i][i2 + 1].colID;

					if (nextRow != curRow && nextRow >= 0) {
						rows.get(curRow).east.add(nextRow);
					}
					if (nextCol != curCol && nextCol >= 0) {
						cols.get(curCol).east.add(nextCol);
					}
				}
				if (map[i][i2].north) {
					int nextRow = map[i - 1][i2].rowID;
					int nextCol = map[i - 1][i2].colID;

					if (nextRow != curRow && nextRow >= 0) {
						rows.get(curRow).north.add(nextRow);
					}
					if (nextCol != curCol && nextCol >= 0) {
						cols.get(curCol).north.add(nextCol);
					}
				}
				if (map[i][i2].west) {
					int nextRow = map[i][i2 - 1].rowID;
					int nextCol = map[i][i2 - 1].colID;

					if (nextRow != curRow && nextRow >= 0) {
						rows.get(curRow).west.add(nextRow);
					}
					if (nextCol != curCol && nextCol >= 0) {
						cols.get(curCol).west.add(nextCol);
					}
				}
				if (map[i][i2].south) {
					int nextRow = map[i + 1][i2].rowID;
					int nextCol = map[i + 1][i2].colID;

					if (nextRow != curRow && nextRow >= 0) {
						rows.get(curRow).south.add(nextRow);
					}
					if (nextCol != curCol && nextCol >= 0) {
						cols.get(curCol).south.add(nextCol);
					}
				}
			}
		}

		//TEST
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
				debug2 += map[i][i2].rowID;
			}
			debug2 += "\n";
		}
		String printout = "";
		for (Line l : rows) {
			printout += l.goalLine + "\n";
		}
		//System.err.println(debug);

		//Draw virtual line
		/*
		 * ArrayList<Position> deadEnds = new ArrayList<Position>(); boolean
		 * done = false; while(!done){ int curSize = deadEnds.size(); for(Line l
		 * : rows){ int connectionCount = 0; if(l.east.size()>0){
		 * connectionCtoun } } }
		 */

		//Prioritize cells based on dead ends
		boolean done = true;
		while (!done) {
			for (Goal g : allGoals) {
				//If g isn't a dead end...
				//And all neighbors are walls and dead ends except one
				//This is a dead end. Priority should be lower than innermost dead end.
			}
		}
		// Supernode setup
		int superID = 0;
		for (int i = 0; i < rows.size(); i++) {
			if (!rows.get(i).isInSuperNode) {
				//Make new supernode
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
							if (rows.get(id).goalLine && l.goalLine || !rows.get(id).goalLine && !l.goalLine) {//If they're both goals or not goals
								superLinesTemp.add(rows.get(id));
								rows.get(id).isInSuperNode = true;
								rows.get(id).superNodeID = superID;
							} else {
								south.add(id);
							}
						}
						for (int id : l.north) {
							if (rows.get(id).goalLine && l.goalLine || !rows.get(id).goalLine && !l.goalLine) {//If they're both goals or not goals
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
						//System.err.println(superLines.size() + " = " + curSize + " = " + superLinesTemp.size());
						localDone = true;
					}
				}
				//Create the SuperNode
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
		//Connect the supernodes
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
		//If a goal supernode is ONLY connected to ONE superNode, absorb it into that superNode.
		Iterator<SuperNode> it = superNodes.iterator();
		//		for(SuperNode sn : superNodes){
		//			if(sn.connectedID.size() == 1 && sn.goalSuperNode){
		//				for(Integer id : sn.connectedID){
		//					superNodes.get(id).internalGoalNodes.add(sn);
		//				}
		//			}
		//		}

		while (it.hasNext()) {
			SuperNode sn = it.next();
			if (sn.connectedID.size() == 1 && sn.goalSuperNode) {
				for (Integer id : sn.connectedID) {
					superNodes.get(id).internalGoalNodes.add(sn);
					sn.absorbed = true;
					//System.err.println("It gets here! WOO!");
				}
			}
			//System.err.println(sn.connectedID.size());
		}

		//Those who have absorbed false have LOWER priority than those who have absorbed true.
	}

}
/*
 * TODO Andrei: update the non square maps with wall on the empty spaces -
 * update the boxes2 and the goals list in the Node class - call the heuristic
 * function with the distances - if starts with space and there is a wall,
 * complete everithinh with walls - the annoying bug when parsing the level and
 * the agents are not in the good position in the rows
 */

// conflict resolution code, might need it.
// String f = "false"; this needs debugging because
// false/true have different lengths and it fails for
// [true,false]
// int spot = response.indexOf(f);
// int placeInIndex =0;
// if(spot<2){
// placeInIndex =0;
// }
// else if(spot>2 && spot < 10)
// {
// placeInIndex = 1;
//
// }
//
// int increase = 0;
// if (jointAction.charAt(spot) == 'M') {
// increase = 2;
// } else {
// increase = 4;
// }

// jointAction.replace(spot, spot + f.length() +
// increase, "NoOp");
// System.out.println(jointAction);
// System.err.println("jointAction after false:
// "+jointAction);

// in try code, maybe usefull
// check if conflict on same node with the other
// agents
// for (Agent agent : agents) {
// if (agent.name != j) {
// System.err.println("the two possible conflciting
// nodes:/n/n" + solutions.get(j).get(i) +
// solutions.get(agent.name).get(i) + "/n");
// if (!n.doNoOp &&
// !solutions.get(agent.name).get(i).doNoOp) {
// if
// (n.isConflict(solutions.get(agent.name).get(i)))
// {
// // conflict
// System.err.println("i am in" + agents);
//
// agents.get(j).initialState =
// solutions.get(j).get(i - 1);
// agents.get(j).initialState.parent = null;
// System.err.println("i am shrek " + agents);
//
// for(int o = i; o<= maxSol; o++)
// {
// Node tnode = solutions.get(j).get(i-1);
// tnode.doNoOp=true;
// tnode.parent = null;
// solutions.get(j).set(o,tnode);
// }
// //n.doNoOp = true;
// //make everything for one of the two agents NoOp
// break;
// }
//
// else {
// //do sth? maybe?
// }
// }
// }
// }
