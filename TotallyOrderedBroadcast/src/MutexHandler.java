
public class MutexHandler {
	
	public void csEnter(ClusterNode clusterNode)
	{
		MutexPriorityQueueElement qElement = new MutexPriorityQueueElement(clusterNode.getId(), clusterNode.getLogicalClock());
		MutexGlobals.addNodeToPriorityQueue(qElement);
		MutexGlobals.setMutexReqClock(clusterNode.getLogicalClock());
		
		Message msg = new Message(clusterNode.getId(), null, MessageType.MUTEX_REQUEST, clusterNode.getLogicalClock());
		
		//Initiate sender thread to broadcast the mutex request message
		LamportsSender lamportSender = new LamportsSender(msg,null);
		Thread thread = new Thread(lamportSender);
        thread.start();
	}
	public void csLeave(ClusterNode clusterNode)
	{
		MutexPriorityQueueElement qElement = new MutexPriorityQueueElement(clusterNode.getId(), clusterNode.getLogicalClock());
		MutexGlobals.removeNodeFromPriorityQueue(qElement);
		
		Message msg = new Message(clusterNode.getId(), null, MessageType.CS_LEAVE, clusterNode.getLogicalClock());
		
		//Initiate sender thread to broadcast the mutex leave message
		LamportsSender lamportSender = new LamportsSender(msg,null);
		Thread thread = new Thread(lamportSender);
        thread.start();
	}
	
}
