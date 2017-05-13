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
	
	public Box(Character name, String color, Position position,boolean isBlocking) {
		super();
		this.name = name;
		this.color = color;
		this.position = position;
		this.isBlocking = isBlocking;
	}
	
	public Box(Box b){
		super();
		this.name = b.name;
		this.color = new String(b.color);
		this.position = new Position(b.position.row,b.position.col);
		this.isBlocking = isBlocking;
	}

	@Override
	public String toString() {
		return "Box [name=" + name + ", color=" + color + ", position=" + position +", isBlocking="+isBlocking+ "]";
	}
}
