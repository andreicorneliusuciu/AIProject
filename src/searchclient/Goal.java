package searchclient;

public class Goal {
	public Character name;
	public String color;
	public Position position;
	public boolean isSatisfied = false;
	public Integer priority = 10; //lower for more priority
	
	public Goal(Character name, String color, Position position) {
		this.name = name;
		this.color = color;
		this.position = position;
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
