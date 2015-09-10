package com.ketan.aos;

import java.util.HashMap;

public class Node {

    private final int id;
    private final int labelValue;
    private final NodeInfo nodeInfo;
    private final HashMap<Integer, NodeInfo> nodeMap;

    private Token token;

    public Node(final int id) {
        this.id = id;
        AppConfigurations.setupApplicationEnvironment();
        this.nodeMap = AppConfigurations.getNodeMap();
        this.nodeInfo = nodeMap.get(id);
        this.token = AppConfigurations.getTokenMap().get(id);
        this.labelValue = id;//AppConfigurations.generateRandomNumber();
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public int getLabelValue() {
        return labelValue;
    }

    public Token getToken() {
        return token;
    }

    public HashMap<Integer, NodeInfo> getNodeMap() {
        return nodeMap;
    }

    public NodeInfo getNodeInfoFromIndex(int index) {
        return nodeMap.get(index);
    }

    @Override
    public String toString() {
        return " Id: " + id
                + " Label: " + labelValue
                + nodeInfo.toString();
    }

    public static void main(String[] args) throws InterruptedException {

        int id = Integer.parseInt(args[0]);
        Node node = new Node(id);

        TcpReceiver tokenReceiver = new TcpReceiver(
                node.getNodeInfo().getPortNumber(),
                node.getLabelValue(),
                node.getNodeMap());
        Thread receiverThread = new Thread(tokenReceiver);
        receiverThread.start();

        // Wait for sometime so that all the nodes are initialized
        // and their receiver threads are up
        System.out.println("Node up and listening-> " + node);
        Thread.sleep(3000);

        NodeInfo info = node.getNodeInfoFromIndex(node.getToken().getNextPathNode());
        TcpSender tokenSender = new TcpSender(info.getHostName(),
                info.getPortNumber(),
                node.getToken());
        Thread senderThread = new Thread(tokenSender);
        senderThread.start();

//        senderThread.join();
        receiverThread.join();
    }
}
