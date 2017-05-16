package searchclient;

import java.util.List;

public class Goal {
	public Character name;
	public String color;
	public Position position;
	public boolean priorityGiven = false;
	public boolean isSatisfied = false;
	public boolean assigned = false;
	public Integer priority = 10; //lower for more priority
	
	public Goal(Character name, String color, Position position) {
		this.name = name;
		this.color = color;
		this.position = position;
	}
	
	public Goal(Goal g){
		this.name = g.name;
		this.color = new String(g.color);
		this.position = new Position(g.position.row,g.position.col);
		this.priority = g.priority;
		this.isSatisfied = g.isSatisfied;
		this.priorityGiven = g.priorityGiven;
		this.assigned = g.assigned;
	}
	
	public void lowerPriority(int i)
	{
		if(priority-i>0)
		{
			priority-=i;
		}
		else
		{
			priority =0;
		}
	}

	

	@Override
	public String toString() {
		return "Goal [name=" + name + ", color=" + color + " pos= " + position + ", isSatisfied=" + isSatisfied + "]";
	}
}
