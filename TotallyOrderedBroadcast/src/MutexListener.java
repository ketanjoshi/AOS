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
public class MutexListener implements Runnable {

    private static final int PEER_SIZE = MutexGlobals.numNodes - 1;
    public volatile boolean isRunning = true;
    private final ServerSocket listenerSocket;

    private int connector;

    public MutexListener(final ServerSocket listenerSocket) {

        this.listenerSocket = listenerSocket;
    }

    @Override
    public void run() {

        Socket connectionSocket = null;

        try {

            while (MutexGlobals.getSocketMapSize() < PEER_SIZE) {

                try {
                    connectionSocket = listenerSocket.accept();

                    ObjectInputStream ois = new ObjectInputStream(connectionSocket.getInputStream());
                    byte[] buff = new byte[4];
                    ois.read(buff, 0, 4);
                    ByteBuffer bytebuff = ByteBuffer.wrap(buff);
                    int nodeId = bytebuff.getInt();
                    connector = nodeId;
                    //MutexGlobals.log("Connected : " + nodeId);

                    MutexGlobals.addSocketEntry(nodeId, connectionSocket);
                    MutexGlobals.addInputStreamEntry(nodeId, ois);
                    MutexGlobals.addOutputStreamEntry(nodeId, new ObjectOutputStream(connectionSocket.getOutputStream()));

                } catch (IOException e) {
                	//MutexGlobals.log(connector + " - Listener : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
        	//MutexGlobals.log(connector + " - Listener : " + e.getMessage());
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
