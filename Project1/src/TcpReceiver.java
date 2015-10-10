import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Receiver thread for the node which receives tokens sent to a given input stream.
 * It processes the received tokens and forwards accordingly.
 * @author ketan
 */
public class TcpReceiver implements Runnable {

    private static int tokensReturned = 0;

    private final ObjectInputStream inputStream;
    private final int numOfTokens;
    private final int label;

    public TcpReceiver(
            final ObjectInputStream inputStream,
            final int numOfTokens,
            final int labelValue) {

        this.inputStream = inputStream;
        this.numOfTokens = numOfTokens;
        this.label = labelValue;
    }

    @Override
    public void run() {

        while(true) {
            Token token = null;
            try {
                synchronized (inputStream) {
                    token = (Token) inputStream.readObject();
                }
            } catch (ClassNotFoundException e) {
//                Globals.log("Receiver : " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
//                Globals.log("Receiver : " + e.getMessage());
                e.printStackTrace();
            }

            // Check if the currently received token has completed its path
            if (token.isConsumed()) {
                tokensReturned++;
                Globals.log("Received token " + token.getId()
                        + "\tToken sum = " + token.getSum());
                if (tokensReturned == numOfTokens) {
                    Globals.log("All tokens received");
                }
                continue;
            }

            // Add the label value to the token
            token.addToSum(label);

            // Get next node from the token path
            int nextNodeId = token.getNextPathNode();

            // Send the token to the destined node
            TcpSender tokenSender = new TcpSender(
                    Globals.getWriterStream(nextNodeId),
                    token);
            Thread thread = new Thread(tokenSender);
            thread.start();

        }

    }

}
