package com.ketan.aos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class TcpReceiver implements Runnable {

    /**
     * TODO: Use it to gain control over the receiver thread from the main thread
     */
    public volatile boolean isRunning = true;

    private final int listenerPort;
    private final int label;
    private final HashMap<Integer, NodeInfo> nodeMap;
    private final int numOfTokens;

    public TcpReceiver(
            final int port,
            final int labelValue,
            final HashMap<Integer, NodeInfo> map,
            final int tokens) {

        listenerPort = port;
        label = labelValue;
        nodeMap = map;
        numOfTokens = tokens;
    }

    @Override
    public void run() {

        int tokensReturned = 0;

        try (
                ServerSocket listenerSocket = new ServerSocket(listenerPort)
            ) {

            while (isRunning) {

                try (
                        Socket connectionSocket = listenerSocket.accept()
                    ){
                    ObjectInputStream inputStream = new ObjectInputStream(
                            connectionSocket.getInputStream());
                    Token token = (Token) inputStream.readObject();

                    // Check if the currently received token has completed its path
                    if (token.isConsumed()) {
                        tokensReturned++;
                        System.out.println("Received token " + token.getId()
                                + "\tToken sum = " + token.getSum());
                        if (tokensReturned == numOfTokens) {
                            System.out.println("All tokens received");
                        }
                        continue;
                    }

                    // Add the label value to the token
                    token.addToSum(label);
                    // Get next node from the token path
                    int nextNodeId = token.getNextPathNode();
                    NodeInfo nextNdeInfo = nodeMap.get(nextNodeId);
                    // Send the token to the destined node
                    TcpSender tokenSender = new TcpSender(
                            nextNdeInfo.getHostName(),
                            nextNdeInfo.getPortNumber(), token);
                    new Thread(tokenSender).start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
