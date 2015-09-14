package com.ketan.aos;

import java.util.HashMap;
import java.util.Random;

public class Node {

    private static final long WAIT_TIME = 3000L;
    private static final Random INTEGER_GENERATOR = new Random();
    private static final int LABEL_UPPER_BOUND = 10;

    private final int id;
    private final int labelValue;
    private final NodeInfo nodeInfo;
    private final HashMap<Integer, NodeInfo> nodeMap;

    private Tokens tokens;

    public Node(final int id) {
        this.id = id;
        AppConfigurations.setupApplicationEnvironment();
        this.nodeMap = AppConfigurations.getNodeMap();
        this.nodeInfo = nodeMap.get(id);
        this.tokens = AppConfigurations.getTokenMap().get(id);
        this.labelValue = Node.generateRandomNumber();
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

    public NodeInfo getNodeInfoFromIndex(int index) {
        return nodeMap.get(index);
    }

    public static int generateRandomNumber() {
        return INTEGER_GENERATOR.nextInt(LABEL_UPPER_BOUND);
    }

    public void initializeTokens() {
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

    public static void main(String[] args) throws InterruptedException {

        if(args.length == 0) {
            System.out.println("Usage : Argument required - 'Id'\ne.g.: java com.ketan.aos.Node 2");
            System.exit(0);
        }

        int id = Integer.parseInt(args[0]);
        Node node = new Node(id);

        System.out.println("Net ID: " + AppConfigurations.getNetId());
        System.out.println("Node ID: " + node.getId());

        TcpReceiver tokenReceiver = new TcpReceiver(
                node.getNodeInfo().getPortNumber(),
                node.getLabelValue(),
                node.getNodeMap(),
                node.getTokens().size());
        Thread receiverThread = new Thread(tokenReceiver);
        receiverThread.start();

        /**
         *  Wait for sometime so that all the nodes are initialized
         *  and their receiver threads are up
         */
        System.out.println("Listening on " + node.getNodeInfo()
                + "Random number: " + node.getLabelValue());
        Thread.sleep(WAIT_TIME);

        for (Token token : node.getTokens().getTokenList()) {
            System.out.println("Emitting token " + token.getId() + " with path " + token.getPrintablePath());
            NodeInfo info = node.getNodeInfoFromIndex(token.getNextPathNode());
            TcpSender tokenSender = new TcpSender(
                    info.getHostName(),
                    info.getPortNumber(),
                    token);
            Thread senderThread = new Thread(tokenSender);
            senderThread.start();
        }

        receiverThread.join();
    }
}
