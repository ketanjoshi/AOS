import java.io.ObjectInputStream;

public class MutexHandler {

    public MutexHandler() {
        initialiseLamportReceivers();
    }

    private void initialiseLamportReceivers() {
        for (ObjectInputStream stream : MutexGlobals.readerStreamMap.values()) {
            LamportsReceiver receiver = new LamportsReceiver(stream);
            Thread thread = new Thread(receiver);
            thread.start();
        }
    }

    public void csEnter() {
        MutexPriorityQueueElement qElement = new MutexPriorityQueueElement(
                MutexGlobals.id, TobMutexGlobals.getLogicalclock());
        synchronized (MutexGlobals.mutexReqQueue) {
            MutexGlobals.mutexReqQueue.add(qElement);
        }
        synchronized (MutexGlobals.mutexReqClock) {
            MutexGlobals.mutexReqClock = TobMutexGlobals.getLogicalclock();
        }

        Message msg = new Message(MutexGlobals.id, null,
                MessageType.MUTEX_REQUEST, TobMutexGlobals.getLogicalclock());

        // Initiate sender thread to broadcast the mutex request message
        LamportsSender lamportSender = new LamportsSender(msg, null);
        Thread thread = new Thread(lamportSender);
        thread.start();

        while (!TobMutexGlobals.isReqGranted()) {
            // wait
        }
    }

    public void csLeave() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        synchronized (TobMutexGlobals.reqGranted) {
            TobMutexGlobals.reqGranted = false;
        }

        synchronized (MutexGlobals.mutexReqQueue) {
            MutexGlobals.mutexReqQueue.poll();
        }

        Message msg = new Message(MutexGlobals.id, null, MessageType.CS_LEAVE,
                TobMutexGlobals.getLogicalclock());

        // Initiate sender thread to broadcast the mutex leave message
        LamportsSender lamportSender = new LamportsSender(msg, null);
        Thread thread = new Thread(lamportSender);
        thread.start();
    }

}
