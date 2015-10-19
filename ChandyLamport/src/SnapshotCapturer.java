
public class SnapshotCapturer implements Runnable {

    private static final long SNAPSHOT_DELAY = Globals.snapshotDelay;

    public SnapshotCapturer() {
    }

    public void initiateSnapshotProcess() {
        Message snapshotMsg = new Message(Globals.id, null, MessageType.MARKER);
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
