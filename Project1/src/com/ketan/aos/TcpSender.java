package com.ketan.aos;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class TcpSender implements Runnable {

    private final String receiverName;
    private final int receiverPort;
    private final Token token;

    public TcpSender(
            final String server,
            final int port,
            final Token payload) {

        receiverName = server;
        receiverPort = port;
        token = payload;
    }

    @Override
    public void run() {

        ObjectOutputStream outputStream = null;

        try {
            Socket clientSocket = new Socket(receiverName, receiverPort);
//            System.out.println("Connect successful");
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            outputStream.writeObject(token);
            System.out.println("Token sent : " + token + "\nReceiver : " + receiverName + ":" + receiverPort);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
