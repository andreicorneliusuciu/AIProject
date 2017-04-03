package searchclient;

public class Box {
	
	public char name;
	public String color;
	public Position pos;
	public Node initialstate;
	
	
	Box(char name, String color, Position pos,Node intialstate){
		
		this.name = name;
		this.color = color;
		this.pos = pos;
		this.initialstate = initialstate;
	}
	
}
