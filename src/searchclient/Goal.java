package searchclient;

public class Goal implements Comparable<Goal> {
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

	@Override
	public int compareTo(Goal o) {
		// TODO Auto-generated method stub
		return name.compareTo(o.name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
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
		Goal other = (Goal) obj;
		if (color == null) {
			if (other.color != null)
				return false;
		} else if (!color.equals(other.color))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}
	
	
}
