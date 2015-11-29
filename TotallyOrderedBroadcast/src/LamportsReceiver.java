import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class LamportsReceiver implements Runnable{
	public static volatile boolean isRunning = true;
	private final ObjectInputStream inputStream;

	public LamportsReceiver(ObjectInputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public void run() {
		while(isRunning)
		{
			Message message;
			try {
				message = (Message) inputStream.readObject();

				MessageType msgType = message.getMessageType();
				if (msgType.equals(MessageType.MUTEX_REPLY)) 
				{
					handleReplyMutexMessages(message.getLogicalClock());
				}
				else if (msgType.equals(MessageType.MUTEX_REQUEST))
				{
					handleRequestMutexMessages();
				}
				else if (msgType.equals(MessageType.CS_LEAVE))
				{
					handleCSLeaveMessages();
				}
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void handleRequestMutexMessages()
	{
		Message rcvdMessage;

		try {
			rcvdMessage = (Message) inputStream.readObject();

			MutexPriorityQueueElement element = new MutexPriorityQueueElement(rcvdMessage.getId(), 
					rcvdMessage.getLogicalClock());
			
			// add node to the priority queue
			MutexGlobals.addNodeToPriorityQueue(element);

			// Take the maximum of the rcvd clock and own clock
			int rcvdLogicalClock = rcvdMessage.getLogicalClock();
			
			// Increment the clock value
			TobMutexGlobals.setLogicalclock(Math.max(TobMutexGlobals.getLogicalclock(),rcvdLogicalClock) + 1);
						
			// Reply to the node with the current logical clock
			Message msg = new Message(MutexGlobals.id, null, MessageType.MUTEX_REPLY,TobMutexGlobals.getLogicalclock());

			// Initiate sender thread to send reply msg to this node
			ObjectOutputStream outputStream = MutexGlobals.getWriterStream(rcvdMessage.getId());
			LamportsSender lamportSender = new LamportsSender(msg,outputStream);
			Thread thread = new Thread(lamportSender);
	        thread.start();
	        
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void handleReplyMutexMessages(int logicalClock)
	{
		// check L1 and L2 of Lamport Mutex protocol
		if (logicalClock >= MutexGlobals.getMutexReqClock())
		{
			MutexGlobals.incrementMutexReplyCount();
		}

		TobMutexGlobals.setReqGranted(checkConditionsForLamport());
	}

	/**
	 * 
	 */
	private static boolean checkConditionsForLamport() {
		if (MutexGlobals.getMutexRepliesRecvCounter() == (MutexGlobals.numNodes -1))
		{
			MutexPriorityQueueElement nodeElement = MutexGlobals.getMutexReqQueue().peek();
			if (nodeElement.getNodeId() == MutexGlobals.id) 
			{
				// node is itself on the top
				// L1 and L2 both are satisfied
				return true;
			}
		}
		return false;
	}
	
	private void handleCSLeaveMessages()
	{
		Message rcvdMessage;

		try {
			rcvdMessage = (Message) inputStream.readObject();

			MutexPriorityQueueElement element = new MutexPriorityQueueElement(rcvdMessage.getId(), 
					rcvdMessage.getLogicalClock());
			
			// delete node from the priority queue
			MutexGlobals.removeNodeFromPriorityQueue(element);

			// check L1 and L2 as node can be on the top
			TobMutexGlobals.setReqGranted(checkConditionsForLamport());
			
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
}
