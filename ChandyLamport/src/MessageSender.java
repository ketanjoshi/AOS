import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Sender thread to send a message randomly 
 * on any output stream when the node is active
 * @author ketan
 */
public class MessageSender implements Runnable {

    private static final ArrayList<Integer> NEIGHBORS = AppConfigurations.getNeighborNodes();
    private static final int MIN_PER_ACTIVE = Globals.minPerActive;
    private static final int MAX_PER_ACTIVE = Globals.maxPerActive;
    private static final int DIFF = MAX_PER_ACTIVE - MIN_PER_ACTIVE;
    private static final long MIN_SEND_DELAY = Globals.minSendDelay;
    private static final HashMap<Integer, ObjectOutputStream> OUTPUTSTREAM_MAP = NetworkComponents.writerStreamMap;
    private static final Random RANDOM_GENERATOR = Globals.RANDOM;
    private static final int ID = Globals.id;
    private static final int CLUSTER_SIZE = Globals.clusterSize;

    public static volatile boolean isRunning = true;


    public MessageSender() {
    }

    @Override
    public void run() {
        while (isRunning) {

            if (!Globals.isNodeActive()) {
                continue;
            }

            int numOfMsg = RANDOM_GENERATOR.nextInt(DIFF) + MIN_PER_ACTIVE;
            sendApplicationMessages(numOfMsg);

            Globals.log("Turning passive...");
            Globals.setNodeActive(false);

            if (Globals.getSentMessageCount() >= Globals.maxNumber) {
                Globals.log("Stopping sender thread.");
                isRunning = false;
            }
        }

    }

    /**
     * Randomly selects a neighbor and sends application message to it.
     * Stops the execution if total sent messages reach the maximum limit.
     * @param numOfMsg - number of messages
     */
    private void sendApplicationMessages(int numOfMsg) {
        for (int i = 0; i < numOfMsg; i++) {

            int nextNodeId = selectRandomNeighbor();

            // Send to this random node
            ObjectOutputStream outputStream = OUTPUTSTREAM_MAP.get(nextNodeId);
            Message message = null;
            int[] localClock = new int[CLUSTER_SIZE];
            synchronized (Globals.vectorClock) {
                Globals.vectorClock[ID]++;
                System.arraycopy(Globals.vectorClock, 0, localClock, 0, CLUSTER_SIZE);
            }
            Payload p = new Payload(localClock);
            ArrayList<Payload> payloads = new ArrayList<>();
            payloads.add(p);
            message = new Message(ID, payloads, MessageType.APPLICATION);

            try {
                synchronized (outputStream) {
                    outputStream.writeObject(message);
                }
                Globals.incrementSentMessageCount();

                Globals.log("Sent message to " + nextNodeId
                            + " SentMessageCount : " + Globals.getSentMessageCount());
                if (Globals.getSentMessageCount() >= Globals.maxNumber) {
                    Globals.log("MaxNumber message reached.");
                    isRunning = false;
                    break;
                }
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

    /**
     * Returns a randomly chosen neighbor id
     * @return - neighbor node id
     */
    private int selectRandomNeighbor() {
        int size = NEIGHBORS.size();
        while(true) {
            int random = RANDOM_GENERATOR.nextInt(100) % size;
            return NEIGHBORS.get(random);
        }
    }
}
