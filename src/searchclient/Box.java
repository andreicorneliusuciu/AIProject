package searchclient;

public class Box implements Comparable<Box> {

	public Character name;
	public String color;
	public Position position;
	public boolean isBlocking = false;

	public Box(Character name, String color, Position position) {
		super();
		this.name = name;
		this.color = color;
		this.position = position;
	}

	public Box(Character name, String color, Position position, boolean isBlocking) {
		super();
		this.name = name;
		this.color = color;
		this.position = position;
		this.isBlocking = isBlocking;
	}

	public Box(Box b) {
		super();
		this.name = b.name;
		this.color = new String(b.color);
		this.position = new Position(b.position.row, b.position.col);
		this.isBlocking = b.isBlocking;
	}

	public boolean isOnOwnGoal() {
		for (Goal g : SearchClient.allGoals) {
			if (this.position.equals(g.position) && g.name.equals(Character.toLowerCase(this.name))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "Box [name=" + name + ", color=" + color + ", position=" + position + ", isBlocking=" + isBlocking + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + (isBlocking ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
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
		Box other = (Box) obj;
		if (color == null) {
			if (other.color != null)
				return false;
		} else if (!color.equals(other.color))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}

	@Override
	public int compareTo(Box o) {
		// TODO Auto-generated method stub
		return name.compareTo(o.name);
	}
}