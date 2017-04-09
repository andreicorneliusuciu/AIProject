package searchclient;

public class Box {
	
	public String name;
	public String color;
	public Position position;
	public Boolean isStorage=false;
	
	public Box(String name, String color, Position position) {
		super();
		this.name = name;
		this.color = color;
		this.position = position;
	}
	
	public Box(String name, String color, Position position, Boolean isStorage) {
		super();
		this.name = name;
		this.color = color;
		this.position = position;
		this.isStorage = isStorage;
	}
	

	@Override
	public String toString() {
		return "Box [name=" + name + ", color=" + color + ", position=" + position + "]";
	}
}
