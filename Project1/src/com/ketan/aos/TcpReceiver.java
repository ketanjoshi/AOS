package com.ketan.aos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class TcpReceiver implements Runnable {

    private final int listenerPort;
    private final int label;
    private final HashMap<Integer, NodeInfo> nodeMap;

    public TcpReceiver(
            final int port,
            final int labelValue,
            final HashMap<Integer, NodeInfo> map) {

        listenerPort = port;
        label = labelValue;
        nodeMap = map;
    }

    @Override
    public void run() {

        ServerSocket listenerSocket = null;
        try {
            listenerSocket = new ServerSocket(listenerPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true) {
            ObjectInputStream inputStream = null;

            try {
                Socket connectionSocket = listenerSocket.accept();
//                System.out.println("Connection accepted");
                inputStream = new ObjectInputStream(connectionSocket.getInputStream());
                Token token = (Token) inputStream.readObject();
                System.out.println("Token received : " + token);

                token.addToSum(label);

                if(token.isConsumed()) {
                    System.out.println("Token completed the path traversal. Sum : " + token.getSum());
                    continue;
                }

                int nextNodeId = token.getNextPathNode();
                NodeInfo nextNdeInfo = nodeMap.get(nextNodeId);
                TcpSender tokenSender = new TcpSender(nextNdeInfo.getHostName(),
                        nextNdeInfo.getPortNumber(),
                        token);
                new Thread(tokenSender).start();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
