import java.io.Serializable;

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
    private final String content;
    private final MessageType messageType;
    private final Integer logicalClock;
    private int randomNumber;

    public Message(final int id, final String content, MessageType messageType, Integer logicalClock) {
        this.id = id;
        this.content = content;
        this.messageType = messageType;
        this.logicalClock = logicalClock;
    }

    public Message(final int id, final String content, MessageType messageType) {
        this(id, content, messageType, null);
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public MessageType getMessageType() {
		return messageType;
	}

	public int getLogicalClock() {
		return logicalClock;
	}

	@Override
    public String toString() {
        return "Id : " + id
                + " Content : " + content;
    }
}
