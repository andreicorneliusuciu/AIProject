package searchclient;

public class Goal {
	
	public Character name;
	public Position position;
	public boolean isSatisfied;
	
	public Goal(Character name, Position position) {
		
		this.name = name;
		this.position = position;
		this.isSatisfied = false;
	}	
}
