import java.io.IOException;
import java.io.ObjectOutputStream;


/**
 * Thread to send given snapshot message to a given neighbor
 * @author ketan
 */
public class SnapshotSender implements Runnable {

    private final int nodeId;
    private final Message message;

    public SnapshotSender(final int id,
            final Message msg) {
        nodeId = id;
        message = msg;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream outputStream = NetworkComponents.getWriterStream(nodeId);
            synchronized (outputStream) {
                outputStream.writeObject(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
