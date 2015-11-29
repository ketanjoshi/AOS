import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;


public class LamportsSender implements Runnable{
	public static volatile boolean isRunning = true;
	//private ClusterNode node;
	private Message msg;
	ObjectOutputStream outputStream;

	public LamportsSender( Message msg, ObjectOutputStream outputStream) {
		this.msg = msg;
		this.outputStream = outputStream;
	}
	
	@Override
	public void run() {
		while(isRunning)
		{
			if (msg.getMessageType().equals(MessageType.MUTEX_REQUEST) || msg.getMessageType().equals(MessageType.CS_LEAVE))
			{
				broadcastMutexMessages();
			}
			else if (msg.getMessageType().equals(MessageType.CS_ENTER))
			{
				// Inform TOB layer that it has permission for Critical section
			}
			else if (msg.getMessageType().equals(MessageType.MUTEX_REPLY))
			{
				// send reply to the node id (whose request message is received)
				sendReplyMutexMessage();
			}
		}
	}
	
	private void broadcastMutexMessages()
	{
		HashMap <Integer, ObjectOutputStream> writerStreamMap = MutexGlobals.getWriterStreamMap();

		for (Integer nodeId:writerStreamMap.keySet()) 
		{
			if (nodeId != msg.getId() ) 
			{
				ObjectOutputStream outputStream = writerStreamMap.get(nodeId);
				try {
					synchronized (outputStream) {
						outputStream.writeObject(msg);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void sendReplyMutexMessage() 
	{
		try {
			synchronized (outputStream) {
				outputStream.writeObject(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
