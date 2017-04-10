package searchclient;

public class Planner {

	public Agent agent;
	public Node state;
	// we need the initialState of the agent to see everything but the other
	// agents goals.

	// agent contains specialised initialNode
	public Planner(Agent agent) {

		// TODO Make simplified level where commands have immediate effects.
		// example: MoveBoxToGoal(1,A,a) moves A to a immediately.
		// Make high-level plan based on such moves.
		// example: FreeAgent(1),MoveBoxToGoal(A,a),MoveBoxToGoal(B,b),
		// StoreBox(B,Position x)
		// Take the moves, use classical heuristic methods to implement them.
		// Use custom, appropriate goalStates.
		// Make low-level plan for the agent (solution).
		// feed the result to conflict resolution class.

		this.agent = agent;
		this.state = agent.initialState;

	}

	public Planner(Node initialState) // constructor just for testing
	{

		// this.agent=agent;
		this.state = initialState;
		
		
		System.err.println("\n ///////////////////////////////// \n");
		System.err.println("Initial:"+state.boxes2.get(0));

		Node newN = MoveBoxToGoal(state,state.boxes2.get(0), state.goals2.get(0));
		System.err.println("MoveToGoal at "+state.goals2.get(0).position+" :"+newN.boxes2.get(0));

		
		Node newN1 = StoreBox(state, state.boxes2.get(0));
		System.err.println("StoreBox "+state.boxes2.get(0).name+" :"+newN1.boxes2.get(0));
		System.err.println("\n ///////////////////////////////// \n");

	}

	public Node MoveBoxToGoal(Node node, Box box, Goal goal) {
		// find box and goal in the node and update their values

		Node newState = node;

		Position goalPos = null;

		// find the goal position
		if (!newState.goals2.contains(goal)) {
			System.err.println("404: Goal not found. Shit");
			return null;
		} else {
			for (Goal b : newState.goals2) {

				if (b == goal) {
					goalPos = b.position;
					break;
				}
			}

		}
		// find the box, set its position to goalPosition
		if (!newState.boxes2.contains(box)) {
			System.err.println("404: Box not found. Shit");
			return null;
		} else {
			for (Box b : newState.boxes2) {

				if (b == box) {
					b.position = goalPos;
					break;
				}
			}

		}
		return newState;
	}

	public Node FreeAgent(Agent agent) {
		// TODO look if any agent has isTrapped = true. Move closest(??) same
		// color box to storage
		// Always calls StoreBox()

		
		
		if (!agent.isTrapped) {
			return null;
		}

		Box blockingBox = null;

		// find blocking boxes in node
		for (Box b : agent.initialState.boxes2) {
			if (b.isBlocking) {
				blockingBox = b;
				break;
			}
		}
		

		Node tempNode = StoreBox(agent.initialState, blockingBox);

		return tempNode;
	}

	public Node StoreBox(Node node, Box box) {
		//TODO if no isStorage cells exist ?

		Node newState = node;
		
		float length = 100;
		Position shortestPos = null;

		// find shortest manhattan distance available position
		
		if(!Heuristic.storageSpace.isEmpty()){
		for (Position p : Heuristic.storageSpace) 
		{
			if ((Math.abs((p.row - box.position.row) + Math.abs((p.col - box.position.col)))) < length) 
			{
				length = (Math.abs((p.row - box.position.row) + Math.abs((p.col - box.position.col))));
				shortestPos = p;
			}
		}
		}
		else
		{
			System.err.println("No storage space available");
			//TODO what to do if no space available. Maybe move randomly.
			return node;
		}
/////////////////////////////////////////////////////////////////////////////
		
		
		// find the box, set its position
		if (!newState.boxes2.contains(box)) {
			System.err.println("404: Box not found. Shit");
			return null;
		} else {
			for (Box b : newState.boxes2) {

				if (b == box) {
					b.position = shortestPos;
					break;
				}
			}

		}
		return newState;
	}

}
