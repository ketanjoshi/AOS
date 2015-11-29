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
				else if (msgType.equals(MessageType.CS_ENTER))
				{
					handleCSEnterMessages();
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
			
			// reply to the node with the current logical clock
			//Initiate sender thread to send reply msg to this node
			//////////////////need to put the current logical clock of the node last
			Message msg = new Message(MutexGlobals.id, null, MessageType.MUTEX_REPLY,rcvdMessage.getLogicalClock());
				
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
		if (logicalClock <= MutexGlobals.getMutexReqClock())
		{
			MutexGlobals.incrementMutexReplyCount();
		}

		// check L1 and L2 of Lamport Mutex protocol
		if (MutexGlobals.getMutexRepliesRecvCounter() == (MutexGlobals.numNodes -1))
		{
			MutexPriorityQueueElement nodeElement = MutexGlobals.getMutexReqQueue().peek();
			if (nodeElement.getNodeId() == MutexGlobals.id) 
			{
				// node is itself on the top

			}
		}
	}
	
	private static void handleCSEnterMessages()
	{

	}
}
