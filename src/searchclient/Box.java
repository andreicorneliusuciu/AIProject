package searchclient;

public class Box {
	
	public char name;
	public String color;
	public Position position;
	
	public Box(char name, String color, Position position) {
		
		this.name = name;
		this.color = color;
		this.position = position;
	}
	
	@Override
	public String toString() {
		return "Name = " + name + " Color = " + color + " Position = " + position + "\n";
	}
}
