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
    public int hashCode() {
        int hash = 17;
        hash = ((hash + row) << 5) - (hash + row);
        hash = ((hash + col) << 5) - (hash + col);
        return hash;
    }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (col != other.col)
			return false;
		if (row != other.row)
			return false;
		return true;
	}
}
