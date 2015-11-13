import java.io.Serializable;
import java.util.ArrayList;


/**
 * Represents the message sent and received between nodes
 * @author ketan
 */
public class Message implements Serializable {

    /**
     * Default serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    private final int id;
    private final ArrayList<Payload> payload;
    private final MessageType type;
    private int messageId = 0;

    public Message(final int id, final ArrayList<Payload> payload, final MessageType type) {
        this.id = id;
        this.payload = payload;
        this.type = type;
    }

    public Message(final int id, final ArrayList<Payload> payload, final MessageType type, final int mId) {
        this.id = id;
        this.payload = payload;
        this.type = type;
        this.messageId = mId;
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

    public int getMessageId() {
        return messageId;
    }

    @Override
    public String toString() {
        return "Id : " + id
                + " Payload : " + payload
                + " Type : " + type;
    }
}
