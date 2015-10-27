import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;


public class ClusterNode {

    private static final long WAIT_TIME = 3000L;

    private int id;
    private boolean isInitiator;
    private NodeInfo nodeInfo;
    private HashMap<Integer, NodeInfo> nodeMap;
    private ServerSocket listenerSocket;
    private ArrayList<Integer> neighbors;

    public ClusterNode() {
    }

    public void initializeNode(String config, int nodeId) throws IOException {
        id = nodeId;
        isInitiator = nodeId == 0;
        nodeMap = AppConfigurations.getNodeMap();
        nodeInfo = nodeMap.get(nodeId);
        neighbors = AppConfigurations.getNeighborNodes();
        listenerSocket = new ServerSocket(nodeInfo.getPortNumber());
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
        Listener listener = new Listener(listenerSocket, neighbors);
        Thread listenerThread = new Thread(listener);
        listenerThread.start();

        /**
         *  Wait for sometime so that all the nodes are initialized
         *  and their listener threads are up
         */
        Thread.sleep(WAIT_TIME);

        for (Integer nodeId : neighbors) {

            if (nodeId <= id || NetworkComponents.hasSocketEntry(nodeId)) {
                continue;
            }

            addToNetworkComponents(nodeId);
        }

        while (NetworkComponents.getSocketMapSize() < neighbors.size()) {
            System.out.println(NetworkComponents.getSocketMapSize() + " Waiting... " + neighbors.size());
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
                + "Neighbors : " + neighbors
                + "\nNodeMap : " + nodeMap;
    }

    /**
     * Launches and returns thread handle to message receiver thread
     * @return {@link Thread}
     * @throws InterruptedException
     */
    private ArrayList<Thread> launchReceiverThreads() throws InterruptedException {
        // Launch receiver threads
        ArrayList<Thread> receiverThreadPool = new ArrayList<>();
        for (Integer neighborId : neighbors) {
            ObjectInputStream stream = NetworkComponents.getReaderStream(neighborId);
            MessageReceiver receiver = new MessageReceiver(stream, neighbors);
            Thread thread = new Thread(receiver);
            thread.start();
            receiverThreadPool.add(thread);
        }
        Thread.sleep(WAIT_TIME);
        return receiverThreadPool;

    }

    /**
     * Launches and returns thread handle to message sender thread
     * @return {@link Thread}
     */
    private Thread launchSenderThread() {
        // Launch sender thread
        MessageSender sender = new MessageSender();
        Thread thread = new Thread(sender);
        thread.start();
        return thread;
    }

    public static void main(String[] args) {

        int id = Integer.parseInt(args[0]);
        String configFileName = args[1];

        Globals.initialiseLogger(AppConfigurations.getLogFileName(id, configFileName));
        AppConfigurations.setupApplicationEnvironment(configFileName, id);

        ClusterNode cNode = new ClusterNode();
        try {
            cNode.initializeNode(configFileName, id);
            Globals.log(cNode.toString());

            cNode.establishConnections();
            ArrayList<Thread> receiverThreads = cNode.launchReceiverThreads();
            Thread senderThread = cNode.launchSenderThread();

            Globals.setNodeActive(id % 2 == 0);
            Globals.log("Active - " + Globals.isNodeActive());

            Thread snapshotThread = cNode.launchSnapshotInitiator();

            senderThread.join();
            for (Thread thread : receiverThreads) {
                thread.join();
            }
            if(snapshotThread != null) {
                snapshotThread.join();
            }

        } catch (IOException e) {
            System.err.println("Exception thrown during node initialization. Cannot proceed.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        System.exit(0);
    }

    /**
     * Launches and returns thread handle to snapshot capturing thread
     * @return {@link Thread}
     */
    private Thread launchSnapshotInitiator() {
        if (!isInitiator) {
            return null;
        }
        SnapshotInitiator snapshotInitiator = new SnapshotInitiator(neighbors);
        Thread thread = new Thread(snapshotInitiator);
        thread.start();
        return thread;
    }

}
