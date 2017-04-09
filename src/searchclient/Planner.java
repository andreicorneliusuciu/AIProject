package searchclient;

import java.util.List;

public class Planner {

	public Agent agent;
	public Node state;
	//we need the initialState of the agent to see everything but the other agents goals.
	
	//agent contains specialised initialNode
	public Planner(Agent agent)
	{
		
		//TODO Make simplified level where commands have immediate effects.
		//example: MoveBoxToGoal(1,A,a) moves A to a immediately. 
		//Make high-level plan based on such moves.
		//example: FreeAgent(1),MoveBoxToGoal(A,a),MoveBoxToGoal(B,b), StoreBox(B,Position x)
		//Take the moves, use classical heuristic methods to implement them. Use custom, appropriate goalStates.
		//Make low-level plan for the agent (solution).
		//feed the result to conflict resolution class. 
		
		this.agent=agent;
		this.state = agent.initialState;
		
		
	}
	
	
	
	public Node MoveBoxToGoal(Box box,Goal goal)
	{
		Node newState = state;	
		
		if(!state.boxes2.contains(box))
		{
			System.err.println("404: Box not found. Shit");
			return null;
		}
		else
		{
			for(Box b : newState.boxes2)
			{
				
			}
			
		}
		
		return newState;
	}
	
	public void FreeAgent(Agent agent)
	{
	//TODO look if any agent has isTrapped = true. Move closest?? same color box to storage
	//Always calls StoreBox()		
	}
	
	public void StoreBox(Box box, Position pos)
	{
	//if no isStorage cells exist		
	}
	
	
	
	
	
}
