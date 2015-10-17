import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 * Class to store and access global scope variables
 * @author ketan
 */
public class NetworkComponents {

    protected static HashMap<Integer, Socket> socketMap = new HashMap<>();
    protected static HashMap<Integer, ObjectInputStream> readerStreamMap = new HashMap<>();
    protected static HashMap<Integer, ObjectOutputStream> writerStreamMap = new HashMap<>();

    public static int getSocketMapSize() {
        return socketMap.size();
    }

    public static synchronized void addSocketEntry(int index, Socket socket) {
        socketMap.put(index, socket);
    }

    public static boolean hasSocketEntry(int index) {
        return socketMap.containsKey(index);
    }

    public static synchronized void addInputStreamEntry(int index, ObjectInputStream stream) {
        readerStreamMap.put(index, stream);
    }

    public static ObjectInputStream getReaderStream(int index) {
        return readerStreamMap.get(index);
    }

    public static synchronized void addOutputStreamEntry(int index, ObjectOutputStream stream) {
        writerStreamMap.put(index, stream);
    }

    public static ObjectOutputStream getWriterStream(int index) {
        return writerStreamMap.get(index);
    }

}
