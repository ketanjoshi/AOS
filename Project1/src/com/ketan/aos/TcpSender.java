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

        try (
                Socket clientSocket = new Socket(receiverName, receiverPort);
                ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream())
            ){
            outputStream.writeObject(token);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
