import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;


public class SnapshotInitiator implements Runnable {

    private static class PayloadComparator implements Comparator<Payload> {

        public PayloadComparator() {
        }

        @Override
        public int compare(Payload p1, Payload p2) {
            return p1.getId() - p2.getId();
        }

    }

    private static final long SNAPSHOT_DELAY = Globals.snapshotDelay;
    private static final PayloadComparator PAYLOAD_COMPARATOR = new PayloadComparator();

    private final ArrayList<Integer> neighbors;

    public volatile boolean isRunning = true;

    public SnapshotInitiator(final ArrayList<Integer> neighbors) {
        this.neighbors = neighbors;
    }

    public void initiateSnapshotProcess() {
        Globals.log("Initiating snapshot...");
        Globals.setMarkerMsgReceived(true);
        Message snapshotMessage = new Message(Globals.id, null, MessageType.MARKER);
        for (Integer neighborId : neighbors) {
            SnapshotSender snapshotSender = new SnapshotSender(neighborId, snapshotMessage);
            Thread thread = new Thread(snapshotSender);
            thread.start();
        }
    }

    @Override
    public void run() {

        while (isRunning) {

            initiateSnapshotProcess();

            while (!Globals.isAllSnapshotReplyReceived()) {
                // Continue to wait till all replies received
            }

            TreeSet<Payload> replyPayloadList = new TreeSet<>(PAYLOAD_COMPARATOR);
            replyPayloadList.addAll(Globals.getPayloads());

            StringBuilder builder = new StringBuilder("--------------Snapshot---------------\n");
            for (Payload p : replyPayloadList) {
                builder.append(p.toString() + "\n");
            }
            builder.append("-------------------------------------");
            Globals.log(builder.toString());

            if (isSystemTerminated(replyPayloadList)) {
                Globals.log("********************System terminated...");
                /**
                 * TODO: Send FINISH messages and terminate.
                 */
            }
            else {
                Globals.log("********************NOT terminated...");
            }

            // Reset snapshot variables
            Globals.resetSnapshotVariables();

            // If system not yet terminated, sleep for constant time
            try {
                Globals.log("Snapshot process sleeping... " + SNAPSHOT_DELAY);
                Thread.sleep(SNAPSHOT_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }

        }
    }

    private boolean isSystemTerminated(TreeSet<Payload> payloads) {
        return isAllPassive(payloads) && isChannelsEmpty(payloads);
    }

    private boolean isAllPassive(TreeSet<Payload> payloads) {
        boolean isAnyActive = false;
        for (Payload payload : payloads) {
            isAnyActive |= payload.isActive();
        }
        return !isAnyActive;
    }

    private boolean isChannelsEmpty(TreeSet<Payload> payloads) {
        int totalSentCount = 0, totalReceiveCount = 0;
        for (Payload payload : payloads) {
            totalReceiveCount += payload.getReceivedMsgCount();
            totalSentCount += payload.getSentMsgCount();
        }
        return totalReceiveCount - totalSentCount == 0;
    }

}
