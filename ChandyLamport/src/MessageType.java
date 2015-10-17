import java.io.Serializable;


public enum MessageType implements Serializable {

    // For application messages, piggybacked vector clocks
    APPLICATION,

    // Marker message for Chandy-Lamport protocol, by initiator
    MARKER,

    // For indicating the reply sent by child node to marker message
    LOCAL_STATE,

    // Indicates that the node received marker message from some other parent
    // and hence message from current node was ignored
    IGNORED,

    // Finish message from snapshot initiator
    FINISH;
}
