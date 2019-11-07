//package banker;
import java.util.*;

public class Task {
	int waitTime = 0;
	int totalTime = 0;
	int numResources; //need to set
	int taskNum;
	boolean terminated = false; 
	int[] claim; 
	boolean aborted = false;
	int valWait = 1;

	String error = "";
	
	//used for bankers algorithm to store the number of each resource that the task needs - helps to determine whether state is safe or unsafe
	HashMap<Integer, Integer> needs = new HashMap<>();
	
	//stores resource number and quantity
	HashMap<Integer, Integer> taskResource = new HashMap<>();
	Queue<String> activity = new LinkedList<>();
	
	//to store which order resources are released and requested in (how many)
	Queue<Integer> requests = new LinkedList<>();
	Queue<Integer> releases = new LinkedList<>();
	Queue<Integer> computes = new LinkedList<>();

	//to store which resources are released and requested (which resource)
	Queue<Integer> requestROrder = new LinkedList<>();
	Queue<Integer> releaseROrder = new LinkedList<>();
	
	
	public Task(int taskNum, int numResources) {
		this.numResources = numResources;
		this.taskNum = taskNum;
		setHas(numResources);
		claim = new int[numResources]; //list to store the claims of each resource
	}
	
	//this method initializes the quantity that the process has of each resource
	public void setHas(int numResources) {
		for (int i = 1; i <= numResources; i++) {
			taskResource.put(i,0); //initializes the quantity that the task has of each resource to 0
			
		}
	}
	
	public void addState(String state) {
		activity.add(state);
	}
	
	public void addReq(int val) {
		requests.add(val);
	}
	
	public void addRel(int val) {
		releases.add(val);
	}
	
	public void addReqOrd(int val) {
		requestROrder.add(val);
	}
	
	public void addRelOrd(int val) {
		releaseROrder.add(val);
	}
	
	public void addComp(int val) {
		computes.add(val);
	}
	
}
