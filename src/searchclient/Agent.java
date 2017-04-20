package searchclient;

public class Agent {
	
	public int name;
	public String color;
	public Position position;
	public Node initialState;
	public boolean isTrapped = false;
	
	public Agent(int name, String color, Position position, Node initialState) {
		this.name = name;
		this.color = color;
		this.position = position;
		this.initialState = initialState;
	}
	
	public Agent(int name, Node initialState) {
		this.name = name;
		this.initialState = initialState;
	}

	public void assignInitialState(Node initialState){
		this.initialState = initialState;
	}
	

	@Override
	public String toString() {
		return "Agent [name=" + name + ", color=" + color + " position=" + position + " initialState=" + initialState + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + name;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Agent other = (Agent) obj;
		if (name != other.name)
			return false;
		return true;
	}
	
	
}
