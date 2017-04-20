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
