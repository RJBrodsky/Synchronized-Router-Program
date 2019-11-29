import java.util.LinkedList; //Imported to Keep track of the packets

/*
 * This runnable routes packets as they traverse the network.
 */
class Router implements Runnable {

	private LinkedList<Packet> list = new LinkedList<Packet>(); //Packet List for packets that need to be processed.
	private int routes[]; //An array for possible routes, "Routing Table"
	private Router routers[]; //An Array of the routers 
	private int routerNum; //Variable for Router number.
	private boolean end = false; //Variable to mark when end has been called.
	private boolean done = false; //Variable to mark when we are done.
	Packet currentPacket = null; // Variable to track current packet

	Router(int rts[], Router rtrs[], int num) {
		routes = rts;
		routers = rtrs;
		routerNum = num;
	}
	
	/*
	 * Add a packet to this router.
	 * This method is Synchronized on List. It adds the packet to the LinkedList,
	 * then it Calls notifyAll to wake up any waiting threads.
	 */
	public void addWork(Packet p) {
		synchronized (list) {
			list.add(p); //Adds packet to list
			list.notifyAll(); //notify's all threads
		}
	}
	
	/*
	 * End the thread, once no more packets are outstanding.
	 * Sets the end boolean to true when called
	 * Notify's all when done.
	 */
	public synchronized void end() {
		synchronized (list) {
			end = true; //Sets end to true
			list.notifyAll(); //notify's all threads
		}
	}

	/*
	 * This method is called when the network is empty.
	 * It sets the done boolean to true
	 * Notify's all when done.
	 */
	public synchronized void networkEmpty() {
		synchronized (list) {
			done = true; //Sets done to true
			list.notifyAll(); //notify's all threads
		}
	}

	/*
	 * This is the run method. It runs until the list is empty and done and end have been called.
	 * If the list is empty it waits.
	 * If the list is empty and done and end have been called it returns.
	 * Else it will Processes the Packet. 
	 * 			--> if the destination == routerNum it decrements the packet count
	 * 			--> if the destination !- routerNum it addsWork to the next router
	 */
	public void run() {

		while(!(done && end && list.isEmpty()))	{ //Loop until done and end have been called and list is empty
			synchronized(list) { //synchronized on list
				while(list.isEmpty())	{ //if the list is empty
					try	{
						list.wait(); //call wait on list
					} catch (InterruptedException e) {	//Catches interrupted exception				
						System.out.println("Error: " + e); //Prints error message
					}
					
					if(done == true && end == true && list.isEmpty()) { //if done and end have been called and list is empty
						return; //done
					}
				}

				int currentRouter = this.routerNum; //keeps track of the current router
				currentPacket = list.remove(); //Removes the next packet from the list
				currentPacket.Record(currentRouter); //records the path for the current router
			}

			int destination = currentPacket.getDestination(); //Keeps track of the packets destination
			int nextRouter = routes[currentPacket.getDestination()]; //keeps track of the next router

			if (destination == routerNum) { //if the destination equals the routerNum	
				//System.out.println("Packet Count Decremented " + routing.packetCount);
				routing.decPacketCount(); //Decrements the packet count

			}

			if (destination != routerNum) { //if the destination does not equal the routerNum
				//System.out.println("Adding Work to " + nextRouter);
				routers[nextRouter].addWork(currentPacket); //Adds work to the next router
			}
		}
	}
}