import java.io.Serializable;


public class Payload implements Serializable {

    private int[] vectorClock;
    private boolean isActive;
    private int sentMsgCount;
    private int receivedMsgCount;

    public Payload(final int[] vectorClock,
            final boolean isActive,
            final int sentMsgCount,
            final int receivedMsgCount) {
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
        StringBuilder builder = new StringBuilder("VectorClock : [ ");
        for(int i = 0; i < vectorClock.length; i++) {
            builder.append(vectorClock[i] + " ");
        }
        builder.append("]");
        return builder.toString();
    }

}
