package searchclient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SuperNode {
	Set<Line> memberLines = new HashSet<Line>();
	Set<SuperNode> internalGoalNodes = new HashSet<SuperNode>();
	Set<Integer> connectedID = new HashSet<Integer>();
	public Set<Integer> east = new HashSet<Integer>();
	public Set<Integer> west = new HashSet<Integer>();
	public Set<Integer> south = new HashSet<Integer>();
	public Set<Integer> north = new HashSet<Integer>();
	boolean absorbed = false;
	boolean goalSuperNode = false;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((memberLines == null) ? 0 : memberLines.hashCode());
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
		SuperNode other = (SuperNode) obj;
		if (memberLines == null) {
			if (other.memberLines != null)
				return false;
		} else if (!memberLines.equals(other.memberLines))
			return false;
		return true;
	}
}
