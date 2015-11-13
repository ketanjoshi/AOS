import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Class to store and access global scope variables
 * @author ketan
 */
public class Globals {

    public static final Random RANDOM = new Random();

    public static int loggerCalls = 0;
    /**
     * Snapshot related globals
     */
    public static boolean markerMsgReceived = false;
    public static boolean allSnapshotReplyReceived = false;
    public static int receivedSnapshotReplies = 0;
    public static int markerSenderNode;
    public static int markersReceivedSoFar = 0;
    public static boolean repliedToSnapshot = false;
    public static boolean isSystemTerminated = false;
    public static HashSet<Integer> markersReceived = new HashSet<>();

    /**
     * Application message related globals
     */
    public static boolean isActive = false;
    public static int sentMessageCount = 0;
    public static int receivedMessageCount = 0;

    /**
     * Environment globals
     */
    public static int id;
    public static int neighborCount;
    public static int clusterSize;
    public static int minPerActive;
    public static int maxPerActive;
    public static int maxNumber;
    public static long minSendDelay;
    public static long snapshotDelay;
    public static int[] vectorClock;

    public static ArrayList<Payload> payloads = new ArrayList<>();

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
            String prependString = loggerCalls == 0 ? "" : "\n";
            logger.write(prependString + message);
            logger.flush();
            loggerCalls++;
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

    public static synchronized void incrementSentMessageCount() {
        sentMessageCount++;
    }

    public static synchronized int getReceivedMessageCount() {
        return receivedMessageCount;
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

    /**
     * Returns a boolean if valid marker message is received.
     * If any stale marker message is received then it should return false.
     * @return true if valid marker message received, else false
     */
    public static synchronized boolean isMarkerMsgReceived() {
        if(!markerMsgReceived) {
            if((markersReceivedSoFar % neighborCount) == 1) {
                markerMsgReceived = true;
            }
            return false;
        }
        return markerMsgReceived;
    }

    public static synchronized boolean isRepliedToSnapshot() {
        if (!repliedToSnapshot) {
            repliedToSnapshot = true;
            return false;
        }
        return repliedToSnapshot;
    }

    public static synchronized void setMarkerMsgReceived(boolean markerReceived) {
        markerMsgReceived = markerReceived;
    }

    public static synchronized boolean isAllSnapshotReplyReceived() {
        return allSnapshotReplyReceived;
    }

    public static synchronized void setAllSnapshotReplyReceived(boolean allSnapshotReceived) {
        allSnapshotReplyReceived = allSnapshotReceived;
    }

    public static synchronized boolean isSystemTerminated() {
        return isSystemTerminated;
    }

    public static synchronized void setIsSystemTerminated(boolean systemTerminated) {
        isSystemTerminated = systemTerminated;
    }

    public static synchronized int getMarkerSenderNode() {
        return markerSenderNode;
    }

    public static synchronized void setMarkerSenderNode(int markerSender) {
        markerSenderNode = markerSender;
    }

    public static synchronized int getReceivedSnapshotReplies() {
        return receivedSnapshotReplies;
    }

    public static synchronized void incrementReceivedSnapshotReplies() {
        receivedSnapshotReplies++;
    }

    public static synchronized int getMarkersReceivedSoFar() {
        return markersReceivedSoFar;
    }

    public static synchronized void incrementMarkersReceivedSoFar() {
        markersReceivedSoFar++;
    }

    public static synchronized ArrayList<Payload> getPayloads() {
        return payloads;
    }

    public static synchronized void addPayloads(ArrayList<Payload> payload) {
        payloads.addAll(payload);
    }

    public static synchronized void addPayload(Payload payload) {
        payloads.add(payload);
    }

    /**
     * Resets all the variables used for snapshot capturing
     */
    public static synchronized void resetSnapshotVariables() {
        payloads.clear();
        payloads = new ArrayList<>();
        receivedSnapshotReplies = 0;
        allSnapshotReplyReceived = false;
        markerMsgReceived = id == 0;
        repliedToSnapshot = false;
    }
}
