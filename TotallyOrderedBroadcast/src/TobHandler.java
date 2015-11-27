import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Singleton class which takes care of setting up TOB related threads.
 * Activates a single sender thread and one receiver thread per peer for TOB.
 * @author ketan
 *
 */
public class TobHandler {

    private static final HashMap<Integer, ObjectInputStream> INPUTSTREAM_MAP = TobGlobals.readerStreamMap;

    private static TobHandler tobHandler = null;
    private static ArrayList<Thread> tobReceivers = null;
    private static Thread tobSender = null;

    private TobHandler() {}

    public static TobHandler getInstance() {
        if(tobHandler == null) {
            tobHandler = new TobHandler();
            setupTobReceivers();
            setupTobSender();
        }
        return tobHandler;
    }

    private static void setupTobSender() {
        TobSender sender = new TobSender();
        tobSender = new Thread(sender);
        tobSender.start();
    }

    private static void setupTobReceivers() {
        tobReceivers = new ArrayList<>();
        for (ObjectInputStream stream : INPUTSTREAM_MAP.values()) {
            TobReceiver receiver = new TobReceiver(stream);
            Thread thread = new Thread(receiver);
            thread.start();
            tobReceivers.add(thread);
        }
    }

    public void tobSend() {
        synchronized (TobGlobals.pendingTobRequestNum) {
            TobGlobals.pendingTobRequestNum++;
        }
    }

    public List<Message> tobReceive() {
        ArrayList<Message> list = new ArrayList<>();
        synchronized (TobGlobals.receivedTobs) {
            list.addAll(TobGlobals.receivedTobs);
            TobGlobals.receivedTobs.clear();
        }
        return list;
    }
}
