/**
 * Banker Lab 
 * Due: November 7th
 * @author sonamtailor
 * This lab does resource allocation using both an optimistic resource manager and the banker's algorithm of Dijkstra. 
 * 
 */
package banker;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.*;

public class banker {
	public static void main(String[] args) throws URISyntaxException, FileNotFoundException {
		
		File inputFile;
		//storing the random number file

		//verify that command line argument exists
		if (args.length == 0) {
			System.err.println("Usage Error: the program expects an argument.");
			System.exit(1);
		}
		
		inputFile = new File(args[0]);
		Scanner input = new Scanner(inputFile);
		
		int numTasks = input.nextInt();
		int numResources = input.nextInt();
		//two versions bc they are overwritten when passed by reference to the functions
		HashMap<Integer, Integer> resourcesOpt = new HashMap<>(numResources);
		HashMap<Integer, Integer> resourcesBank = new HashMap<>(numResources);
		
		//Storing our HashMaps
		//make two hashmaps to store the tasks for each method --> we pass by reference (assure changes to one does not affect the other
		HashMap<Integer, Task> optimisticTasks = new HashMap<>(numTasks);
		HashMap<Integer, Task> bankersTasks = new HashMap<>(numTasks);
		
		
		//for loop to store the quantity of each resource in the hashmap
		for(int i = 1; i <= numResources; i++) {
			int resourcenum = input.nextInt();
			resourcesOpt.put(i, resourcenum);
			resourcesBank.put(i, resourcenum);
			
		}
		
		//for loop to store and intialize the tasks
		for (int j = 1; j <= numTasks; j++) {
			Task temp1 = new Task(j, numResources); //initialize the number of resources
			Task temp2 = new Task(j, numResources);
			
			optimisticTasks.put(j,temp1);
			bankersTasks.put(j,temp2);
		}
		
		//code to accumulate and store the remaining data for each process
		int numTerminated = 0; //loop sentinel to determine when the file still has data 
		while (numTerminated!=numTasks) {
			String tempstr = input.next();
			int taskAssociated = input.nextInt(); //which task
			optimisticTasks.get(taskAssociated).addState(tempstr);
			bankersTasks.get(taskAssociated).addState(tempstr);
			
			int resourceAssociated = input.nextInt(); //stores 
			int num = input.nextInt(); //the number of resources we are either claiming, releasing, or requesting
			
			if(tempstr.equals("terminate")) {
				numTerminated+=1;
			}
			else if(tempstr.equals("initiate")) {
				optimisticTasks.get(taskAssociated).claim[resourceAssociated-1] = num;
				bankersTasks.get(taskAssociated).claim[resourceAssociated-1] = num;
			}
			else if (tempstr.equals("request")) {
				optimisticTasks.get(taskAssociated).addReqOrd(resourceAssociated);
				bankersTasks.get(taskAssociated).addReqOrd(resourceAssociated);
				optimisticTasks.get(taskAssociated).addReq(num);
				bankersTasks.get(taskAssociated).addReq(num);
			}
			else if (tempstr.equals("release")) {
				optimisticTasks.get(taskAssociated).addRelOrd(resourceAssociated);
				bankersTasks.get(taskAssociated).addRelOrd(resourceAssociated);
				optimisticTasks.get(taskAssociated).addRel(num);
				bankersTasks.get(taskAssociated).addRel(num);
			}
			//case of compute
			else {
				optimisticTasks.get(taskAssociated).addComp(resourceAssociated); //stores number of cycles to wait for
				bankersTasks.get(taskAssociated).addComp(resourceAssociated);
			}
			
		}
		//method call
		optimisticManager(numTasks, resourcesOpt, optimisticTasks);
		//outputs the FIFO - Optimistic Banker
		System.out.println("\t\tFIFO");
		int count = 0;
		int total = 0;
		int totalWait = 0;
		for (Task c : optimisticTasks.values()) {
			count++;
			if(c.aborted) {
				System.out.printf("\tTask %d\t\t%s\n",count, "aborted");
			}
			else {
				float perc = 100*((float)c.waitTime/(float)c.totalTime);
				int newperc = Math.round(perc);
				System.out.printf("\tTask %d\t\t%5d   %5d   %5d%%\n", count, c.totalTime, c.waitTime, newperc);
				totalWait+=c.waitTime;
				total+=c.totalTime;
			}
		}
		
		float totalperc = 100*((float)totalWait/(float)total);
		int roundtot = Math.round(totalperc);
		System.out.printf("\ttotal\t\t%5d   %5d   %5d%%\n\n",total, totalWait, roundtot);
		
		//method call
		bankManager(numTasks, resourcesBank, bankersTasks);
		//outputs the Bankers output - Dijkstra
		System.out.println("\t\tBANKER'S");
		count = 0;
		total = 0;
		totalWait = 0;
		for (Task c: bankersTasks.values()) {
			if (!c.error.equals("")) {
				System.out.println(c.error);
			}
		}
		for (Task c : bankersTasks.values()) {
			count++;
			if(c.aborted) {
				System.out.println("\tTask "+count + "\t\taborted");
			}
			else {
				float perc = 100*((float)c.waitTime/(float)c.totalTime);
				int newperc = Math.round(perc);
				System.out.printf("\tTask %d\t\t%5d   %5d   %5d%%\n", count, c.totalTime, c.waitTime, newperc);
				totalWait+=c.waitTime;
				total+=c.totalTime;
			}
		}
		
		totalperc = 100*((float)totalWait/(float)total);
		roundtot = Math.round(totalperc);
		System.out.printf("\ttotal\t\t%5d   %5d   %5d%%\n",total, totalWait, roundtot);
		
	}
	/*
	 * Optimistic Manager Method 
	 * This algorithm satisfies the request if possible, if not it makes the task wait - essentially when we have pending request we use a FIFO manner to run
	 * If we have a deadlock (in the case that all the resources are deadlocked, my code aborts the lowest numbered deadlocked task after releasing its resources.
	 * This process of aborting is continued until we are able to satisfy the requests of one of the resources
	 * 
	 * @param - numTasks - the number of tasks we have 
	 * @param HashMap<Integer,Integer> resources - stores the resource number as the key and the number of resources we have respectively (as the value). 
	 * @param HashMap<Integer, Task> optimisticTasks - stores the task number as the key and the actual Task object as the value 
	 * 
	 * The function is void and instead the changes are updates to the optimisticTasks hashmap which has been passed by reference.
	 */
	public static void optimisticManager(int numTasks, HashMap<Integer, Integer> resources, HashMap<Integer, Task> optimisticTasks) {
	//	int cycle = 0;
	//	Queue<Task> done = new LinkedList<>();
		Queue<Integer> toRemove = new LinkedList();
		ArrayList<Integer> wait = new ArrayList<>();
		Queue<Integer> blocked = new LinkedList<>();
		Queue<Integer> run = new LinkedList<>();
		Queue<Integer> toAdd = new LinkedList();
		HashMap<Integer, Integer> releasedres = new HashMap<>();
		//initialize run
		for(int c: optimisticTasks.keySet()) {
			run.add(c);
		}
		
		while (!blocked.isEmpty() || !run.isEmpty()) {
			numTasks = blocked.size()+run.size();
			
			if(!blocked.isEmpty()){
				if (blocked.size()==numTasks && blocked.size()!=1) { //case that all of our tasks are blocked - DEADLOCK case - we abort the lowest numbered deadlocked task after releasing all of its resources 
					//System.out.println(numTasks);
					for(int i: blocked) {
						wait.add(i); //this will be the array list to sort
					}
					Collections.sort(wait); //want lowest 
					int toAbort = wait.get(0); //the lowest numbered deadlocked task 
					
					//release all of the resources used by this aborted task
					Task aborted = optimisticTasks.get(toAbort);
					for(int res: aborted.taskResource.keySet()) { 
						int amountReleased = aborted.taskResource.get(res);
						int prevAmount = resources.get(res);
						int newTot = amountReleased + prevAmount;
						resources.put(res,newTot);
					}
					optimisticTasks.get(toAbort).aborted = true;
					blocked.remove(toAbort); //remove the aborted Task from the blocked queue it was originally stored in 
				}
				//case that we do not have a deadlock
				else {
					for(int i: blocked) {
						Task curr = optimisticTasks.get(i);
						String state = curr.activity.peek(); //what is activity
						int numReq = curr.requests.peek();
						int whichResource = curr.requestROrder.peek();
						if (state.equals("request")) {
							if(numReq<=resources.get(whichResource)) { //case that we have enough
								//update quantity that task has in the hashmap
								int newNumofRforTask = curr.taskResource.get(whichResource)+numReq;
								curr.taskResource.put(whichResource,newNumofRforTask);
								curr.activity.poll();
								curr.requestROrder.poll();
								curr.requests.poll();
								int newNumofR = resources.get(whichResource)-numReq;
								resources.put(whichResource, newNumofR);
								toRemove.add(i);//remove from blocked
								toAdd.add(i); //to add to run
								//System.out.println("In blocked request satisfied " + curr.totalTime + " for task " + curr.taskNum);
							}
							else{//case we dont have enough
								curr.waitTime++;
								
							}
						}
						curr.totalTime++;
					}
				}
				//remove the tasks that we aborted or are no longer blocked 
				for(int i: toRemove) {
					blocked.remove(i);			
				}
				toRemove.clear(); //reset
				wait.clear();
			}
			
			for(int i : run) {
				Task curr = optimisticTasks.get(i);
				String activity = curr.activity.peek();
				
				//initiate
				if(activity.equals("initiate")) {
//					for(int i = 0; i<c.claim.length; i++) {
//						if(c.claim[i]>resources.get(i+1)) {
//							c.aborted=true;
//							done.add(c);
//						}	
//					}
					curr.activity.poll();
					curr.totalTime++;
					
				}
				//release
				else if(activity.equals("release")) {
					int resourceRel = curr.releaseROrder.peek();
					int numRel = curr.releases.peek();

					//resources to be released - will iterate through after this cycle bc they become available after 
					releasedres.put(resourceRel, resources.get(resourceRel)+numRel);
					curr.releaseROrder.poll();
					curr.releases.poll();
					curr.activity.poll();
					curr.totalTime++;
					int whatOrig = curr.taskResource.get(resourceRel);
					int whatNew = whatOrig - numRel;
					curr.taskResource.put(resourceRel, whatNew);
					//System.out.println("Task: "+curr.taskNum+ " released "+numRel +" of "+resourceRel);
				}
				
				//terminate
				else if(activity.equals("terminate")) {
					curr.activity.poll();
					curr.terminated = true;
					toRemove.add(curr.taskNum);
				
				}
				
				else if(activity.equals("request")) {
					int numReq = curr.requests.peek();
					int whichResource = curr.requestROrder.peek();
					if(numReq<=resources.get(whichResource)) { //case that we have enough
						//update quantity that task has in the hashmap
						int newNumofRforTask = curr.taskResource.get(whichResource)+numReq;
						curr.taskResource.put(whichResource,newNumofRforTask);
						curr.activity.poll();
						curr.requestROrder.poll();
						curr.requests.poll();
						int newNumofR = resources.get(whichResource)-numReq;
						resources.put(whichResource, newNumofR);
						//System.out.println("task: "+curr.taskNum+ " in main wanted "+ numReq +" of "+ whichResource +" success and we have "+resources.get(whichResource) );
					}
					else { //case that we do not have enough
						toRemove.add(curr.taskNum);
						blocked.add(curr.taskNum); //add task to the blocked list for the next cycle 
						curr.waitTime++;
						//System.out.println("task: "+curr.taskNum+ " in main but wanted "+ numReq +" of "+ whichResource +" but could not" );
					}
					
					curr.totalTime++;
					
				}
				//case of compute 
				else {
					int val = curr.computes.peek();
					if(val==curr.valWait) {
						curr.computes.poll();
						curr.activity.poll();
						curr.valWait = 1;
					}
					else {
						curr.valWait++;
					}
					curr.totalTime++;
				}
				
				int prev = curr.totalTime -1;
				//System.out.println("Made it to " + prev + "-" + curr.totalTime + " for task " + curr.taskNum+"\n");
				
			}
			
			//update released for next cycle
			for(int i : releasedres.keySet()) {
				resources.put(i, releasedres.get(i));
			}
			for(int i: toAdd) {
				run.add(i);
			}
			for(int i: toRemove) {
				run.remove(i);
			}
			
			//reset 
			toAdd.clear();
			toRemove.clear();
			releasedres.clear();
			
		}
		
		//return done;
	}
	/*
	 * Bankers Manager Method 
	 * This algorithm satisfies the request if the number of resourses requested does not exceed the claim
	 * if not it makes the task wait - essentially when we have pending request 
	 * We abort tasks if the resources requested exceeds the initial claim made by the task
	 * 
	 * @param - numTasks - the number of tasks we have 
	 * @param HashMap<Integer,Integer> resources - stores the resource number as the key and the number of resources we have respectively (as the value). 
	 * @param HashMap<Integer, Task> bankersTasks - stores the task number as the key and the actual Task object as the value 
	 * 
	 * The function is void and instead the changes are updates to the bankersTasks hashmap which has been passed by reference.
	 */
	public static void bankManager(int numTasks, HashMap<Integer, Integer> resources, HashMap<Integer, Task> bankersTasks) {
		Queue<Integer> toRemove = new LinkedList();
		ArrayList<Integer> wait = new ArrayList<>();
		Queue<Integer> blocked = new LinkedList<>();
		Queue<Integer> run = new LinkedList<>();
		Queue<Integer> toAdd = new LinkedList();
		HashMap<Integer, Integer> releasedres = new HashMap<>();
		int newNumofRforTask = 0;

		
		//initialize run
		for(int c: bankersTasks.keySet()) {
			run.add(c);
		}
		
		//set the needs hashmap
		for(Task c: bankersTasks.values()) {
			for(int i = 0; i < c.claim.length; i++) {
				c.needs.put(i+1, c.claim[i]); //each task needs the claimed amount 
			}
		}
		
		while (!blocked.isEmpty() || !run.isEmpty()) {
			if(!blocked.isEmpty()){
					for(int i: blocked) {
						Task curr = bankersTasks.get(i);
						String state = curr.activity.peek(); //what is activity
						int numReq = curr.requests.peek();
						int whichResource = curr.requestROrder.peek();

					//	System.out.print("\n\nTask "+curr.taskNum+" requests "+ numReq + " and we have "+ resources.get(whichResource));
						if (state.equals("request")) {
							
							//determines whether it is safe request
							boolean safe = true;
							for(int p: resources.keySet()) {
								if(resources.get(p) < curr.needs.get(p)) {
									safe = false; //if we cannot meet the claim of one of the resources 
								}
							}
							if(safe) {//case that it is safe
								//update quantity that task has in the hashmap
								newNumofRforTask = curr.taskResource.get(whichResource)+numReq;
								
								//check if requests is > claim -- abort
								if(curr.taskResource.get(whichResource) + numReq > curr.claim[whichResource-1]) {
									curr.aborted = true;
									int endcurrTime = curr.totalTime + 1;
									curr.error = "During cycle " + curr.totalTime + "-" + endcurrTime + " of Banker's Algorithms " + " \n\tTask " +curr.taskNum + "'s request exceeds its claim; aborted; " + curr.taskResource.get(whichResource) + " units available next cycle\n";
									releasedres.put(whichResource, curr.taskResource.get(whichResource));
									toRemove.add(curr.taskNum);
								}
								//case that we can meet the request
								else {	
									curr.taskResource.put(whichResource,newNumofRforTask);
									curr.activity.poll();
									curr.requestROrder.poll();
									curr.requests.poll();
									
									int newNumofR = resources.get(whichResource)-numReq;
									resources.put(whichResource, newNumofR);
									toRemove.add(i);//remove from blocked
									toAdd.add(i); //to add to run
									//curr.numRequested[whichResource-1]+=numReq;
									int newNeed = curr.claim[whichResource -1]-newNumofRforTask;
									curr.needs.put(whichResource, newNeed);
								}
							}
							else{//case unsafe

								curr.waitTime++;
							}
						}
						//System.out.println("After the " + curr.totalTime + " time we have " + resources.get(1)+ " resources -- during blocked ");
						curr.totalTime++;
					}
				
				for(int i: toRemove) {
					blocked.remove(i);			
				}
				for(int i : releasedres.keySet()) {
					int oldVal = resources.get(i);
					int newVal = oldVal + releasedres.get(i);
					resources.put(i, newVal);
				}
				
				toRemove.clear(); //reset
				wait.clear();
			}

			for(int i : run) {
				Task curr = bankersTasks.get(i);
				String activity = curr.activity.peek();
				int resourcenum = 0; 
				//initiate
				if(activity.equals("initiate")) {
					boolean abort = false;
					for(int j = 0; j < curr.claim.length; j++) {
						if (curr.claim[j] > resources.get(j+1)) {
							abort = true;
							resourcenum = j+1;
						}
					}
					//case that intialized claim val exceeds the number of resources we have
					if(abort) {
						curr.aborted = true;
						curr.error = "Banker aborts task " + curr.taskNum + " before run begins: " + " \n\tclaim for resource " + resourcenum + " (" + curr.claim[resourcenum-1] + ") exceeds number of units present (" + resources.get(resourcenum) + ")\n";  
						toRemove.add(curr.taskNum);
						
					}
					else {
						curr.activity.poll();
						curr.totalTime++;
					}
				}
				//release
				else if(activity.equals("release")) {
					int resourceRel = curr.releaseROrder.peek();
					int numRel = curr.releases.peek();

					
					//resources.put(resourceRel, resources.get(resourceRel)+numRel);
					releasedres.put(resourceRel, numRel); //puts 3 
					curr.releaseROrder.poll();
					curr.releases.poll();
					curr.activity.poll();
					curr.totalTime++;
					int whatOrig = curr.taskResource.get(resourceRel);
					int whatNew = whatOrig - numRel;
					curr.taskResource.put(resourceRel, whatNew);
					//update need amount 
					int newNeed = curr.claim[resourceRel-1] - curr.taskResource.get(resourceRel);
					curr.needs.put(resourceRel, newNeed);
					
					//check if now done 
					//System.out.println(curr.taskNum + " "+ curr.activity.peek());
					if(curr.activity.peek().equals("terminate")) {
						curr.activity.poll();
						curr.terminated = true;
						toRemove.add(curr.taskNum);
					}

				}
				
				//terminate
				else if(activity.equals("terminate")) {
					//System.out.println(curr.taskNum +" "+ curr.activity.peek());
					curr.activity.poll();
					curr.terminated = true;
					toRemove.add(curr.taskNum);
				
				}
				
				//request
				else if(activity.equals("request")) {
					int numReq = curr.requests.peek();
					int whichResource = curr.requestROrder.peek();
					//debug
					//System.out.println("Task Num "+ curr.taskNum + " with total time as "+ curr.totalTime);
					
					//determines whether it is safe 
					boolean safe = true;
					for(int p: resources.keySet()) {
						if(resources.get(p) < curr.needs.get(p)) { //not safe - not sufficient resources 
							safe = false; 
						}
					}

					if(safe) { //case that it is safe 
						
						//check if requests is > claim -- abort
						if(curr.taskResource.get(whichResource) + numReq > curr.claim[whichResource-1]) {
							curr.aborted = true;
							int endcurrTime = curr.totalTime + 1;
							curr.error = "During cycle " + curr.totalTime + "-" + endcurrTime + " of Banker's Algorithms " + " \n\tTask " +curr.taskNum + "'s request exceeds its claim; aborted; " + curr.taskResource.get(whichResource) + " units available next cycle\n";
							releasedres.put(whichResource, curr.taskResource.get(whichResource));
							toRemove.add(curr.taskNum);
						}
						else {
							//update quantity that task has in the hashmap
							newNumofRforTask = curr.taskResource.get(whichResource)+numReq;
							curr.taskResource.put(whichResource,newNumofRforTask);
							curr.activity.poll();
							curr.requestROrder.poll();
							curr.requests.poll();
							int newNumofR = resources.get(whichResource)-numReq;
							resources.put(whichResource, newNumofR);
							int newNeed = curr.claim[whichResource-1]-newNumofRforTask;
							curr.needs.put(whichResource, newNeed);
							//curr.numRequested[whichResource-1]+=numReq;
						}
					}
					else { //case that it is unsafe
						toRemove.add(curr.taskNum);
						blocked.add(curr.taskNum);
						curr.waitTime++;
					}
					//System.out.println("After the " + curr.totalTime + " time we have " + resources.get(1)+ " resources -- during reg");
					curr.totalTime++;
					
				}
				//case of compute 
				else {
					int val = curr.computes.peek();
					if(val==curr.valWait) {
						curr.computes.poll();
						curr.activity.poll();
						curr.valWait = 1;
					}
					else {
						curr.valWait++;
					}
					curr.totalTime++;
				}
				//System.out.println("We made it to " + curr.totalTime + " for "+curr.taskNum);
			}
			
			//update released for next cycle
			for(int i : releasedres.keySet()) {
				int oldVal = resources.get(i);
				int newVal = oldVal + releasedres.get(i);
				resources.put(i, newVal);
			}
			for(int i: toAdd) {
				run.add(i);
			}
			for(int i: toRemove) {
				run.remove(i);
			}
			//reset
			toAdd.clear();
			toRemove.clear();
			releasedres.clear();
			
		}
	}
				
}