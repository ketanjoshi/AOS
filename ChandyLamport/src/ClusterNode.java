import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;


public class ClusterNode {

    public static int sentMessageCount = 0;

    private int id;
    private boolean isInitiator;
    private NodeInfo nodeInfo;
    private HashMap<Integer, NodeInfo> nodeMap;
    private ServerSocket listenerScoket;
    private ArrayList<Integer> neighbors;

    public ClusterNode() {
    }

    public void initializeNode(String config, int nodeId) throws IOException {
        AppConfigurations.setupApplicationEnvironment(config, nodeId);
        id = nodeId;
        isInitiator = nodeId == 0;
        nodeMap = AppConfigurations.getNodeMap();
        nodeInfo = nodeMap.get(nodeId);
        neighbors = AppConfigurations.getNeighborNodes();
        listenerScoket = new ServerSocket(nodeInfo.getPortNumber());
    }

    public void establishConnections() {
        // Launch listener thread
        // Connect to all the neighbors with nodeId > own id

    }

    public static void main(String[] args) {

        ClusterNode cNode = new ClusterNode();
        try {
            cNode.initializeNode("./config.txt", 0);
            cNode.establishConnections();
        } catch (IOException e) {
            System.err.println("Exception thrown during node initialization. Cannot proceed.");
            e.printStackTrace();
            System.exit(0);
        }

    }

}
