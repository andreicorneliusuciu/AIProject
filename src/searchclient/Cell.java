package searchclient;

// <<<<<<< PlanningImplementation
// public class Cell {
// 	Position position;
// 	int type; //0 = wall, 1 = Free, 2 = goal. To be swapped out with enum?
// 	boolean north = false;
// 	boolean east = false;
// 	boolean west = false;
// 	boolean south = false;
	
// 	public Cell(int row, int col){
// 		this.position = new Position(row,col);
// 		//More data to be entered later
// =======
import java.util.Map;

public class Cell {
	
	//the position of the cell
	public Position cellPostition;
	
	/*Contains all the other cells and the distance to the current cell(the field above).
	 *  For instance if you want the distance from (0, 0) to (5, 10):
	 *  cellPosition is set to (0, 0) and you search in the map for Position(5, 10)
	 */
	public Map<Position, Integer> otherCellsDistance;

	public Cell(Position cellPostition, Map<Position, Integer> otherCellsDistance) {
		
		this.cellPostition = cellPostition;
		this.otherCellsDistance = otherCellsDistance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cellPostition == null) ? 0 : cellPostition.hashCode());
		result = prime * result + ((otherCellsDistance == null) ? 0 : otherCellsDistance.hashCode());
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
		if (cellPostition == null) {
			if (other.cellPostition != null)
				return false;
		} else if (!cellPostition.equals(other.cellPostition))
			return false;
		if (otherCellsDistance == null) {
			if (other.otherCellsDistance != null)
				return false;
		} else if (!otherCellsDistance.equals(other.otherCellsDistance))
			return false;
		return true;
	}
}
