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
                MessageType type = message.getType();
                if (type.equals(MessageType.APPLICATION)) {
                    handleApplicationMessage(message);
                }
                else if(type.equals(MessageType.MARKER)) {
                    handleMarkerMessage(message);
                }
                else if(type.equals(MessageType.FINISH)) {
                    handleFinishMessage(message);
                }
                else {
                    // LOCAL_STATE or IGNORED message
                    handleSnapshotReplyMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleFinishMessage(Message message) {
        Globals.setIsSystemTerminated(true);
        isRunning = false;
    }

    /**
     * <pre>Processes incoming snapshot reply message
     * If type LOCAL_STATE - Adds received payload to the global payload list
     * If type IGNORED - ignore the message
     * When all the expected replies are received,
     * send the consolidated payload to the node from which it received the marker message<pre>
     * @param message {@link Message}
     */
    private void handleSnapshotReplyMessage(Message message) {
        // Increment received reply count
        Globals.incrementReceivedSnapshotReplies();

        if(message.getType().equals(MessageType.IGNORED)) {
//            Globals.log("Received IGNORED reply from " + message.getId());
            // Do nothing
        }
        else {
            // LOCAL_STATE type
            Globals.addPayloads(message.getPayload());
//            Globals.log("Received LOCAL_STATE reply from " + message.getId() 
//                    + " ==> Received payload : " + message.getPayload());
        }
        // Check if all expected replies are received
        if((Globals.getReceivedSnapshotReplies() == expectedSnapshotReplies) 
                && !Globals.isRepliedToSnapshot()) {
            // Add own local state to the received local state list
            int[] localClock = new int[CLUSTER_SIZE];
            synchronized (Globals.vectorClock) {
                System.arraycopy(Globals.vectorClock, 0, localClock, 0, CLUSTER_SIZE);
            }

            Payload myPayload = new Payload(ID, localClock,
                    Globals.isNodeActive(), Globals.getSentMessageCount(),
                    Globals.getReceivedMessageCount());
            Globals.log(myPayload.toString());
            Globals.addPayload(myPayload);

            // If node ID = 0, then set all replies received as true
            if (ID == 0) {
                Globals.setAllSnapshotReplyReceived(true);
            }
            else {
                // Send consolidated local state reply
//                Globals.log("Received expected number of replies, send cumulative local states");
                ArrayList<Payload> snapshotPayload = new ArrayList<>();
                snapshotPayload.addAll(Globals.getPayloads());

                Message replyStateMsg = new Message(ID, snapshotPayload,
                        MessageType.LOCAL_STATE);
                int markerSenderNode = Globals.getMarkerSenderNode();
//                Globals.log("Send snapshot reply to " + markerSenderNode 
//                        + " ==> Message : " + replyStateMsg);
                launchSnapshotSender(markerSenderNode, replyStateMsg);

                // Reset all counter variables
                Globals.resetSnapshotVariables();
            }

        }
    }

    /**
     * <pre>Processes incoming application message
     * Merge the piggybacked vector clock from the message 
     * and become active if node satisfies predefined criteria<pre>
     * @param message {@link Message}
     */
    private void handleApplicationMessage(Message message) {
        // Application message
        Globals.incrementReceivedMessageCount();
        mergeVectorClocks(message);

//        Globals.log("Received application message : " + message 
//                + "\nMerged clock : " + Globals.getPrintableGlobalClock());

        if (Globals.isNodeActive()) {
            // Already active, ignore the message
//            Globals.log("Already active...");
            return;
        }
        if (Globals.getSentMessageCount() >= Globals.maxNumber) {
            // Cannot become active, so ignore
//            Globals.log("Reached max send limit... cannot become active");
            return;
        }

        // Can become active
//        Globals.log("Becoming active...");
        Globals.setNodeActive(true);
    }

    /**
     * Processes incoming marker message
     * If it is a valid marker message, broadcast it to other neighbors, else discard
     * @param message {@link Message}
     */
    private void handleMarkerMessage(Message message) {
        Globals.incrementMarkersReceivedSoFar();

        // Record state and send
        if (Globals.isMarkerMsgReceived() || ID == 0) {
            // Send ignore message
//            Globals.log("Marker msg received from " + message.getId() + "... IGNORE");
            Message replyMessage =  new Message(ID, null, MessageType.IGNORED);
            launchSnapshotSender(message.getId(), replyMessage);
        }
        else {
            Globals.setMarkerMsgReceived(true);
            Globals.setMarkerSenderNode(message.getId());
//            Globals.log("Marker msg received from " + message.getId() + "... BROADCAST\n"
//                    + "Expecting replies = " + expectedSnapshotReplies);
            // Send marker message to neighbors and wait for response
            Message broadcastMarkerMsg = new Message(ID, null, MessageType.MARKER);
            for (Integer neighborId : neighbors) {
                if (neighborId != message.getId()) {
                    launchSnapshotSender(neighborId, broadcastMarkerMsg);
                }
            }
        }
    }

    /**
     * Merge the incoming message's piggybacked vector clock into own global clock
     * @param message {@link Message}
     */
    private void mergeVectorClocks(Message message) {
        int[] piggybackVectorClock = message.getPayload().get(0).getVectorClock();
        synchronized (Globals.vectorClock) {
            for (int i = 0; i < CLUSTER_SIZE; i++) {
                Globals.vectorClock[i] = Math.max(Globals.vectorClock[i], piggybackVectorClock[i]);
            }
            Globals.vectorClock[ID]++;
        }
    }

    /**
     * Launch {@link SnapshotSender} thread and wait for it to finish.
     * It sends given snapshot reply message to given node.
     * @param id - neighbor node id
     * @param message {@link Message}
     */
    private void launchSnapshotSender(int id, Message message) {
        SnapshotSender snapshotSender = new SnapshotSender(id, message);
        Thread thread = new Thread(snapshotSender);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
