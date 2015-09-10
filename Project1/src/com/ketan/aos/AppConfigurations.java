package com.ketan.aos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class AppConfigurations {

    private static final String CONFIG_FILE = "./config.txt";
    private static final Random INTEGER_GENERATOR = new Random();
    private static final char COMMENT = '#';
    private static final int LIMIT = 10;

    private static HashMap<Integer, NodeInfo> nodeMap = new HashMap<>();
    private static HashMap<Integer, Token> tokenMap = new HashMap<>();

    /**
     * Reads the configuration file and sets up environment such as machine
     * name, port number, other machines in the system
     */
    public static void setupApplicationEnvironment() {
        int nodeNum = 1;
        try {
            Scanner scanner = new Scanner(new File(CONFIG_FILE));
            while (scanner.hasNext()) {
                // input = scanner.next();
                // if(input.charAt(0) == COMMENT) {
                // input = scanner.nextLine();
                // continue;
                // }

                int totalNodes = scanner.nextInt();
                scanner.nextLine();

                for (int i = 0; i < totalNodes; i++) {
                    String line = scanner.nextLine();

                    Scanner lineScanner = new Scanner(line);
                    String machineName = lineScanner.next();
                    int port = lineScanner.nextInt();

                    NodeInfo info = new NodeInfo(machineName, port);
                    nodeMap.put(nodeNum, info);

                    ArrayList<String> tokenPath = new ArrayList<>();
                    while (lineScanner.hasNext()) {
                        tokenPath.add(lineScanner.next());
                    }
                    int j = 0;
                    String[] path = new String[tokenPath.size()];
                    for (String string : tokenPath) {
                        path[j] = string;
                        j++;
                    }
                    Token token = new Token(nodeNum, path);
                    tokenMap.put(nodeNum, token);
                    nodeNum++;
                }
            }

//            System.out.println(nodeMap);
//            System.out.println(tokenMap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<Integer, NodeInfo> getNodeMap() {
        return nodeMap;
    }

    public static HashMap<Integer, Token> getTokenMap() {
        return tokenMap;
    }

    public static int generateRandomNumber() {
        return INTEGER_GENERATOR.nextInt(LIMIT);
    }
}
