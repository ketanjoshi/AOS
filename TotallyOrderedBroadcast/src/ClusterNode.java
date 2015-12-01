import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;


public class ClusterNode {

    private static final long WAIT_TIME = 3000L;
    private int id;
    private NodeInfo nodeInfo;
    private HashMap<Integer, NodeInfo> nodeMap;
    private int logicalClock;
    private ServerSocket listenerSocket;

    public ClusterNode() {
    }


    public void initializeNode(String config, int nodeId, int logicalClock) throws IOException {
        id = nodeId;
        nodeMap = AppConfigurations.getNodeMap();
        nodeInfo = nodeMap.get(nodeId);
        this.logicalClock = logicalClock;
        listenerSocket = new ServerSocket(nodeInfo.getPortNumber());
    }

    public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public HashMap<Integer, NodeInfo> getNodeMap() {
		return nodeMap;
	}


	public void setNodeMap(HashMap<Integer, NodeInfo> nodeMap) {
		this.nodeMap = nodeMap;
	}


	public int getLogicalClock() {
		return logicalClock;
	}


	public void setLogicalClock(int logicalClock) {
		this.logicalClock = logicalClock;
	}


	/**
     * Establish connection with the neighbors and adds entries for 
     * connection sockets, input streams and output streams in global maps
     * @throws InterruptedException
     * @throws IOException
     */
    public void establishTobConnections() throws InterruptedException, IOException {
        // Launch listener thread
        // Connect to all the neighbors with nodeId > own id
//        TobListener listener = new TobListener(listenerSocket);
//        Thread listenerThread = new Thread(listener);
//        listenerThread.start();

        /**
         *  Wait for sometime so that all the nodes are initialized
         *  and their listener threads are up
         */
        Thread.sleep(WAIT_TIME);

        for (int i = 0; i < TobGlobals.numNodes; i++) {

//            if (i < id || TobGlobals.hasSocketEntry(i)) {
            if (i < id) {
                continue;
            }
            addToTobNetworkComponents(i);
        }

        while (TobGlobals.getSocketMapSize() < TobGlobals.numNodes) {
            System.out.println(TobGlobals.getSocketMapSize() + " Waiting... " + TobGlobals.numNodes);
            Thread.sleep(WAIT_TIME);
        }
        System.out.println("**************** " + TobGlobals.getSocketMapSize());

//        listenerThread.interrupt();
    }

    public void establishMutexConnections() throws InterruptedException, IOException {
        // Launch listener thread
        // Connect to all the neighbors with nodeId > own id
//        MutexListener listener = new MutexListener(listenerSocket);
//        Thread listenerThread = new Thread(listener);
//        listenerThread.start();

        /**
         *  Wait for sometime so that all the nodes are initialized
         *  and their listener threads are up
         */
//        Thread.sleep(WAIT_TIME);

        for (int i = 0; i < MutexGlobals.numNodes; i++) {

//            if (i < id || MutexGlobals.hasSocketEntry(i)) {
            if (i < id) {
                continue;
            }
            addToMutexNetworkComponents(i);
        }

        while (MutexGlobals.getSocketMapSize() < MutexGlobals.numNodes) {
            System.out.println(MutexGlobals.getSocketMapSize() + " Waiting... " + MutexGlobals.numNodes);
            Thread.sleep(WAIT_TIME);
        }
        System.out.println("**************** " + MutexGlobals.getSocketMapSize());

//        listenerThread.interrupt();
    }

