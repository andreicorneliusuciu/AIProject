package searchclient;

import java.util.ArrayList;

public class Line {
	public ArrayList<Position> positions;
	public boolean goalLine = false;
	public boolean deadEnd = false;
	public ArrayList<Integer> east;
	public ArrayList<Integer> west;
	public ArrayList<Integer> south;
	public ArrayList<Integer> north;
	
	public Line(){
		east = new ArrayList<Integer>();
		west = new ArrayList<Integer>();
		south = new ArrayList<Integer>();
		north = new ArrayList<Integer>();
	}
}
