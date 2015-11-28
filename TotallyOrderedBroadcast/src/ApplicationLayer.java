
/**
 * Singleton class which implements application layer behavior.
 * Creates a TOB handler and periodically sends message.
 * @author ketan
 *
 */
public class ApplicationLayer implements Runnable {

    private static final long DELAY = TobGlobals.delay;
    private static final long NUM_MSG = TobGlobals.numMessages;

    private static ApplicationLayer appLayer = new ApplicationLayer();
    private static TobHandler tobHandler = null;
    private static boolean initialised = false;

    private ApplicationLayer() {}

    public static ApplicationLayer getInstance() {
        return appLayer;
    }

    public void initialise() {
        if(tobHandler == null)
            return;

        tobHandler = TobHandler.getInstance();
        tobHandler.initialise();
        initialised = true;

    }

    @Override
    public void run() {
        if(!initialised)
            initialise();

        for (int i = 0; i < NUM_MSG; i++) {
            tobHandler.tobSend();
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
        
    }

}
