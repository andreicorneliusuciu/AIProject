package searchclient;

import java.util.HashMap;
import java.util.Map;

public class Cell {
	
	//the position of the cell
	
	public Position position;
	public int type; //0 = wall, 1 = Free, 2 = goal. To be swapped out with enum?
	int rowID = -1;
	int colID = -1;
	int priority = 0;
	int addPriority = 0;
	boolean prioritySet = false;
	boolean north = false;
	boolean east = false;
	boolean west = false;
	boolean south = false;
	public Map<Position, Integer> distancesToAllOtherCells;
	
	/*Contains all the other cells and the distance to the current cell(the field above).
	 *  For instance if you want the distance from (0, 0) to (5, 10):
	 *  cellPosition is set to (0, 0) and you search in the map for Position(5, 10)
	 */
	

	public Cell(Position cellPostition, Map<Position, Integer> distancesToAllOtherCells) {
		
		this.position = cellPostition;
		this.distancesToAllOtherCells = distancesToAllOtherCells;
	}
	
	public Cell(int row, int col){
  		this.position = new Position(row,col);
	}
	
	public Cell(Position p){
  		this.position = p;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		//result = prime * result + ((otherCellsDistance == null) ? 0 : otherCellsDistance.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cell other = (Cell) obj;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}
}
