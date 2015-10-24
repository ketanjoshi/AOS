import java.io.Serializable;


/**
 * Captures the local state of particular node
 * @author ketan
 */
public class Payload implements Serializable {

    /**
     * Default serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    private int id;
    private int[] vectorClock;
    private boolean isActive;
    private int sentMsgCount = -1;
    private int receivedMsgCount = -1;

    public Payload(final int id,
            final int[] vectorClock,
            final boolean isActive,
            final int sentMsgCount,
            final int receivedMsgCount) {
        this.id = id;
        this.vectorClock = new int[vectorClock.length];
        System.arraycopy(vectorClock, 0, this.vectorClock, 0, vectorClock.length);
        this.isActive = isActive;
        this.sentMsgCount = sentMsgCount;
        this.receivedMsgCount = receivedMsgCount;
    }

    public Payload(final int[] vectorClock) {
        this.vectorClock = new int[vectorClock.length];
        System.arraycopy(vectorClock, 0, this.vectorClock, 0, vectorClock.length);
    }

    public int[] getVectorClock() {
        return vectorClock;
    }

    public void setVectorClock(int[] vectorClock) {
        this.vectorClock = vectorClock;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getSentMsgCount() {
        return sentMsgCount;
    }

    public int getReceivedMsgCount() {
        return receivedMsgCount;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Id : " + id + " VectorClock : [ ");
        for(int i = 0; i < vectorClock.length; i++) {
            builder.append(vectorClock[i] + " ");
        }
        builder.append("]")
               .append(" SentMsgCount : " + sentMsgCount)
               .append(" ReceivedMsgCount : " + receivedMsgCount);
        return builder.toString();
    }

}
