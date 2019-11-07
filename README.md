# BankerLab3
The goal of this lab is to do resource allocation using both an optimistic resource manager and the banker's algorithm of Dijkstra. 

The proggram takes one command line argument which is the name of the file containing the input. I have included the input.txt file for reference. 
For this code the input.txt file must be within the same directory. The input.txt file must contain input as specified in the spec. 

There are 5 possible string activities. This includes: "initiate" "compute" "request" "release" "terminate"

At the end of the program - all of the output is printed. This includes the time taken for each task, the waiting time, and the percentage of time spent waiting. 

To Run Compile this Code on Crackle:

1. cd BankerLab3
2. javac Task.java banker.java

To Run: 

java banker input.txt


Side Notes: 
Note that for my code, I did not set the arbitrary limits on T and R. (optional)

Also after speaking to the professor, he said it was okay that my output is vertically printed rather than left and right adjusted. 

In the case of a deadlock - (Optimistic Algorithm)
- abort the lowest numbered deadlocked task after releasing all its resources
- If you detect the deadlock at cycle k, the task is aborted at cycle k and the resources become available at cycle k+1

The Banker's Algorithm also supports aborted tasks
- this happens if the requested resources exceed the claim
- errors are outputted when the program is run!