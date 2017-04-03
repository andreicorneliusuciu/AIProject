package searchclient;

public class Goal {
	
	public Character name;
	public Position position;
	public boolean isSatisfied;
	public String color;
	
	public Goal(Character name, Position position, String color) {
		
		this.name = name;
		this.position = position;
		this.color = color;
		this.isSatisfied = false;
	}
	
	@Override
	public String toString() {
		return "Goal = " + name + " Color = " + color + " Position = " + position + "\n";
	}
}
