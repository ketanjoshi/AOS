import java.io.Serializable;


public class Payload implements Serializable {

    private int[] vectorClock;
    private boolean isActive;
    private int inTransitMsgCount;

    public Payload(final int[] vectorClock) {
        this.vectorClock = new int[vectorClock.length];
        System.arraycopy(vectorClock, 0, this.vectorClock, 0, vectorClock.length);
    }

}
