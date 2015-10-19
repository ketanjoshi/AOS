import java.io.Serializable;


public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final Payload payload;
    private final MessageType type;

    public Message(final int id, final Payload payload, final MessageType type) {
        this.id = id;
        this.payload = payload;
        this.type = type;
    }

    public Payload getPayload() {
        return payload;
    }

    public MessageType getType() {
        return type;
    }

    public int getId() {
        return id;
    }

}
