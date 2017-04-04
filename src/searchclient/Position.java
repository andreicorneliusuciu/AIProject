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
}
