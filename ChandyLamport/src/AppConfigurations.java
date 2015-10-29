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
    private static ArrayList<Integer> neighborNodes = new ArrayList<>();

    /**
     * Reads the configuration file and sets up environment such as machine
     * name, port number, other machines in the system
     */
    public static void setupApplicationEnvironment(String configFileName, int id) {

        Globals.id = id;
        Scanner lineScanner = null;
        try (
                Scanner scanner = new Scanner(new File(configFileName))
            ) {

            String input = getNextValidInputLine(scanner);

            lineScanner = new Scanner(input);
            int clusterSize = lineScanner.nextInt();
            Globals.clusterSize = clusterSize;
            Globals.vectorClock = new int[clusterSize];
            int minPerActive = lineScanner.nextInt();
            Globals.minPerActive = minPerActive;
            int maxPerActive = lineScanner.nextInt();
            Globals.maxPerActive = maxPerActive;
            int minSendDelay = lineScanner.nextInt();
            Globals.minSendDelay = minSendDelay;
            int snapshotDelay = lineScanner.nextInt();
            Globals.snapshotDelay = snapshotDelay;
            int maxNumber = lineScanner.nextInt();
            Globals.maxNumber = maxNumber;
            lineScanner.close();

            input = getNextValidInputLine(scanner);

            lineScanner = new Scanner(input);
            int nodeNumber = lineScanner.nextInt();
            String machineName = lineScanner.next();
            int port = lineScanner.nextInt();
            NodeInfo info = new NodeInfo(machineName, port);
            nodeMap.put(nodeNumber, info);

            for (int i = 1; i < clusterSize; i++) {
                String line = scanner.nextLine();

                lineScanner = new Scanner(line);
                nodeNumber = lineScanner.nextInt();
                machineName = lineScanner.next();
                port = lineScanner.nextInt();

                info = new NodeInfo(machineName, port);
                nodeMap.put(nodeNumber, info);
            }
            lineScanner.close();

            input = getNextValidInputLine(scanner);

            int lineNumber = 0;
            ArrayList<Integer> neighbors = new ArrayList<>();
            while (input != null) {
                lineScanner = new Scanner(input);
                if (lineNumber != id) {
                    while (lineScanner.hasNext()) {
                        String neighbor = lineScanner.next();
                        if (neighbor.charAt(0) == COMMENT) {
                            break;
                        }
                        int neighborId = Integer.parseInt(neighbor);
                        if (neighborId == id && !neighbors.contains(lineNumber) && lineNumber != id) {
                            neighbors.add(lineNumber);
                        }
                    }
                } else {
                    while (lineScanner.hasNext()) {
                        String neighbor = lineScanner.next();
                        if (neighbor.charAt(0) == COMMENT) {
                            break;
                        }
                        int neighborId = Integer.parseInt(neighbor);
                        if (!neighbors.contains(neighborId) && neighborId != id) {
                            neighbors.add(neighborId);
                        }
                    }
                }
//                input = scanner.nextLine();
                input = getNextValidInputLine(scanner);
                lineScanner.close();
                lineNumber++;
            }
            neighborNodes = neighbors;
            Globals.neighborCount = neighbors.size();

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

    public static ArrayList<Integer> getNeighborNodes() {
        return neighborNodes;
    }
}
