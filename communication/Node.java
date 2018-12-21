package communication;

import communication.model.NodeAddress;
import util.DuplexSocket;
import util.Logger;
import util.Socketer;

import java.io.IOException;

/**
 * This is a generic job, that can listen to incoming sockets.
 */
abstract class Node {
    private final Socketer serverSocket;
    private final NodeAddress address;
    Node(NodeAddress address) {
        this.address = address;
        try {
            this.serverSocket = Socketer.server(address.getPort());
        } catch (IOException e) {
            throw new IllegalStateException("Node failed to start listening", e);
        }
    }

    /**
     * This is the task, performed by this node.
     */
    abstract protected void runNode();

    public void run() {
        Logger.log("Running %s on %s:%s (given %s)",
                this.getClass().getName(), this.serverSocket.getIpAddr(), this.serverSocket.getPort(), address);
        runNode();
    }

    DuplexSocket acceptClient() {
        try {
            return this.serverSocket.accept();
        } catch (IOException e) {
            throw new IllegalStateException("Node failed to accept client", e);
        }
    }

    NodeAddress getOwnAddress() {
        return address;
    }
}
