package searchclient;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DistancesComputer {
	
	public char[][] map;
	public static Set<Cell> allDistancesBetweenCells = new HashSet<>();
	public static final int levelRowSize = SearchClient.levelRowSize;
	public static final int levelColSize = SearchClient.levelColumnSize;
	public Set<Position> visitated = new HashSet<>();

	public DistancesComputer(char[][] map) {
		this.map = map;
	}
	
	public void computeDistanceBetweenTwoPoints(Position start, Position end) {
		ArrayDeque<Position> queue = new ArrayDeque<>();
		queue.add(start);
		visitated.add(start);
		
		while(!queue.isEmpty()) {
			Position p = queue.poll();
			List<Position> expandedCells = getExpandedCells(p);
			queue.addAll(expandedCells);
			visitated.add(p);
		}
	}
	
	public List<Position> getExpandedCells(Position cell) {
		List<Position> result = new ArrayList<>();
		int x = cell.row;
		int y = cell.col;
		
		if (x - 1 < 0) {
			x = 1;
		} else if(x + 1 > levelRowSize) {
			x = levelRowSize - 1;
		} else if(y - 1 < 0) {
			y = 1;
		} else if(y + 1 > levelColSize) {
			y = levelColSize - 1;
		} 
		
		for(int i = x - 1; i < x + 1; i++) {
			for(int j = y - 1; j < y + 1; j++) {
				Position p = new Position(x, y);
				//if the neighbour was not visitated and is not a wall
				if(!visitated.contains(p) && map[i][j] != '+') {
					result.add(p);
					
				}
			}
		}
		
		return result;
	}
}
