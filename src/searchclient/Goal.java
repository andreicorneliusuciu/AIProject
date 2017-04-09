package searchclient;

public class Goal {
	public Character name;
	public String color;
	public Position position;
	public boolean isSatisfied = false;
	public Integer priority=100; //100% is lowest priority. 
	
	public Goal(Character name, String color, Position position) {
		this.name = name;
		this.color = color;
		this.position = position;
	}

	public Goal(Character name, String color, Position position, Integer priority) {
		this.name = name;
		this.color = color;
		this.position = position;
		this.priority = priority;
	}
	
	
	public void lowerPriority(int amount)
	{
		if(this.priority-amount>=0)
		{
			this.priority -=amount;
		}
		else
		{
			this.priority = 0;
		}
		}
	
	@Override
	public String toString() {
		return "Goal [name=" + name + ", color=" + color + " pos= " + position + ", isSatisfied=" + isSatisfied + "]";
	}
}
