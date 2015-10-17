import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Sender thread to send a token on a given output stream.
 * @author ketan
 */
public class MessageSender implements Runnable {

    private static final ArrayList<Integer> NEIGHBORS = AppConfigurations.getNeighborNodes();
    private static final int CLUSTER_SIZE = Globals.clusterSize;
    private static final int MIN_PER_ACTIVE = Globals.minPerActive;
    private static final int MAX_PER_ACTIVE = Globals.maxPerActive;
    private static final int DIFF = MAX_PER_ACTIVE - MIN_PER_ACTIVE;
    private static final long MIN_SEND_DELAY = Globals.minSendDelay;
    private static final HashMap<Integer, ObjectOutputStream> OUTPUTSTREAM_MAP = null;
    private static final Random RANDOM_GENERATOR = Globals.RANDOM;

    public static volatile boolean isRunning = true;


    public MessageSender() {
    }

    @Override
    public void run() {
        while (isRunning) {
            if (!Globals.isActive) {
                continue;
            }

            int numOfMsg = RANDOM_GENERATOR.nextInt(DIFF) + MIN_PER_ACTIVE;

            for (int i = 0; i < numOfMsg; i++) {

                int nextNodeId = selectRandomNeighbor();

                // Send to this random node
                ObjectOutputStream outputStream = OUTPUTSTREAM_MAP.get(nextNodeId);
                Message message = null;
                synchronized (Globals.vectorClock) {
                    Payload p = new Payload(Globals.vectorClock);
                    message = new Message(p, MessageType.APPLICATION);
                }

                try {
                    outputStream.writeObject(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(MIN_SEND_DELAY);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        }

    }
    
    private int selectRandomNeighbor() {
        while(true) {
            int random = RANDOM_GENERATOR.nextInt(CLUSTER_SIZE);
            if(NEIGHBORS.contains(random)) {
                return random;
            }
        }
    }
}
