package searchclient;

public class Box {
	
	public Character name;
	public String color;
	public Position position;
	
	public Box(Character name, String color, Position position) {
		super();
		this.name = name;
		this.color = color;
		this.position = position;
	}

	@Override
	public String toString() {
		return "Box [name=" + name + ", color=" + color + ", position=" + position + "]";
	}
}
