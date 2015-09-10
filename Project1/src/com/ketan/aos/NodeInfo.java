package com.ketan.aos;

public class NodeInfo {

    private final String hostName;
    private final int portNumber;

    public NodeInfo(
            final String hostName,
            final int portNumber) {

        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPortNumber() {
        return portNumber;
    }

    @Override
    public String toString() {
        return " Host: " + hostName
                + " Port: " + portNumber;
    }
}
