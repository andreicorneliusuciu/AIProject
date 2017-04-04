package searchclient;

public class Goal {
	public Character name;
	public String color;
	public Position position;
	public boolean isSatisfied = false;
	
	public Goal(Character name, String color, Position position) {
		this.name = name;
		this.color = color;
		this.position = position;
	}

	@Override
	public String toString() {
		return "Goal [name=" + name + ", color=" + color + " pos= " + position + ", isSatisfied=" + isSatisfied + "]";
	}
}
