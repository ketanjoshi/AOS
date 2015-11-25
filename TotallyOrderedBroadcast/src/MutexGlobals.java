import java.util.PriorityQueue;


public class MutexGlobals 
{
	private static PriorityQueue<MutexPriorityQueueElement> mutexReqQueue = new PriorityQueue<MutexPriorityQueueElement>();

	public static PriorityQueue<MutexPriorityQueueElement> getMutexReqQueue() {
		return mutexReqQueue;
	}

	public static void addNodeToPriorityQueue(MutexPriorityQueueElement element)
	{
		mutexReqQueue.add(element);
	}
	
	public static void removeNodeFromPriorityQueue(MutexPriorityQueueElement element)
	{
		mutexReqQueue.remove(element);
	}
}
