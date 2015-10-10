import java.util.ArrayList;

/**
 * Class to represent a set of tokens owned by a particular node.
 * @author ketan
 */
public class Tokens {

    private ArrayList<Token> tokens;

    public Tokens() {
        tokens = new ArrayList<>();
    }

    public ArrayList<Token> getTokenList() {
        return tokens;
    }

    public void addToken(Token token) {
        tokens.add(token);
    }

    public int size() {
        return tokens.size();
    }
    
    @Override
    public String toString() {
        if(tokens == null) {
            return "Empty";
        } else {
            StringBuilder sb = new StringBuilder();
            for (Token token : tokens) {
                sb.append(token.toString());
            }
            return sb.toString();
        }
    }
}
