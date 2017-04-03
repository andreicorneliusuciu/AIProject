package searchclient;

public class Agent {
		
	public int name;
	public String color;
	public Position position;
	public Node node;
	
	public Agent(String color, int name, Position position, Node node) {
		this.color = color;
		this.name = name;
		this.position = position;
		this.node = node;
	}
}
