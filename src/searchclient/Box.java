package searchclient;

public class Box {
	
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
	
	
//	public Box(String name, Position position) {
//		this.name = name;
//		this.position = position;
//	}
//	
//	public Box(Position position) {
//		this.position = position;
//	}
	
	
	

	@Override
	public String toString() {
		return "Box [name=" + name + ", color=" + color + ", position=" + position + "]";
	}
}
