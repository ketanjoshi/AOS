import java.io.ObjectInputStream;


/**
 * Class to receive the totally ordered broadcast message from particular node
 * @author ketan
 *
 */
public class TobReceiver implements Runnable {

    public static volatile boolean isRunning = true;
    private static int count = 0;

    private final ObjectInputStream iStream;

    public TobReceiver(final ObjectInputStream stream) {
        this.iStream = stream;
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                Message message = (Message) iStream.readObject();
                synchronized (TobGlobals.receivedTobs) {
                    TobGlobals.receivedTobs.add(message);
                }
                System.out.println("Received application message from " + message.getId());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
