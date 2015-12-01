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

    private static final int PEER_SIZE = MutexGlobals.numNodes;
    public volatile boolean isRunning = true;
    private ServerSocket listenerSocket;

    private int connector;

    public MutexListener(final ServerSocket listenerSocket) {

        this.listenerSocket = listenerSocket;
    }

    @Override
    public void run() {

        System.out.println("MutexListener start");
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
                    if(nodeId == MutexGlobals.id) {
                        MutexGlobals.addInputStreamEntry(nodeId, ois);
                        continue;
                    }
                    System.out.println("Mutex Connected : " + nodeId);

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
//            try {
//                System.out.println("Closing listener");
//                listenerSocket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            System.out.println("Finally MutexListener");
        }
    }

}
