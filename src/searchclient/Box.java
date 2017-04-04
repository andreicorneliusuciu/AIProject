package searchclient;

public class Box {
	
	public String name;
	public String color;
	public Position position;
	
	public Box(String name, String color, Position position) {
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
