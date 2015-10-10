import java.io.Serializable;
import java.util.Arrays;

/**
 * Serializable class to represent the token.
 * It also keeps track of its own sum, path, current index on the path. 
 * @author ketan
 */
public class Token implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String SEPARATOR = " -> ";

    private final int originator;
    private final String[] path;

    private int id;
    private int currentIndex;
    private int sum;
    private boolean isFinished;

    public Token(
            final int originator,
            final String[] path) {

        this.originator = originator;
        this.path = path;
        this.currentIndex = -1;
        this.sum = 0;
        this.isFinished = false;
        if(path.length == 0) {
            this.isFinished = true;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void addToSum(int value) {
        sum += value;
    }

    public int getSum() {
        return sum;
    }

    /**
     * Checks if the current node is the last node in the path
     * @return true if current node is the last one in the path, else false
     */
    private boolean isLastNode() {
        if(currentIndex > path.length - 1) {
            isFinished = true;
        }
        return isFinished;
    }

    /**
     * Returns next node in the path, if exists.
     * If end of the path is reached then returns the originator of the token
     * @return next node
     */
    public int getNextPathNode() {
        currentIndex++;
        return isLastNode()
                ? originator
                : Integer.parseInt(path[currentIndex]);
    }

    /**
     * Checks if the token has traversed its complete path
     * @return true if path is completed, else false
     */
    public boolean isConsumed() {
        return isFinished;
    }

    @Override
    public String toString() {
        return "Originator: " + originator
                + " Token: " + Arrays.asList(path)
                + " Sum: " + sum + "\n";
    }

    public String getPrintablePath() {
        StringBuilder builder = new StringBuilder()
                                    .append(originator).append(SEPARATOR);
        for (String node : path) {
            builder.append(node).append(SEPARATOR);
        }
        builder.append(originator);
        return builder.toString();
    }

}
