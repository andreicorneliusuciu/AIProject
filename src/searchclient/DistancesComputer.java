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
	public static Map<Cell, Cell> allDistancesBetweenCells = new HashMap<>();
	public static final int levelRowSize = SearchClient.levelRowSize;
	public static final int levelColSize = SearchClient.levelColumnSize;
	public Set<Position> visitated = new HashSet<>();

	public DistancesComputer(int[][] map) {
		DistancesComputer.map = map;
	}
	
	public void computeDistanceBetweenTwoPoints(Position start) {
		
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
	
	public void computeAllDistancesBetweenOneCellAndTheOthers(Position start) {
		
		//
		if(map[start.row][start.col] != -1) {
			//this one computes the distance between start and all the other cells on the map
			computeDistanceBetweenTwoPoints(start);
		} else return;
		
		Map<Position, Integer> otherCellsDistance = new HashMap<>();
		
		for(int i = 1; i <=  levelRowSize-2; i++) {
			for (int j = 1; j <= levelColSize-2; j++) {
				if(map[i][j] != -1) {
							
					otherCellsDistance.put(new Position(i,j), new Integer(map[i][j]));
				}
			}
		}
		
		Position cellPostition = new Position(start.row, start.col);
		Cell cell = new Cell(cellPostition, otherCellsDistance);
		allDistancesBetweenCells.put(cell, cell);
		//printMap(cellPostition);
		for(int i = 1; i <=  levelRowSize-2; i++) {
			for (int j = 1; j <= levelColSize-2; j++) {
				if(map[i][j] != -1)
					map[i][j] = 0;
			}
		}
		visitated.clear();
		//reset the map
	}
	
	public void computeAllDist() {
		for(int i = 1; i <=  levelRowSize-2; i++) {
			for (int j = 1; j <= levelColSize-2; j++) {
				computeAllDistancesBetweenOneCellAndTheOthers(new Position(i,j));
			}
		}
		
//		System.err.println("NNNNNNNNNNNNNN " + allDistancesBetweenCells.size());
		
//		for(Cell c:allDistancesBetweenCells) {
//			
//			for(int i = 1; i <=  levelRowSize-2; i++) {
//				for (int j = 1; j <= levelColSize-2; j++) {
//					try {
//						System.err.println("Dist from (" + c.position.row +", " + c.position.col+") to (" + i + ", " +j + ") is " + c.distancesToAllOtherCells.get(new Position(i,j))) ;
//					}catch (Exception e) {
//						//e.printStackTrace();
//					}
//				}
//			}
//		}
	}
	
	public static int getDistanceBetween2Positions(Position p1, Position p2) {
		//check for the wall
//		System.err.println("CELLLLLLL NULLL");
		Cell c = allDistancesBetweenCells.get(new Cell(p1));
		if(c == null) {
			//If I dont find the distance I return a big value so that the it won't have priority in the state space queue
			return Integer.MAX_VALUE;
		}
		if(c.distancesToAllOtherCells.containsKey(p2)) {
			
			//return the distance if I find it
			//System.err.println("The dist between " + p1 + " and " + p2 + " is " + c.distancesToAllOtherCells.get(p2));
			return c.distancesToAllOtherCells.get(p2);
			
		}  
		
		//System.err.println("CELLLLLLL NULLL");
		return Integer.MAX_VALUE;
		
	}
	
	public int getDirection(Position p1, Position p2) {
		int dist = map[p2.row][p2.col] - map[p1.row][p1.col];
		if(dist > 0) 
			//I have to move either on the right or down on the map
			return 1;
		//I have to move either on the left or up on the map
		else return 2;
	}
	
	public void printMap(Position start) {
		System.err.println("\n ------------------------------------");
		System.err.println("###### THE Distance for [" + start.row + ", " + start.col+ "] :");
		
		for(int i1 = 1; i1 <=  levelRowSize - 2; i1++) {
			for (int j = 1; j <= levelColSize - 2; j++) {
				if(map[i1][j] == -1) {
					System.err.print("+"+"  ");
				} else if(i1 == start.row && j == start.col) {
					System.err.print("S" + "  ");
				
				} else {
					if(map[i1][j] <= 9)
						System.err.print(map[i1][j] + "  ");
					else System.err.print(map[i1][j] + " ");
				}
			}
			System.err.println("");
		}
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