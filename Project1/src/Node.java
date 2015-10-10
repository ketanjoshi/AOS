import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

/**
 * Class to represent the machine or a node participating 
 * in the distributed system
 * Also a driver class
 * @author ketan
 */
public class Node {

    private static final long WAIT_TIME = 3000L;
    private static final Random INTEGER_GENERATOR = new Random();
    private static final int LABEL_UPPER_BOUND = 10000;

    private final int id;
    private final int labelValue;
    private final NodeInfo nodeInfo;
    private final HashMap<Integer, NodeInfo> nodeMap;
    private final ServerSocket listenerScoket;

    private Tokens tokens;

    public Node(final int id) throws IOException {
        this.id = id;
        this.nodeMap = AppConfigurations.getNodeMap();
        this.nodeInfo = nodeMap.get(id);
        this.tokens = AppConfigurations.getNodeTokens(id);
        this.labelValue = Node.generateRandomNumber();
        this.listenerScoket = new ServerSocket(nodeInfo.getPortNumber());
        initializeTokens();
    }

    public int getId() {
        return id;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public int getLabelValue() {
        return labelValue;
    }

    public Tokens getTokens() {
        return tokens;
    }

    public HashMap<Integer, NodeInfo> getNodeMap() {
        return nodeMap;
    }

    public ServerSocket getListenerScocket() {
        return listenerScoket;
    }

    public NodeInfo getNodeInfoFromIndex(int index) {
        return nodeMap.get(index);
    }

    public static int generateRandomNumber() {
        return INTEGER_GENERATOR.nextInt(LABEL_UPPER_BOUND);
    }

    public void initializeTokens() {
        if(tokens == null) {
            return;
        }

        int tokenId = 1;
        for (Token t : tokens.getTokenList()) {
            t.setId(tokenId++);
            t.addToSum(labelValue);
        }
    }

    @Override
    public String toString() {
        return " Id: " + id
                + " Label: " + labelValue
                + nodeInfo.toString();
    }

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {

        if(args.length < 2) {
            System.out.println("Usage : Insufficient number of arguments\ne.g.: java Node <nodeId> <configFilePath>");
            System.exit(0);
        }

        int id = Integer.parseInt(args[0]);
        String configFileName = args[1];

        AppConfigurations.setupApplicationEnvironment(configFileName);
        Node node = new Node(id);
        NodeInfo nodeInfo = node.getNodeInfo();
        Globals.initialiseLogger(AppConfigurations.getLogFileName(id, configFileName));

        String msg = "Net ID: " + AppConfigurations.getNetId()
                + "\nNode ID: " + node.getId()
                + "\nListening on " + nodeInfo.getHostName() + ":" + nodeInfo.getPortNumber()
                + "\nRandom number: " + node.getLabelValue();
        Globals.log(msg);

        HashMap<Integer, NodeInfo> nodeMap = node.getNodeMap();
        TcpListener tcpListener = new TcpListener(
                id,
                node.getListenerScocket(),
                nodeMap);
        Thread listenerThread = new Thread(tcpListener);
        listenerThread.start();

        /**
         *  Wait for sometime so that all the nodes are initialized
         *  and their listener threads are up
         */
        Thread.sleep(WAIT_TIME);

        for (Entry<Integer, NodeInfo> entry : nodeMap.entrySet()) {

            int nodeId = entry.getKey();
            if(nodeId < id) {
                continue;
            }
            if(Globals.hasSocketEntry(nodeId) && nodeId != id) {
                // Prune
                continue;
            }

            NodeInfo info = entry.getValue();

//            Globals.log("Trying to connect " + nodeId + " - " + info);
            boolean connected = false;
            Socket sock = null;
            while(!connected) {
                try {
            sock = new Socket(info.getHostName(), info.getPortNumber());
            connected = true;
                } catch (ConnectException ce) {
//                    Globals.log("Consuming ConnectException... Retrying...");
                }
            }

            synchronized (Globals.socketMap) {
//                if(Globals.socketMap.containsKey(nodeId)) {
//                    sock.close();
//                    continue;
//                } else {
                    Globals.socketMap.put(nodeId, sock);
//                }
            }
            ByteBuffer dbuf = ByteBuffer.allocate(4);
            dbuf.putInt(id);
            ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
            byte[] bytes = dbuf.array();
            oos.write(bytes);
            oos.flush();
            oos.reset();
            Globals.addOutputStreamEntry(nodeId, oos);
            if(nodeId == id) {
                continue;
            }
            Globals.addInputStreamEntry(nodeId, new ObjectInputStream(sock.getInputStream()));
        }

        while (Globals.getSocketMapSize() < nodeMap.size()) {
            Thread.sleep(WAIT_TIME);
        }
        listenerThread.interrupt();

//        Globals.log(Globals.socketMap.toString());
//        Globals.log(Globals.readerStreamMap.toString());
//        Globals.log(Globals.writerStreamMap.toString());

        // Launch separate receiver threads for each socket connection
        ArrayList<Thread> receiverThreadPool = new ArrayList<>();
        for (int nodeNum : nodeMap.keySet()) {
            int numTokens = -1;
            if(node.getTokens() != null) {
                numTokens = node.getTokens().size();
            }
            TcpReceiver tcpReceiver = new TcpReceiver(
                    Globals.getReaderStream(nodeNum),
                    numTokens,
                    node.getLabelValue());
            Thread receiverThread = new Thread(tcpReceiver);
            receiverThreadPool.add(receiverThread);
            receiverThread.start();
        }

        /**
         *  Wait so that receiver threads of all the nodes are up
         */
        Thread.sleep(WAIT_TIME);

        // Send node's tokens to desired nodes
        Tokens tokens = node.getTokens();
        if (tokens != null) {
            for (Token token : node.getTokens().getTokenList()) {
                Globals.log("Emitting token " + token.getId() + " with path "
                        + token.getPrintablePath());
                int nextNodeId = token.getNextPathNode();
                TcpSender tokenSender = new TcpSender(
                        Globals.getWriterStream(nextNodeId), token);
                Thread senderThread = new Thread(tokenSender);
                senderThread.start();
            }
        }

        // Wait on all the receiver threads
        for (Thread thread : receiverThreadPool) {
            thread.join();
        }
    }
}
