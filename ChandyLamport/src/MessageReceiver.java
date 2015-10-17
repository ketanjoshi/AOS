import java.io.ObjectInputStream;
import java.util.HashMap;

/**
 * Receiver thread for the node which receives tokens sent to a given input stream.
 * It processes the received tokens and forwards accordingly.
 * @author ketan
 */
public class MessageReceiver implements Runnable {

    private static final HashMap<Integer, ObjectInputStream> INPUTSTREAM_MAP = null;

    public volatile boolean isRunning = true;

    public MessageReceiver() {
    }

    @Override
    public void run() {

        while(isRunning) {

        }

    }

}
