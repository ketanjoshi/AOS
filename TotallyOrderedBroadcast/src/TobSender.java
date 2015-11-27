import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Random;


/**
 * Class to broadcast the application message once CS is granted
 * @author ketan
 *
 */
public class TobSender implements Runnable {

    private static final long SLEEP_TIME = 1000;
    private static final Random RANDOM = new Random();
    private static final int ID = TobGlobals.id;
    private static final HashMap<Integer, ObjectOutputStream> OUTPUTSTREAM_MAP = TobGlobals.writerStreamMap;

    public static volatile boolean isRunning = true;

    public TobSender() {
        // set the mutex handle, which we need to use for csLeave and csEnter
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                while(TobGlobals.pendingTobRequestNum == 0) {
                    Thread.sleep(SLEEP_TIME);
                }
                int messageNum;
                synchronized (TobGlobals.pendingTobRequestNum) {
                    messageNum = TobGlobals.pendingTobRequestNum;
                    TobGlobals.pendingTobRequestNum = 0;
                }

                System.out.println("CSEnter... waiting...");
                // blocking call for csEnter
                System.out.println("CS granted... broadcasting messages...");

             // Once csEnter returns, send that many messages
                for (int i = 0; i < messageNum; i++) {
                    // Generate random number for i-th message
                    int randomNum = RANDOM.nextInt(20);

                    // Create a message
                    // TODO: CONFIRM => I feel there is no need to pass logical clock value
                    Message message = new Message(ID, String.valueOf(randomNum), MessageType.APPLICATION);

                    // Broadcast the message
                    for (ObjectOutputStream stream : OUTPUTSTREAM_MAP.values()) {
                        stream.writeObject(message);
                        stream.flush();
                    }
                }


                System.out.println("CSLeave... leaving CS...");
                // call for csLeave
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
