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
                    handleApplicationMessage(message);
                }
                else if(type.equals(MessageType.MARKER)) {
                    Globals.setMarkerMsgReceived(true);
                    handleMarkerMessage(message);
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

    private void handleApplicationMessage(Message message) {
        // Application message
        Globals.incrementReceivedMessageCount();
        mergeVectorClocks(message);

        Globals.log("Received message : " + message 
                + "\nMerged clock : " + Globals.getPrintableGlobalClock());

        if (Globals.isNodeActive()) {
            // Already active, ignore the message
            return;
        }
        if (Globals.sentMessageCount >= Globals.maxNumber) {
            // Cannot become active, so ignore
            return;
        }

        // Can become active
        Globals.setNodeActive(true);
    }

    private void handleMarkerMessage(Message message) {
        // Record state and send
        Message replyMessage = null;
        if(Globals.isMarkerMsgReceived()) {
            // Send ignore message
            replyMessage =  new Message(ID, null, MessageType.IGNORED);
        }
        else {
            // Send local state as reply
            Payload p = new Payload(Globals.getGlobalVectorClock(), Globals.isNodeActive(),
                    Globals.getSentMessageCount(), Globals.getReceivedMessageCount());
            replyMessage = new Message(ID, p, MessageType.LOCAL_STATE);
        }

        SnapshotSender snapshotSender = new SnapshotSender(message.getId(), replyMessage);
        Thread thread = new Thread(snapshotSender);
        thread.start();
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
