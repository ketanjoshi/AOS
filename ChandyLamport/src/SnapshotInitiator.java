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
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
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

            // Reset snapshot variables
            Globals.resetSnapshotVariables();

            /**
             * TODO: Check if state is strongly consistent => system has terminated.
             *       If yes, send FINISH messages and terminate.
             */
            checkSystemTermination();


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

    private boolean checkSystemTermination() {
        isStronglyConsistentSnapshot();
        return false;
    }

    private boolean isStronglyConsistentSnapshot() {
        return false;
    }

}
