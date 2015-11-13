import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Class to store and access global scope variables
 * @author ketan
 */
public class Globals {

    public static final Random RANDOM = new Random();

    public static int loggerCalls = 0;

    /**
     * Application message related globals
     */
    public static int sentMessageCount = 0;
    public static int receivedMessageCount = 0;

    /**
     * Environment globals
     */
    public static int id;
    public static int numNodes;
    public static int numMessages;
    public static long delay;

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

}
