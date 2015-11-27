import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Listener thread for connection requests from other nodes.
 * It accepts a connection (if not already established) and adds it into global connection maps
 * @author ketan
 */
public class TobListener implements Runnable {

    private static final int PEER_SIZE = TobGlobals.numNodes;
    public volatile boolean isRunning = true;
    private ServerSocket listenerSocket;

    private int connector;

    public TobListener(final ServerSocket listenerSocket) {

        this.listenerSocket = listenerSocket;

    }

    @Override
    public void run() {

        Socket connectionSocket = null;

        try {

            while (TobGlobals.getSocketMapSize() < PEER_SIZE) {

                try {
                    connectionSocket = listenerSocket.accept();

                    ObjectInputStream ois = new ObjectInputStream(connectionSocket.getInputStream());
                    byte[] buff = new byte[4];
                    ois.read(buff, 0, 4);
                    ByteBuffer bytebuff = ByteBuffer.wrap(buff);
                    int nodeId = bytebuff.getInt();
                    connector = nodeId;
                    if(nodeId == TobGlobals.id) {
                        TobGlobals.addInputStreamEntry(nodeId, ois);
                        continue;
                    }
                    TobGlobals.log("Connected : " + nodeId);

                    TobGlobals.addSocketEntry(nodeId, connectionSocket);
                    TobGlobals.addInputStreamEntry(nodeId, ois);
                    TobGlobals.addOutputStreamEntry(nodeId, new ObjectOutputStream(connectionSocket.getOutputStream()));

                } catch (IOException e) {
                    TobGlobals.log(connector + " - Listener : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            TobGlobals.log(connector + " - Listener : " + e.getMessage());
            e.printStackTrace();
        }
    }

}
