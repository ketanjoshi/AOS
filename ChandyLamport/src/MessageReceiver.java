import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 * Receiver thread for the node which receives tokens sent to a given input stream.
 * It processes the received tokens and forwards accordingly.
 * @author ketan
 */
public class MessageReceiver implements Runnable {

    private static final int ID = Globals.id;
    private static final int CLUSTER_SIZE = Globals.clusterSize;

    private final ObjectInputStream inputStream;
    private final ArrayList<Integer> neighbors;
    private final int neighborCount;
    private final int expectedSnapshotReplies;

    public volatile boolean isRunning = true;

    public MessageReceiver(final ObjectInputStream inputStream,
            final ArrayList<Integer> neighbors) {
        this.inputStream = inputStream;
        this.neighbors = neighbors;
        this.neighborCount = neighbors.size();
        this.expectedSnapshotReplies = ID == 0 ? this.neighborCount : this.neighborCount - 1;
    }

    @Override
    public void run() {

        while(isRunning) {
            try {
                Message message = (Message) inputStream.readObject();
//                Globals.log("Message received : " + message.toString());
                MessageType type = message.getType();
                if (type.equals(MessageType.APPLICATION)) {
                    handleApplicationMessage(message);
                }
                else if(type.equals(MessageType.MARKER)) {
                    handleMarkerMessage(message);
                }
                else if(type.equals(MessageType.FINISH)) {
                    throw new RuntimeException("Finish msg not implemented");
                    // Exit
                }
                else {
                    handleSnapshotReplyMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleSnapshotReplyMessage(Message message) {
        // Increment received reply count
        Globals.incrementReceivedSnapshotReplies();
//        Globals.log("Snapshot reply received from : " + message.getId()
//                + "\nReplies received so far = " + Globals.getReceivedSnapshotReplies());

        if(message.getType().equals(MessageType.IGNORED)) {
            Globals.log("Received ignored reply from " + message.getId());
            // Do nothing
        }
        else {
            // LOCAL_STATE type
            Globals.addPayloads(message.getPayload());
            Globals.log("Adding received payload => " + message.getPayload());
        }
        // Check if all expected replies are received
        if(Globals.getReceivedSnapshotReplies() == expectedSnapshotReplies) {
            Globals.log("Received expected number of replies");
         // Send consolidated local state
            Payload myPayload = new Payload(ID, Globals.getGlobalVectorClock(),
                    Globals.isNodeActive(), Globals.getSentMessageCount(),
                    Globals.getReceivedMessageCount());
            Globals.addPayload(myPayload);

            // If node ID = 0, then set all replies received as true
            if (ID == 0) {
                Globals.setAllSnapshotReplyReceived(true);
            }
            else {
                ArrayList<Payload> snapshotPayload = new ArrayList<>();
                snapshotPayload.addAll(Globals.getPayloads());

                Message replyStateMsg = new Message(ID, snapshotPayload,
                        MessageType.LOCAL_STATE);
                int markerSenderNode = Globals.getMarkerSenderNode();
                Globals.log("Send snapshot reply to " + markerSenderNode 
                        + "\nMessage==> " + replyStateMsg);
                launchSnapshotSender(markerSenderNode, replyStateMsg);

                // Reset all counter variables
                Globals.resetSnapshotVariables();
            }

        }
    }

    private void handleApplicationMessage(Message message) {
        // Application message
        Globals.incrementReceivedMessageCount();
        mergeVectorClocks(message);

//        Globals.log("Received message : " + message 
//                + "\nMerged clock : " + Globals.getPrintableGlobalClock());

        if (Globals.isNodeActive()) {
            // Already active, ignore the message
            return;
        }
        if (Globals.getSentMessageCount() >= Globals.maxNumber) {
            // Cannot become active, so ignore
            return;
        }

        // Can become active
        Globals.setNodeActive(true);
    }

    private void handleMarkerMessage(Message message) {
        Globals.incrementMarkersReceivedSoFar();

        // Record state and send
        if (Globals.isMarkerMsgReceived() || ID == 0) {
            // Send ignore message
            Globals.log("Marker msg received from " + message.getId() + "... IGNORE");
            Message replyMessage =  new Message(ID, null, MessageType.IGNORED);
            launchSnapshotSender(message.getId(), replyMessage);
        }
        else {
            Globals.setMarkerMsgReceived(true);
            Globals.setMarkerSenderNode(message.getId());
            Globals.log("Marker msg received from " + message.getId() + "... BROADCAST\n"
                    + "Expecting replies = " + expectedSnapshotReplies);
            // Send marker message to neighbors and wait for response
            Message broadcastMarkerMsg = new Message(ID, null, MessageType.MARKER);
            for (Integer neighborId : neighbors) {
                if (neighborId != message.getId()) {
                    launchSnapshotSender(neighborId, broadcastMarkerMsg);
                }
            }
        }
    }

    private void mergeVectorClocks(Message message) {
        int[] piggybackVectorClock = message.getPayload().get(0).getVectorClock();
        synchronized (Globals.vectorClock) {
            for (int i = 0; i < CLUSTER_SIZE; i++) {
                if (i == ID) {
                    Globals.vectorClock[i]++;
                }
                Globals.vectorClock[i] = Math.max(Globals.vectorClock[i],
                        piggybackVectorClock[i]);
            }
        }
    }

    private void launchSnapshotSender(int id, Message message) {
        SnapshotSender snapshotSender = new SnapshotSender(id, message);
        Thread thread = new Thread(snapshotSender);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
