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
    private ServerSocket listenerSocket;
    private int logicalClock;

    public ClusterNode() {
    }


    public void initializeNode(String config, int nodeId, int logicalClock) throws IOException {
        id = nodeId;
        nodeMap = AppConfigurations.getNodeMap();
        nodeInfo = nodeMap.get(nodeId);
        listenerSocket = new ServerSocket(nodeInfo.getPortNumber());
        this.logicalClock = logicalClock;
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


	public ServerSocket getListenerSocket() {
		return listenerSocket;
	}


	public void setListenerSocket(ServerSocket listenerSocket) {
		this.listenerSocket = listenerSocket;
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
    public void establishConnections() throws InterruptedException, IOException {
        // Launch listener thread
        // Connect to all the neighbors with nodeId > own id
        Listener listener = new Listener(listenerSocket);
        Thread listenerThread = new Thread(listener);
        listenerThread.start();

        /**
         *  Wait for sometime so that all the nodes are initialized
         *  and their listener threads are up
         */
        Thread.sleep(WAIT_TIME);

        for (int i = 0; i < Globals.numNodes; i++) {

            if (i < id || NetworkComponents.hasSocketEntry(i)) {
                continue;
            }

            addToNetworkComponents(i);
        }

        while (NetworkComponents.getSocketMapSize() < Globals.numNodes) {
            System.out.println(NetworkComponents.getSocketMapSize() + " Waiting... " + Globals.numNodes);
            Thread.sleep(WAIT_TIME);
        }

        listenerThread.interrupt();
    }

    /**
     * Requests socket connection to the input neighbors and adds entries to global maps
     * @param nodeId - Neighbor node id to connect to
     * @throws UnknownHostException
     * @throws IOException
     */
    private void addToNetworkComponents(int nodeId) throws UnknownHostException, IOException {
        NodeInfo info = nodeMap.get(nodeId);

        Globals.log("Trying to connect " + nodeId + " - " + info);
        boolean connected = false;
        Socket sock = null;
        while (!connected) {
            try {
                sock = new Socket(info.getHostName(), info.getPortNumber());
                connected = true;
            } catch (ConnectException ce) {
                Globals.log("Consuming ConnectException... Retrying...");
            }
        }
        Globals.log("Connected successfully : " + nodeId);

        NetworkComponents.addSocketEntry(nodeId, sock);

        ByteBuffer dbuf = ByteBuffer.allocate(4);
        dbuf.putInt(id);
        ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
        byte[] bytes = dbuf.array();
        oos.write(bytes);
        oos.flush();
        oos.reset();
        NetworkComponents.addOutputStreamEntry(nodeId, oos);
        NetworkComponents.addInputStreamEntry(nodeId, new ObjectInputStream(sock.getInputStream()));
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

        Globals.initialiseLogger(AppConfigurations.getLogFileName(id, configFileName));
        AppConfigurations.setupApplicationEnvironment(configFileName, id);

        ClusterNode cNode = new ClusterNode();
        try {
            cNode.initializeNode(configFileName, id, 0);
            Globals.log(cNode.toString());

            cNode.establishConnections();

        } catch (IOException e) {
            System.err.println("Exception thrown during node initialization. Cannot proceed.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        System.exit(0);
    }
}
