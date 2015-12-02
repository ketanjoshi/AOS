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
				    System.out.println("Received reply from : " + message.getId());
					handleReplyMutexMessages(message.getLogicalClock());
				}
				else if (msgType.equals(MessageType.MUTEX_REQUEST))
				{
					handleRequestMutexMessages(message);
				}
				else if (msgType.equals(MessageType.CS_LEAVE))
				{
					handleCSLeaveMessages(message);
				}
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void handleRequestMutexMessages(Message rcvdMessage)
	{
	    System.out.println("Received request from  : " + rcvdMessage.getId());
	    synchronized (MutexGlobals.mutexRepliesRecvCounter) {
	    	  synchronized (MutexGlobals.mutexReqClock) {
		        if (MutexGlobals.mutexReqClock != -1 && rcvdMessage.getLogicalClock() >= MutexGlobals.mutexReqClock) {
		            MutexGlobals.mutexRepliesRecvCounter++;
		            System.out.println("********Counter : " + MutexGlobals.mutexRepliesRecvCounter);
		        }
	    	  }
	    }
        MutexPriorityQueueElement element = new MutexPriorityQueueElement(
                rcvdMessage.getId(), rcvdMessage.getLogicalClock());

        // add node to the priority queue
        synchronized (MutexGlobals.mutexReqQueue) {
            MutexGlobals.mutexReqQueue.add(element);
        }

        // Take the maximum of the rcvd clock and own clock
        int rcvdLogicalClock = rcvdMessage.getLogicalClock();

        // Increment the clock value
        synchronized (TobMutexGlobals.logicalclock) {
            TobMutexGlobals.logicalclock = Math.max(TobMutexGlobals.logicalclock, rcvdLogicalClock) + 1;
        }

        // Reply to the node with the current logical clock
        Message msg = new Message(MutexGlobals.id, null,
                MessageType.MUTEX_REPLY, TobMutexGlobals.getLogicalclock());

        // Initiate sender thread to send reply msg to this node
        ObjectOutputStream outputStream = MutexGlobals.getWriterStream(rcvdMessage.getId());
        LamportsSender lamportSender = new LamportsSender(msg, outputStream);
        System.out.println("Replying to : " + rcvdMessage.getId());
        Thread thread = new Thread(lamportSender);
        thread.start();

	}
	
	private static void handleReplyMutexMessages(int logicalClock)
	{
	    synchronized (MutexGlobals.mutexRepliesRecvCounter) {
	    	  synchronized (MutexGlobals.mutexReqClock) {
		        if (MutexGlobals.mutexReqClock != -1 && logicalClock >= MutexGlobals.mutexReqClock) {
		            MutexGlobals.mutexRepliesRecvCounter++;
		            System.out.println("********Counter : " + MutexGlobals.mutexRepliesRecvCounter);
		        }
	    	  }
	        if (MutexGlobals.mutexRepliesRecvCounter == (MutexGlobals.numNodes - 1)) {
                synchronized (MutexGlobals.mutexReqQueue) {
                    MutexPriorityQueueElement nodeElement = MutexGlobals.mutexReqQueue.peek();
                    if(nodeElement != null) {
                        System.out.println("********Top element : " + nodeElement.getNodeId());
                    }
                    if (nodeElement != null && nodeElement.getNodeId() == MutexGlobals.id) {
                        // node is itself on the top
                        // L1 and L2 both are satisfied
                        synchronized (TobMutexGlobals.reqGranted) {
                            TobMutexGlobals.reqGranted = true;
                        }
                        synchronized (MutexGlobals.mutexRepliesRecvCounter) {
                            MutexGlobals.mutexRepliesRecvCounter = 0;
                        }
                        synchronized (MutexGlobals.mutexReqClock) {
                            MutexGlobals.mutexReqClock = -1;
                        }
                        System.out.println("Request granted");
                    }
                }
            }
        }
	}

	
    private void handleCSLeaveMessages(Message rcvdMessage) {
    	synchronized (MutexGlobals.mutexRepliesRecvCounter) {
    		  synchronized (MutexGlobals.mutexReqClock) {
		        if (MutexGlobals.mutexReqClock != -1 && rcvdMessage.getLogicalClock() >= MutexGlobals.mutexReqClock) {
		            MutexGlobals.mutexRepliesRecvCounter++;
		            System.out.println("********Counter : " + MutexGlobals.mutexRepliesRecvCounter);
		        }
    		  }
	    }
        synchronized (MutexGlobals.mutexReqQueue) {
            MutexGlobals.mutexReqQueue.poll();
            MutexPriorityQueueElement nodeElement = MutexGlobals.mutexReqQueue.peek();
            if (nodeElement != null
                    && nodeElement.getNodeId() == MutexGlobals.id) {
                // node is itself on the top
                // L1 and L2 both are satisfied
                synchronized (MutexGlobals.mutexRepliesRecvCounter) {
                    if (MutexGlobals.mutexRepliesRecvCounter == (MutexGlobals.numNodes - 1)) {
                        MutexGlobals.mutexRepliesRecvCounter = 0;
                        synchronized (MutexGlobals.mutexReqClock) {
                            MutexGlobals.mutexReqClock = -1;
                        }
                        synchronized (TobMutexGlobals.reqGranted) {
                            TobMutexGlobals.reqGranted = true;
                        }
                    }
                }
            }
        }

    }
}
