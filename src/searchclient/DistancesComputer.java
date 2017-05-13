package searchclient;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DistancesComputer {
	
	public static int[][] map;
	public static Set<Cell> allDistancesBetweenCells = new HashSet<>();
	public static final int levelRowSize = SearchClient.levelRowSize-2;
	public static final int levelColSize = SearchClient.levelColumnSize-2;
	public Set<Position> visitated = new HashSet<>();

	public DistancesComputer(int[][] map) {
		DistancesComputer.map = map;
	}
	
	public void computeDistanceBetweenTwoPoints(Position start, Position end) {
		
		ArrayDeque<Position> queue = new ArrayDeque<>();
		queue.add(start);
		visitated.add(start);		
		while(!queue.isEmpty()) {
			
			Position p = queue.remove();
			List<Position> expandedCells = getExpandedCells(p);
				
			for(Position neigh:expandedCells) {
				//if it is not a wall
				if(map[neigh.row][neigh.col] != -1) {
					if(!isInTheCorners(p.row, p.col, neigh.row, neigh.col)){
						map[neigh.row][neigh.col] = map[p.row][p.col] + 1;
						queue.add(neigh);
						visitated.add(neigh);
					}
				}
			}
		}
		//printMap(start, end);
	}
	
	public boolean isInTheCorners(int x, int y, int row, int col) {
		if(x != row && y != col)
			return true;
		else return false;
	}
	
	public void computeAllDistances(Position start, Position end) {
		computeDistanceBetweenTwoPoints(start, end);
		
//		Map<Position, Integer> otherCellsDistance = new HashMap<>();
//		otherCellsDistance.put(new Position(0,0), 10);
//		Cell cell = new Cell(cellPostition, otherCellsDistance);
//		allDistancesBetweenCells.add(cell);
	}
	
	public static int getDistanceBetween2Positions(Position p1, Position p2) {
		//check for the wall
		int dist = Math.abs(map[p2.row][p2.col] - map[p1.row][p1.col]);
//		if(dist != 0)
			return dist;
	//	else return Integer.MAX_VALUE;
	}
	
	public int getDirection(Position p1, Position p2) {
		int dist = map[p2.row][p2.col] - map[p1.row][p1.col];
		if(dist > 0) 
			//I have to move either on the right or down on the map
			return 1;
		//I have to move either on the left or up on the map
		else return 2;
	}
	
	public void printMap(Position start, Position end) {
		System.err.println("\n ------------------------------------");
		System.err.println("###### THE Distance for [" + start.row + ", " + start.col+ "] :");
		
		for(int i1 = 1; i1 <=  levelRowSize; i1++) {
			for (int j = 1; j <= levelColSize; j++) {
				if(map[i1][j] == -1) {
					System.err.print("+"+"  ");
				} else if(i1 == start.row && j == start.col) {
					System.err.print("S" + "  ");
				} else if(i1 == end.row && j == end.col) {
					
					System.err.print("E" + " ");
				} else {
					if(map[i1][j] <= 9)
						System.err.print(map[i1][j] + "  ");
					else System.err.print(map[i1][j] + " ");
				}
			}
			System.err.println("");
		}
		
		System.err.println(" ###### Endo of distance [" + end.row + ", " + end.col+ "] ####");
	}
	
	public List<Position> getExpandedCells(Position cell) {
		List<Position> result = new ArrayList<>();
		
		int x = cell.row;
		int y = cell.col;
		if(map[x][y] == -1)
			return result;

		for(int i = x - 1; i <= x + 1; i++) {
			for(int j = y - 1; j <= y + 1; j++) {
				try {
					Position p = new Position(i, j);
					//if the neighbour was not visitated and is not a wall
					
					if(!visitated.contains(p) && map[i][j] != -1) {
						result.add(p);
			//			System.err.println("\n i = " + i + " j = " + j);
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					// TODO: handle exception
				}
			}
		}
		
		return result;
	}
	
	public List<Position> getShortestPath(Position from, Position to) {
		List<Position> path = new LinkedList<>();
		Position initial = from;
		
		System.err.println("Dist = " + getDistanceBetween2Positions(from, to));
		//if I have to go from left to right
		while(!from.equals(to)) {
			if(getDirection(from, to) == 1) {
				if(getDistanceBetween2Positions(from, to) > 0) {
	//				if(map[from.row][from.col] == -1) {
	//					continue;
	//				}
					Position p = getNeighbourRight(from);
					System.err.println("From = " + from + " -- Neigh = " + p);
					path.add(p);
					from = p;
				} else
					break;
			} else {
				if(getDistanceBetween2Positions(from, to) > 0) {
	//				if(map[from.row][from.col] == -1) {
	//					continue;
	//				}
					Position p = getNeighbourLeft(from);
					System.err.println("From = " + from + " -- Neigh = " + p);
					path.add(p);
					from = p;
				} else
					break;				
			}
		}
		
		
		//path.add(0, initial);
		System.err.println("Shtortst path between " + initial + " to " + to + " is " + path);
		return path;
	}
	
	public Position getNeighbourRight(Position cell) {
		
		int x = cell.row;
		int y = cell.col;
		int finalX = 0;
		int finalY = 0;
		int shortest = Integer.MIN_VALUE;
		
		
		for(int i = x - 1; i <= x + 1; i++) {
			for(int j = y - 1; j <= y + 1; j++) {
				if(!isInTheCorners(x, y, i, j) && map[i][j] != -1) {
					//go to right
					if(shortest < Math.max(map[x][y], map[i][j])) {
						shortest = Math.max(map[x][y], map[i][j]);
						finalX = i;
						finalY = j;
					}
				}
			}
		}
		
		//if(finalX != 0 && finalY != 0)  {
			
			return new Position(finalX, finalY);
		
		//else return null;
	}
	
public Position getNeighbourLeft(Position cell) {
		
		int x = cell.row;
		int y = cell.col;
		int finalX = 0;
		int finalY = 0;
		int shortest = Integer.MAX_VALUE;
		
		
		for(int i = x - 1; i <= x + 1; i++) {
			for(int j = y - 1; j <= y + 1; j++) {
				if(!isInTheCorners(x, y, i, j) && map[i][j] != -1) {
					//go to right
					if(shortest > Math.min(map[x][y], map[i][j])) {
						shortest = Math.min(map[x][y], map[i][j]);
						finalX = i;
						finalY = j;
					}
				}
			}
		}
		
		//if(finalX != 0 && finalY != 0)  {
			
			return new Position(finalX, finalY);
		
		//else return null;
	}
}