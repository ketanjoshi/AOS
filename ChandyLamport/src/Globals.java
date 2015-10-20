import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Class to store and access global scope variables
 * @author ketan
 */
public class Globals {

    public static final Random RANDOM = new Random();

    public static boolean markerMsgReceived = false;
    public static Boolean isActive = false;
    public static Integer sentMessageCount = 0;
    public static Integer receivedMessageCount = 0;

    public static int id;
    public static int clusterSize;
    public static int minPerActive;
    public static int maxPerActive;
    public static int maxNumber;
    public static long minSendDelay;
    public static long snapshotDelay;
    public static int[] vectorClock;

    private static FileWriter logger = null;

    public static void initialiseLogger(String fileName) {
        if(logger != null) {
            return;
        }
        try {
            logger = new FileWriter(fileName);
        } catch (IOException e) {
            System.err.println("Failed to initialize logger : " + e.getStackTrace());
        }
    }

    public static synchronized void log(String message) {
        if(logger == null) {
            initialiseLogger("config-log.out");
        }

        try {
            logger.write(message + "\n");
            logger.flush();
        } catch (IOException e) {
            System.err.println("Problem while logging..."
                    + "Last log message : " + message);
        }
    }

    public static String getPrintableGlobalClock() {
        StringBuilder builder = new StringBuilder("VectorClock : [ ");
        for(int i = 0; i < vectorClock.length; i++) {
            builder.append(vectorClock[i] + " ");
        }
        builder.append("]");
        return builder.toString();

    }

    public static synchronized int[] getGlobalVectorClock() {
        return vectorClock;
    }

    public static synchronized int getSentMessageCount() {
        return sentMessageCount;
    }

    public static synchronized int getReceivedMessageCount() {
        return receivedMessageCount;
    }

    public static synchronized void incrementSentMessageCount() {
        sentMessageCount++;
    }

    public static synchronized void incrementReceivedMessageCount() {
        receivedMessageCount++;
    }

    public static synchronized boolean isNodeActive() {
        return isActive;
    }

    public static synchronized void setNodeActive(boolean isNodeActive) {
        isActive = isNodeActive;
    }

    public static synchronized boolean isMarkerMsgReceived() {
        return markerMsgReceived;
    }

    public static synchronized void setMarkerMsgReceived(boolean markerReceived) {
        markerMsgReceived = markerReceived;
    }

}
