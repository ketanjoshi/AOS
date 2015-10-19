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
                    if (Globals.isActive) {
                        // Already active, ignore the message
                        continue;
                    }
                    if (Globals.sentMessageCount >= Globals.maxNumber) {
                        // Cannot become active, so ignore
                        continue;
                    }

                    // Can become active
                    Globals.isActive = true;

                }
                else if(type.equals(MessageType.MARKER)) {
                    // Record state and send
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
