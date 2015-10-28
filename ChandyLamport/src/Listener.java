import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Listener thread for connection requests from other nodes.
 * It accepts a connection (if not already established) and adds it into global connection maps
 * @author ketan
 */
public class Listener implements Runnable {

    public volatile boolean isRunning = true;
    private final ServerSocket listenerSocket;
    private final ArrayList<Integer> neighbors;

    private int connector;

    public Listener(
            final ServerSocket listenerSocket,
            final ArrayList<Integer> neighbors
            ) {

        this.listenerSocket = listenerSocket;
        this.neighbors = neighbors;
    }

    @Override
    public void run() {

        int numOfPeers = neighbors.size();
        Socket connectionSocket = null;

        try {

            while (NetworkComponents.getSocketMapSize() < numOfPeers) {

                try {
                    connectionSocket = listenerSocket.accept();

                    ObjectInputStream ois = new ObjectInputStream(connectionSocket.getInputStream());
                    byte[] buff = new byte[4];
                    ois.read(buff, 0, 4);
                    ByteBuffer bytebuff = ByteBuffer.wrap(buff);
                    int nodeId = bytebuff.getInt();
                    connector = nodeId;
//                    Globals.log("Connected : " + nodeId);

                    NetworkComponents.addSocketEntry(nodeId, connectionSocket);
                    NetworkComponents.addInputStreamEntry(nodeId, ois);
                    NetworkComponents.addOutputStreamEntry(nodeId, new ObjectOutputStream(connectionSocket.getOutputStream()));

                } catch (IOException e) {
//                    Globals.log(connector + " - Listener : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
//            Globals.log(connector + " - Listener : " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            try {
                listenerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
