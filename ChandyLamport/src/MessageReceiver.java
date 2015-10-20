import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Receiver thread for the node which receives tokens sent to a given input stream.
 * It processes the received tokens and forwards accordingly.
 * @author ketan
 */
public class MessageReceiver implements Runnable {

    private final ObjectInputStream inputStream;
    private static final int ID = Globals.id;

    public volatile boolean isRunning = true;

    public MessageReceiver(final ObjectInputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {

        while(isRunning) {
            try {
                Message message = (Message) inputStream.readObject();
                MessageType type = message.getType();
                if (type.equals(MessageType.APPLICATION)) {
                    // application message
                    mergeVectorClocks(message);

                    Globals.log("Received message : " + message 
                            + "\nMerged clock : " + Globals.getPrintableGlobalClock());

                    boolean isNodeActive = false;
                    synchronized (Globals.isActive) {
                        isNodeActive = Globals.isActive;
                    }
                    if (isNodeActive) {
                        // Already active, ignore the message
                        continue;
                    }
                    if (Globals.sentMessageCount >= Globals.maxNumber) {
                        // Cannot become active, so ignore
                        continue;
                    }

                    // Can become active
                    synchronized (Globals.isActive) {
                        Globals.isActive = true;
                    }

                }
                else if(type.equals(MessageType.MARKER)) {
                    // Record state and send
                    throw new RuntimeException("Marker msg not implemented");
                }
                else if(type.equals(MessageType.FINISH)) {
                    throw new RuntimeException("Finish msg not implemented");
                    // Exit
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }

    }
    
    private void mergeVectorClocks(Message message) {
        int[] piggybackVectorClock = message.getPayload().getVectorClock();
        synchronized (Globals.vectorClock) {
            for (int i = 0; i < Globals.clusterSize; i++) {
                if (i == ID) {
                    Globals.vectorClock[i]++;
                }
                Globals.vectorClock[i] = Math.max(Globals.vectorClock[i],
                        piggybackVectorClock[i]);
            }
            
        }
    }

}