    /**
     * Requests socket connection to the input neighbors and adds entries to global maps
     * @param nodeId - Neighbor node id to connect to
     * @throws UnknownHostException
     * @throws IOException
     */
    private void addToTobNetworkComponents(int nodeId) throws UnknownHostException, IOException {
        NodeInfo info = nodeMap.get(nodeId);

        TobGlobals.log("Trying to connect " + nodeId + " - " + info);
        boolean connected = false;
        Socket sock = null;
        while (!connected) {
            try {
                sock = new Socket(info.getHostName(), info.getPortNumber());
                connected = true;
            } catch (ConnectException ce) {
                TobGlobals.log("Consuming ConnectException... Retrying...");
            }
        }
        System.out.println("Tob Connected successfully : " + nodeId);

       	TobGlobals.addSocketEntry(nodeId, sock);

       	// Write id in first 4 bytes
       	ByteBuffer dbuf = ByteBuffer.allocate(4);
        dbuf.putInt(id);
        byte[] idBytes = dbuf.array();
        // Write connection type: 0 - Tob, 1- Mutex
        dbuf = ByteBuffer.allocate(4);
        dbuf.putInt(0);
        byte[] tobBytes = dbuf.array();

        // First 4 bytes = idBytes, Last 4 bytes = tobBytes
        byte[] sendBytes = new byte[8];
        System.arraycopy(idBytes, 0, sendBytes, 0, 4);
        System.arraycopy(tobBytes, 0, sendBytes, 4, 4);

        ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
        oos.write(sendBytes);
        oos.flush();
        oos.reset();
       	TobGlobals.addOutputStreamEntry(nodeId, oos);
       	if(nodeId == id) {
            return;
        }
       	TobGlobals.addInputStreamEntry(nodeId, new ObjectInputStream(sock.getInputStream()));
    }
    private void addToMutexNetworkComponents(int nodeId) throws UnknownHostException, IOException {
        NodeInfo info = nodeMap.get(nodeId);

       // MutexGlobals.log("Trying to connect " + nodeId + " - " + info);
        boolean connected = false;
        Socket sock = null;
        while (!connected) {
            try {
                sock = new Socket(info.getHostName(), info.getPortNumber());
                connected = true;
            } catch (ConnectException ce) {
         //   	MutexGlobals.log("Consuming ConnectException... Retrying...");
            }
        }
        System.out.println("Mutex Connected successfully : " + nodeId);
       
        MutexGlobals.addSocketEntry(nodeId, sock);

        // Write id in first 4 bytes
        ByteBuffer dbuf = ByteBuffer.allocate(4);
        dbuf.putInt(id);
        byte[] idBytes = dbuf.array();
        // Write connection type: 0 - Tob, 1- Mutex
        dbuf = ByteBuffer.allocate(4);
        dbuf.putInt(1);
        byte[] mutBytes = dbuf.array();

        // First 4 bytes = idBytes, Last 4 bytes = mutBytes
        byte[] sendBytes = new byte[8];
        System.arraycopy(idBytes, 0, sendBytes, 0, 4);
        System.arraycopy(mutBytes, 0, sendBytes, 4, 4);

        ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
        oos.write(sendBytes);
        oos.flush();
        oos.reset();
        MutexGlobals.addOutputStreamEntry(nodeId, oos);
        if(nodeId == id) {
            return;
        }
        MutexGlobals.addInputStreamEntry(nodeId, new ObjectInputStream(sock.getInputStream()));
    }

    @Override
    public String toString() {
        return "NodeId : " + id
                + "\nNodeInfo : " + nodeInfo
                + "\nNodeMap : " + nodeMap;
    }

    public static void main(String[] args) {

        int id = Integer.parseInt(args[0]);
        String configFileName = args[1];

        TobGlobals.initialiseLogger(AppConfigurations.getLogFileName(id, configFileName));
        AppConfigurations.setupApplicationEnvironment(configFileName, id);

        ClusterNode cNode = new ClusterNode();
        try {
            cNode.initializeNode(configFileName, id, 0);
            TobGlobals.log(cNode.toString());

            CommonListener listener = new CommonListener(cNode.getListenerSocket());
            Thread listenerThread = new Thread(listener);
            listenerThread.start();
            Thread.sleep(WAIT_TIME);

            cNode.establishTobConnections();
//            System.out.println(TobGlobals.readerStreamMap);
//            System.out.println(TobGlobals.writerStreamMap);


//            Thread.sleep(WAIT_TIME);

            cNode.establishMutexConnections();
//            System.out.println(MutexGlobals.readerStreamMap);
//            System.out.println(MutexGlobals.writerStreamMap);

            listenerThread.interrupt();
            Thread.sleep(WAIT_TIME);

            // Start application layer
            ApplicationLayer appLayer = ApplicationLayer.getInstance();
            appLayer.initialise();
            Thread appThread = new Thread(appLayer);
            appThread.start();
            appThread.join();

        } catch (IOException e) {
            System.err.println("Exception thrown during node initialization. Cannot proceed.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        System.exit(0);
    }


    public ServerSocket getListenerSocket() {
        return listenerSocket;
    }

}
