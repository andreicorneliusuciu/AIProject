package searchclient;

public class Position {
	
	public int row;
	public int col;
	
	public Position(int row, int col) {
		this.row = row;
		this.col = col;
	}

	public Position() {
	}
	
	public static float manhattanDistance(Position a, Position b)
	{
		float distance = 0;
		
		distance = Math.abs(a.row-b.row) + Math.abs(a.col - b.col);
		if(distance == 0) System.err.println("Problem with distance: 0");
		return distance;
	}

	@Override
	public String toString() {
		return "(" + row + ", " + col + ")";
	}
	
	@Override
	public boolean equals(Object obj){
		Position p = (Position)obj;
		if(p.row == this.row && p.col == this.col){
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
	    int result = this.row;
	    result = 31 * result + this.col;
	    return result;
	}
}
