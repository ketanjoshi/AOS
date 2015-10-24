import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class SnapshotInitiator implements Runnable {

    private static final long SNAPSHOT_DELAY = Globals.snapshotDelay;
    private final ArrayList<Integer> neighbors;

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
                // TODO Auto-generated catch block
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {

//        while (true) {
        for(int i = 0; i < 3; i++) {

            initiateSnapshotProcess();

            while (!Globals.isAllSnapshotReplyReceived()) {
                // Continue to wait till all replies received
//                try {
//                    Thread.sleep(SNAPSHOT_DELAY);
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
            }

            ArrayList<Payload> replyPayloadList = new ArrayList<>();
            replyPayloadList.addAll(Globals.getPayloads());

            StringBuilder sb = new StringBuilder("--------------Snapshot---------------\n");
            for (Payload p : replyPayloadList) {
                sb.append(p.toString() + "\n");
            }
            sb.append("-------------------------------------");
            Globals.log(sb.toString());

            // Reset snapshot variables
            Globals.resetSnapshotVariables();

            // All replies received, check if system has terminated



            /**
             *  If system not yet terminated, sleep for constant time
            */

            try {
                Globals.log("Sleeping snapshot initiator... " + SNAPSHOT_DELAY);
                Thread.sleep(SNAPSHOT_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }

        }
    }

    private boolean isStronglyConsistentSnapshot() {
        return false;
    }

}
