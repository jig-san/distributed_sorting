package communication.model;

import java.io.Serializable;

/**
 * Simple tuple of string IP and int port.
 */
public class NodeAddress implements Serializable {
    private final String ipAddr;
    private final int port;

    public NodeAddress(String ipAddr, int port) {
        this.ipAddr = ipAddr;
        this.port = port;
    }
    public String getIpAddr() {
        return ipAddr;
    }
    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", ipAddr, port);
    }

    @Override
    public boolean equals(Object nodeAddressObj) {
        if (!(nodeAddressObj instanceof NodeAddress)) {
            return false;
        }
        NodeAddress nodeAddress = (NodeAddress) nodeAddressObj;
        return ipAddr.equals(nodeAddress.ipAddr) && port == nodeAddress.port;
    }
}
