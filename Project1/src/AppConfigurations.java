import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Class to read configurations and setup necessary environment variables
 * @author ketan
 */
public class AppConfigurations {

    private static final char COMMENT = '#';
    private static final String NETID = "kkj140030";

    private static HashMap<Integer, NodeInfo> nodeMap = new HashMap<>();
    private static HashMap<Integer, Tokens> tokenMap = new HashMap<>();

    /**
     * Reads the configuration file and sets up environment such as machine
     * name, port number, other machines in the system
     */
    public static void setupApplicationEnvironment(String configFileName) {

        Scanner lineScanner = null;
        try (
                Scanner scanner = new Scanner(new File(configFileName))
            ) {

            String input = getNextValidInputLine(scanner);

            lineScanner = new Scanner(input);
            int totalNodes = lineScanner.nextInt();
            lineScanner.close();

            input = getNextValidInputLine(scanner);

            lineScanner = new Scanner(input);
            lineScanner.next();
            String machineName = lineScanner.next();
            int port = lineScanner.nextInt();
            NodeInfo info = new NodeInfo(machineName, port);
            nodeMap.put(0, info);

            for (int i = 1; i < totalNodes; i++) {
                String line = scanner.nextLine();

                lineScanner = new Scanner(line);
                lineScanner.next();
                machineName = lineScanner.next();
                port = lineScanner.nextInt();

                info = new NodeInfo(machineName, port);
                nodeMap.put(i, info);
            }
            lineScanner.close();

            input = getNextValidInputLine(scanner);

            lineScanner = new Scanner(input);
            int id = lineScanner.nextInt();
            ArrayList<String> path = new ArrayList<>();
            while (lineScanner.hasNext()) {
                String pathElem = lineScanner.next();
                if(pathElem.charAt(0) == COMMENT) {
                    break;
                }
                path.add(pathElem);
            }
            lineScanner.close();
            int j = 0;
            String[] pathArr = new String[path.size()];
            for (String string : path) {
                pathArr[j] = string;
                j++;
            }
            Token token = new Token(id, pathArr);
            Tokens tokens = new Tokens();
            tokens.addToken(token);
            tokenMap.put(id, tokens);

            while(scanner.hasNext()){
                String line = scanner.nextLine();
                lineScanner = new Scanner(line);
                id = lineScanner.nextInt();
                ArrayList<String> tokenPath = new ArrayList<>();
                while (lineScanner.hasNext()) {
                    String pathElem = lineScanner.next();
                    if(pathElem.charAt(0) == COMMENT) {
                        lineScanner.nextLine();
                        break;
                    }
                    tokenPath.add(pathElem);
                }
                lineScanner.close();
                j = 0;
                pathArr = new String[tokenPath.size()];
                for (String string : tokenPath) {
                    pathArr[j] = string;
                    j++;
                }
                token = new Token(id, pathArr);
                if(tokenMap.containsKey(id)) {
                    tokens = tokenMap.get(id);
                }
                else {
                    tokens = new Tokens();
                }
                tokens.addToken(token);
                tokenMap.put(id, tokens);
            }

        } catch (IOException e) {
            
        } finally {
            lineScanner.close();
        }

    }

    /**
     * Skips the commented or blank lines and returns next valid input line
     * @param scanner
     * @return valid input line
     */
    public static String getNextValidInputLine(Scanner scanner) {
        String input = null;
        while (scanner.hasNext()) {
            input = scanner.nextLine();
            if(input.isEmpty()) {
                continue;
            }
            if(input.charAt(0) != COMMENT) {
                break;
            }
       }
        return input;
    }

    public static HashMap<Integer, NodeInfo> getNodeMap() {
        return nodeMap;
    }

    public static Tokens getNodeTokens(final int index) {
        return tokenMap.get(index);
    }

    public static String getNetId() {
        return NETID;
    }

    public static String getLogFileName(final int nodeId, final String configFileName) {
        String fileName = Paths.get(configFileName).getFileName().toString();
        return String.format("%s-%s-%s.out", 
                fileName.substring(0, fileName.lastIndexOf('.')),
                NETID,
                nodeId);
    }
}
