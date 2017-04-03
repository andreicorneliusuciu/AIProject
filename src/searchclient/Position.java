package searchclient;

public class Position {
	
	public int row;
	public int col;
	
	public Position(int x, int y) {
		this.row = x;
		this.col = y;
	}
	
	@Override
	public String toString() {
		return "(" + row + ", " + col + ")";
	}
}
