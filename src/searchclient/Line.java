package searchclient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Line {
	public ArrayList<Position> positions;
	public boolean goalLine = false;
	public boolean deadEnd = false;
	public Set<Integer> east;
	public Set<Integer> west;
	public Set<Integer> south;
	public Set<Integer> north;
	public boolean isInSuperNode = false;
	public int superNodeID = -1;
	public Line(){
		east = new HashSet<Integer>();
		west = new HashSet<Integer>();
		south = new HashSet<Integer>();
		north = new HashSet<Integer>();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((positions == null) ? 0 : positions.hashCode());
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
		Line other = (Line) obj;
		if (positions == null) {
			if (other.positions != null)
				return false;
		} else if (!positions.equals(other.positions))
			return false;
		return true;
	}
	
	
}
