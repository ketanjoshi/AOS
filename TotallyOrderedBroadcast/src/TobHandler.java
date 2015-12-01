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

    private static TobHandler tobHandler = new TobHandler();
    private static ArrayList<Thread> tobReceivers = null;
    private static Thread tobSender = null;
    private static boolean initialised = false;
    private static MutexHandler mutexHandler = new MutexHandler();

    private TobHandler() {}

    public static TobHandler getInstance() {
        return tobHandler;
    }

    public void initialise() {
        setupTobReceivers();
        setupTobSender();
        initialised = true;
    }

    private void setupTobSender() {
        TobSender sender = new TobSender();
        tobSender = new Thread(sender);
        tobSender.start();
    }

    private void setupTobReceivers() {
        tobReceivers = new ArrayList<>();
        for (ObjectInputStream stream : INPUTSTREAM_MAP.values()) {
            TobReceiver receiver = new TobReceiver(stream);
            Thread thread = new Thread(receiver);
            thread.start();
            tobReceivers.add(thread);
        }
    }

    public void tobSend(String m) {
        if(!initialised)
            initialise();

        System.out.println("Received tobSend - " + m);
        synchronized (TobGlobals.pendingTobs) {
            TobGlobals.pendingTobs.add(TobGlobals.id + ":" + m);
        }
    }

    public List<Message> tobReceive() {
        if(!initialised)
            initialise();

        int receivedTobCount = 0;
        while(receivedTobCount == 0) {
            receivedTobCount = TobGlobals.receivedTobs.size();
            if(receivedTobCount == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        ArrayList<Message> list = new ArrayList<>();
        synchronized (TobGlobals.receivedTobs) {
            list.addAll(TobGlobals.receivedTobs);
            TobGlobals.receivedTobs.clear();
        }
        return list;
    }

    public static MutexHandler getMutexHandler() {
        return mutexHandler;
    }

}
