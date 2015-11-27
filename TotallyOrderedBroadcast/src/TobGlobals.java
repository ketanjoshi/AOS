import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Class to store and access global scope variables
 * @author ketan
 */
public class TobGlobals {

    public static final Random RANDOM = new Random();

    public static int loggerCalls = 0;

    /**
     * Application message related globals
     */
    public static int sentMessageCount = 0;
    public static int receivedMessageCount = 0;

    public static ArrayList<Message> receivedTobs = new ArrayList<>();
    public static Integer pendingTobRequestNum = 0;

    /**
     * Environment globals
     */
    public static int id;
    public static int numNodes;
    public static int numMessages;
    public static long delay;

    private static FileWriter logger = null;

    protected static HashMap<Integer, Socket> socketMap = new HashMap<>();
    protected static HashMap<Integer, ObjectInputStream> readerStreamMap = new HashMap<>();
    protected static HashMap<Integer, ObjectOutputStream> writerStreamMap = new HashMap<>();

    public static int getSocketMapSize() {
        return socketMap.size();
    }

    public static synchronized void addSocketEntry(int index, Socket socket) {
        socketMap.put(index, socket);
    }

    public static boolean hasSocketEntry(int index) {
        return socketMap.containsKey(index);
    }

    public static synchronized void addInputStreamEntry(int index, ObjectInputStream stream) {
        readerStreamMap.put(index, stream);
    }

    public static ObjectInputStream getReaderStream(int index) {
        return readerStreamMap.get(index);
    }

    public static synchronized void addOutputStreamEntry(int index, ObjectOutputStream stream) {
        writerStreamMap.put(index, stream);
    }

    public static ObjectOutputStream getWriterStream(int index) {
        return writerStreamMap.get(index);
    }

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
