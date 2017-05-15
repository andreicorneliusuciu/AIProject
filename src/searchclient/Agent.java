package searchclient;

import java.util.Comparator;

public class Agent implements Comparator<Agent>, Comparable<Agent>{
	
	public int name;
	public String color;
	public Position position;
	public Node initialState;
	public boolean isTrapped = false;
	public Position previousGoal = null;
	
	public Agent(int name, String color, Position position, Node initialState) {
		this.name = name;
		this.color = color;
		this.position = position;
		this.initialState = initialState;
		

	}
	
	public Agent(Agent a){
		this.name = a.name;
		this.color = a.color;
		this.position = new Position(a.position.row, a.position.col);
		if(a.initialState != null){
			this.initialState = a.initialState.Copy();
		} else {
			this.initialState = null;
		}
		this.previousGoal = a.previousGoal;
	}
	
	public Agent(int name, Node initialState) {
		this.name = name;
		this.initialState = initialState;
	}

	public void assignInitialState(Node initialState){
		this.initialState = initialState;
	}
	
	
	public int compareTo(Agent agent) {
		Integer thisName = new Integer(this.name);
	      return (thisName).compareTo(agent.name);
	   }

	   // Overriding the compare method to sort the age 
	   public int compare(Agent d, Agent d1) {
	      return d.name - d1.name;
	   }
	
	

	@Override
	public String toString() {
		return "Agent [name=" + name + ", color=" + color + " position=" + position + "isTrapped "+isTrapped+" initialState= \n" + initialState + "]";
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
