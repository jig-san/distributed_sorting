package communication;

import communication.model.NodeAddress;
import communication.model.NodeData;
import util.DuplexSocket;
import util.Logger;
import util.Socketer;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This a generic job, that uses Master to get addresses of all other nodes,
 * and then creates a socket and a thread for each of them.
 */
abstract public class Worker extends Node {
    private ArrayList<NodeAddress> nodes;
    private final ArrayList<DuplexSocket> nodeSockets = new ArrayList<>();
    private final DuplexSocket masterSocket;

    public Worker(NodeAddress ownAddress, NodeAddress masterAddress) {
        super(ownAddress);
        try {
            this.masterSocket = Socketer.client(masterAddress);
        } catch (IOException e) {
            throw new IllegalStateException("Worker failed to connect to master", e);
        }
    }

    protected ArrayList<DuplexSocket> getSockets() {
        return this.nodeSockets;
    }

    @SuppressWarnings("unchecked")
    private void exchangeSockets() {
        try {
            Logger.log("Sending address to master");
            masterSocket.send(new NodeData(getOwnAddress(), isReveiverNode(), isProducerNode()));
            Logger.log("Address send to master");
            this.nodes = masterSocket.receive(ArrayList.class);
            Logger.log("Received object from master: %s", this.nodes);
            masterSocket.close();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to exchange with master", e);
        }
    }

    private boolean isReveiverNode() {
        return true;
    }

    private boolean isProducerNode() {
        return true;
    }

    /**
     * Overridable method, which does the work with incoming data.
     */
    abstract protected void listenForRecords(DuplexSocket socket);

    /**
     * Overridable method, which does the work before threads are started.
     */
    abstract protected void preProcess();

    /**
     * Overridable method, which does the work with sockets.
     */
    abstract protected void process();

    /**
     * Overridable method, which does the work after sockets are gone.
     */
    abstract protected void postProcess();

    private void createSockets() {
        try {
            boolean pastSelf = false;
            for (NodeAddress node : nodes) {
                if (node.equals(getOwnAddress())) {
                    pastSelf = true;
                    nodeSockets.add(null);
                    continue;
                }
                if (pastSelf) {
                    Logger.log("Opening socket with %s", node);
                    nodeSockets.add(Socketer.client(node));
                    Logger.log("Opened socket with %s", node);
                } else {
                    Logger.log("Waiting for socket from %s", node);
                    nodeSockets.add(acceptClient());
                    Logger.log("Got socket from %s", node);
                }
            }
            Logger.log("Opened all the sockets");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create all the sockets", e);
        }
    }

    private void spawnThreadsAndWork() {
        ArrayList<Thread> threads = new ArrayList<>();
        for (DuplexSocket nodeSocket : nodeSockets) {
            if (nodeSocket == null) {
                continue;
            }
            Thread job = new Thread(() -> listenForRecords(nodeSocket));
            threads.add(job);
            job.start();
        }
        process();
        for (Thread job : threads) {
            while (job.isAlive()) {
                try {
                    job.join();
                } catch (InterruptedException e) {
                    // Will just try again
                }
            }
        }
    }

    private void closeSockets() {
        for (DuplexSocket nodeSocket : nodeSockets) {
            if (nodeSocket == null) {
                continue;
            }
            try {
                nodeSocket.close();
            } catch (IOException e) {
                // Does not matter much anymore
            }
        }
    }

    @Override
    protected void runNode() {
        exchangeSockets();
        createSockets();
        preProcess();
        spawnThreadsAndWork();
        closeSockets();
        postProcess();
    }
}
