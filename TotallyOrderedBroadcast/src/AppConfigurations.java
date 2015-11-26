import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
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

    /**
     * Reads the configuration file and sets up environment such as machine
     * name, port number, other machines in the system
     */
    public static void setupApplicationEnvironment(String configFileName, int id) {

        TobGlobals.id = id;
        MutexGlobals.id = id;
        Scanner lineScanner = null;
        try (
                Scanner scanner = new Scanner(new File(configFileName))
            ) {

            String input = getNextValidInputLine(scanner);

            lineScanner = new Scanner(input);
            int numNodes = lineScanner.nextInt();
            TobGlobals.numNodes = numNodes;
            MutexGlobals.numNodes = numNodes;
            int numMessages = lineScanner.nextInt();
            TobGlobals.numMessages = numMessages;
            int delay = lineScanner.nextInt();
            TobGlobals.delay = delay;
            lineScanner.close();

            input = getNextValidInputLine(scanner);

            lineScanner = new Scanner(input);
            int nodeNumber = lineScanner.nextInt();
            String machineName = lineScanner.next();
            int port = lineScanner.nextInt();
            NodeInfo info = new NodeInfo(machineName, port);
            nodeMap.put(nodeNumber, info);

            for (int i = 1; i < numNodes; i++) {
                String line = scanner.nextLine();

                lineScanner = new Scanner(line);
                nodeNumber = lineScanner.nextInt();
                machineName = lineScanner.next();
                port = lineScanner.nextInt();

                info = new NodeInfo(machineName, port);
                nodeMap.put(nodeNumber, info);
            }
            lineScanner.close();
        } catch (IOException e) {
            e.printStackTrace();
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

    public static String getNetId() {
        return NETID;
    }

    public static String getLogFileName(final int nodeId, final String configFileName) {
        String fileName = Paths.get(configFileName).getFileName().toString();
        return String.format("%s-%s.out", 
                fileName.substring(0, fileName.lastIndexOf('.')), nodeId);
    }

}
