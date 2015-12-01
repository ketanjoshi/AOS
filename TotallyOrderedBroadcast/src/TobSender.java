import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Class to broadcast the application message once CS is granted
 * @author ketan
 *
 */
public class TobSender implements Runnable {

    private static final long SLEEP_TIME = 1000;
    private static final int ID = TobGlobals.id;
    private static final HashMap<Integer, ObjectOutputStream> OUTPUTSTREAM_MAP = TobGlobals.writerStreamMap;
    private static final MutexHandler MUTEX_HANDLER = TobHandler.getMutexHandler();

    public static volatile boolean isRunning = true;

    public TobSender() {
        // set the mutex handle, which we need to use for csLeave and csEnter
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                while(TobGlobals.pendingTobs.size() == 0) {
                    Thread.sleep(SLEEP_TIME);
                }

                // Get mutex permissions
                System.out.println("CSEnter... asking CS permissions...");
                getMutexPermission();

                synchronized (TobGlobals.pendingTobs) {

                    System.out.println("CS granted... broadcasting messages...");

                    // Once csEnter returns, send that many messages
                    Iterator<String> iter = TobGlobals.pendingTobs.iterator();
                    String content = iter.next();
                    while(true) {
                        if(iter.hasNext())
                            content += "\n";
                        else
                            break;
                        content += iter.next();
                    }
                    TobGlobals.pendingTobs.clear();
                    // Create a message
                    Message message = new Message(ID, content, MessageType.APPLICATION);
                    // Broadcast the message
                    for (ObjectOutputStream stream : OUTPUTSTREAM_MAP.values()) {
                        synchronized (stream) {
                            stream.writeObject(message);
                            stream.flush();
                        }
                    }
                }

                System.out.println("CSLeave... leaving CS...");
                // call for csLeave
                releaseMutexPermission();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getMutexPermission() {
        MUTEX_HANDLER.csEnter();
    }

    private void releaseMutexPermission() {
        MUTEX_HANDLER.csLeave();
    }
}
