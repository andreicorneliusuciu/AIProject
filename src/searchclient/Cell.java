package searchclient;

public class Cell {
	Position position;
	int type; //0 = wall, 1 = Free, 2 = goal. To be swapped out with enum?
	boolean north = false;
	boolean east = false;
	boolean west = false;
	boolean south = false;
	
	public Cell(int row, int col){
		this.position = new Position(row,col);
		//More data to be entered later
	}
}
