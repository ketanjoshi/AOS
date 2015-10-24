import java.io.Serializable;
import java.util.ArrayList;


public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final ArrayList<Payload> payload;
    private final MessageType type;

    public Message(final int id, final ArrayList<Payload> payload, final MessageType type) {
        this.id = id;
        this.payload = payload;
        this.type = type;
    }

    public ArrayList<Payload> getPayload() {
        return payload;
    }

    public MessageType getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Id : " + id
                + " Payload : " + payload
                + " Type : " + type;
    }
}
