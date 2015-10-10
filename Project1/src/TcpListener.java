import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Listener thread for connection requests from other nodes.
 * It accepts a connection (if not already established) and adds it into global connection maps
 * @author ketan
 */
public class TcpListener implements Runnable {

    public volatile boolean isRunning = true;
    private final int id;
    private final ServerSocket listenerSocket;
    private final HashMap<Integer, NodeInfo> nodeMap;

    private int connector;
    
    public TcpListener(
            final int id,
            final ServerSocket listenerSocket,
            final HashMap<Integer, NodeInfo> nodeMap
            ) {

        this.id = id;
        this.listenerSocket = listenerSocket;
        this.nodeMap = nodeMap;
    }

    @Override
    public void run() {

        int numOfPeers = nodeMap.size();
        Socket connectionSocket = null;

        try {

            while (Globals.getSocketMapSize() < numOfPeers) {

                try {
                    connectionSocket = listenerSocket.accept();

                    ObjectInputStream ois = new ObjectInputStream(connectionSocket.getInputStream());
                    byte[] buff = new byte[4];
                    ois.read(buff, 0, 4);
                    ByteBuffer bytebuff = ByteBuffer.wrap(buff);
                    int nodeId = bytebuff.getInt();
                    connector = nodeId;
//                    Globals.log("Connected : " + nodeId);
                    if(nodeId == id) {
                        Globals.addInputStreamEntry(nodeId, ois);
                        continue;
                    }

                    synchronized (Globals.socketMap) {
//                        if(Globals.socketMap.containsKey(nodeId)) {
//                            connectionSocket.close();
//                            continue;
//                        } else {
                            Globals.socketMap.put(nodeId, connectionSocket);
//                        }
                    }

                    Globals.addInputStreamEntry(nodeId, ois);
                    Globals.addOutputStreamEntry(nodeId, new ObjectOutputStream(connectionSocket.getOutputStream()));

                } catch (IOException e) {
//                    Globals.log(connector + " - Listener : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
//            Globals.log(connector + " - Listener : " + e.getMessage());
            e.printStackTrace();
        }
    }

}
