import java.util.List;
import java.util.Random;


/**
 * Singleton class which implements application layer behavior.
 * Creates a TOB handler and periodically sends message.
 * @author ketan
 *
 */
public class ApplicationLayer implements Runnable {

    private static final long DELAY = TobGlobals.delay;
    private static final long NUM_MSG = TobGlobals.numMessages;
    private static final long NUM_NODES = TobGlobals.numNodes;
    private static final Random RANDOM = new Random();
    private static final int BOUND = 20;

    private static ApplicationLayer appLayer = new ApplicationLayer();
    private static TobHandler tobHandler = null;
    private static boolean initialised = false;
    private static boolean isFirstTob = false;
    private static int receivedTobs = 0;

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

        while(receivedTobs < (NUM_MSG * NUM_NODES)) {
            System.out.println("Received messages");
            List<Message> list = tobHandler.tobReceive();
            receivedTobs += list.size();
            logReceivedTobs(list);
            System.out.println(list);
        }
    }

    private void logReceivedTobs(List<Message> list) {
        StringBuilder sb = new StringBuilder();
        for (Message message : list) {
            if(isFirstTob) {
                sb.append("\n");
            } else {
                isFirstTob = true;
            }
            sb.append(message.getContent());
        }
        TobGlobals.log(sb.toString());
    }

}
