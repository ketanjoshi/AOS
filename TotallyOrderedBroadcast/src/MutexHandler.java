
public class MutexHandler {
	
	public void csEnter(ClusterNode clusterNode)
	{
		MutexPriorityQueueElement qElement = new MutexPriorityQueueElement(clusterNode.getId(), clusterNode.getLogicalClock());
		MutexGlobals.addNodeToPriorityQueue(qElement);
		//Initiate sender thread to broadcast the mutex request message
	}
	public void csLeave(ClusterNode clusterNode)
	{
		MutexPriorityQueueElement qElement = new MutexPriorityQueueElement(clusterNode.getId(), clusterNode.getLogicalClock());
		MutexGlobals.removeNodeFromPriorityQueue(qElement);
		//Initiate sender thread to broadcast the mutex leave message
	}
	
}
