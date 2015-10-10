import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;

/**
 * Sender thread to send a token on a given output stream.
 * @author ketan
 */
public class TcpSender implements Runnable {

    private final ObjectOutputStream outputStream;
    private final Token token;

    public TcpSender(
            final ObjectOutputStream outputStream,
            final Token payload) {
        this.outputStream = outputStream;
        this.token = payload;
    }

    @Override
    public void run() {

        try {
            synchronized (outputStream) {
                outputStream.writeObject(token);
                outputStream.flush();
            }
        } catch (UnknownHostException e) {
//            Globals.log("Sender : " + e.getMessage());
//            e.printStackTrace();
        } catch (IOException e) {
//            Globals.log("Sender : " + e.getMessage());
//            e.printStackTrace();
        }

    }
}
