import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;


public class MutexGlobals 
{
    public static PriorityQueue<MutexPriorityQueueElement> mutexReqQueue = new PriorityQueue<MutexPriorityQueueElement>();

	public static int id;
    protected static HashMap<Integer, Socket> socketMap = new HashMap<>();
    protected static HashMap<Integer, ObjectInputStream> readerStreamMap = new HashMap<>();
    protected static HashMap<Integer, ObjectOutputStream> writerStreamMap = new HashMap<>();
    public static int numNodes;
    public static HashSet<Integer> mutexRepliesRecvCounter = new HashSet<>(); 
    public static Integer mutexReqClock = -1;
    
	public static HashMap<Integer, ObjectInputStream> getReaderStreamMap() {
		return readerStreamMap;
	}

	public static void setReaderStreamMap(
			HashMap<Integer, ObjectInputStream> readerStreamMap) {
		MutexGlobals.readerStreamMap = readerStreamMap;
	}

	public static HashMap<Integer, ObjectOutputStream> getWriterStreamMap() {
		return writerStreamMap;
	}

	public static void setWriterStreamMap(
			HashMap<Integer, ObjectOutputStream> writerStreamMap) {
		MutexGlobals.writerStreamMap = writerStreamMap;
	}

	public static int getSocketMapSize() {
        return socketMap.size();
    }

    public static synchronized void addSocketEntry(int index, Socket socket) {
        socketMap.put(index, socket);
    }

    public static boolean hasSocketEntry(int index) {
        return socketMap.containsKey(index);
    }

    public static synchronized void addInputStreamEntry(int index, ObjectInputStream stream) {
        readerStreamMap.put(index, stream);
    }

    public static ObjectInputStream getReaderStream(int index) {
        return readerStreamMap.get(index);
    }

    public static synchronized void addOutputStreamEntry(int index, ObjectOutputStream stream) {
        writerStreamMap.put(index, stream);
    }

    public static ObjectOutputStream getWriterStream(int index) {
        return writerStreamMap.get(index);
    }

	public static PriorityQueue<MutexPriorityQueueElement> getMutexReqQueue() {
		return mutexReqQueue;
	}

	synchronized public static void addNodeToPriorityQueue(MutexPriorityQueueElement element)
	{
		mutexReqQueue.add(element);
	}
	
	public static void removeNodeFromPriorityQueue(MutexPriorityQueueElement element)
	{
		mutexReqQueue.remove(element);
	}

	public static int getNumNodes() {
		return numNodes;
	}

	public static void setNumNodes(int numNodes) {
		MutexGlobals.numNodes = numNodes;
	}
	
}
