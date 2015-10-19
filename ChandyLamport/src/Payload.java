import java.io.Serializable;


public class Payload implements Serializable {

    private int[] vectorClock;
    private boolean isActive;
    private int inTransitMsgCount;

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

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public int getInTransitMsgCount() {
        return inTransitMsgCount;
    }

    public void setInTransitMsgCount(int inTransitMsgCount) {
        this.inTransitMsgCount = inTransitMsgCount;
    }

}
