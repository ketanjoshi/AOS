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

    public Message(final int id, final String content) {
        this.id = id;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Id : " + id
                + " Content : " + content;
    }
}
