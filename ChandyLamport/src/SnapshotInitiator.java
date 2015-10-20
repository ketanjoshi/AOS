
public class SnapshotInitiator implements Runnable {

    private static final long SNAPSHOT_DELAY = Globals.snapshotDelay;

    public SnapshotInitiator() {
    }

    public void initiateSnapshotProcess() {
        Message snapshotMessage = new Message(Globals.id, null, MessageType.MARKER);
    }

    @Override
    public void run() {

        initiateSnapshotProcess();
        try {
            Thread.sleep(SNAPSHOT_DELAY * 2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

    }

}
