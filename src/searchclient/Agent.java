package searchclient;

public class Agent {
	
	public String color;
	public int name;
	public Position position;
	public Node node;
	
	public Agent( int name,String color, Position position, Node node) {
		this.color = color;
		this.name = name;
		this.position = position;
		this.node = node;
	}
}
