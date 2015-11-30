
public class MutexHandler {
	
	public void csEnter()
	{
		MutexPriorityQueueElement qElement = new MutexPriorityQueueElement(MutexGlobals.id, TobMutexGlobals.getLogicalclock());
		MutexGlobals.addNodeToPriorityQueue(qElement);
		MutexGlobals.setMutexReqClock(TobMutexGlobals.getLogicalclock());
		
		Message msg = new Message(MutexGlobals.id, null, MessageType.MUTEX_REQUEST, TobMutexGlobals.getLogicalclock());
		
		//Initiate sender thread to broadcast the mutex request message
		LamportsSender lamportSender = new LamportsSender(msg,null);
		Thread thread = new Thread(lamportSender);
        thread.start();

        while(!TobMutexGlobals.isReqGranted()) {
            // wait
        }
	}
	public void csLeave()
	{
		MutexPriorityQueueElement qElement = new MutexPriorityQueueElement(MutexGlobals.id, TobMutexGlobals.getLogicalclock());
		MutexGlobals.removeNodeFromPriorityQueue(qElement);
		
		Message msg = new Message(MutexGlobals.id, null, MessageType.CS_LEAVE, TobMutexGlobals.getLogicalclock());
		
		//Initiate sender thread to broadcast the mutex leave message
		LamportsSender lamportSender = new LamportsSender(msg,null);
		Thread thread = new Thread(lamportSender);
        thread.start();
	}
	
}
