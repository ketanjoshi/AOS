import java.util.ArrayList;
import java.util.Random;


/**
 * Singleton class which implements application layer behavior.
 * Creates a TOB handler and periodically sends message.
 * @author ketan
 *
 */
public class ApplicationLayer implements Runnable {

    private static int collectedTobs = 0;
    private static ArrayList<String> list = new ArrayList<>();
    private static final long DELAY = TobGlobals.delay;
    private static final long NUM_MSG = TobGlobals.numMessages;
    private static final Random RANDOM = new Random();
    private static final int BOUND = 20;

    private static ApplicationLayer appLayer = new ApplicationLayer();
    private static TobHandler tobHandler = null;
    private static boolean initialised = false;

    private ApplicationLayer() {}

    public static ApplicationLayer getInstance() {
        return appLayer;
    }

    public void initialise() {
        if(tobHandler != null)
            return;

        tobHandler = TobHandler.getInstance();
        tobHandler.initialise();
        initialised = true;
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        if(!initialised)
            initialise();

        for (int i = 0; i < NUM_MSG; i++) {
            tobHandler.tobSend(String.valueOf(RANDOM.nextInt(BOUND)));
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }

        while(true) {
            System.out.println("Received messages");
            System.out.println(tobHandler.tobReceive());
        }
    }

}
