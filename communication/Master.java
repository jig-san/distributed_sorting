package communication;

import communication.model.NodeAddress;
import communication.model.NodeData;
import util.Arrayer;
import util.DuplexSocket;
import util.Logger;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This is a node, that serves as a registry of all worker nodes.
 * Pass its address and port to every node, so that they can exchange
 * addresses with this service.
 */
public class Master extends Node {
    private final ArrayList<NodeAddress> nodes;
    private final ArrayList<DuplexSocket> sockets;
    private final int nodesCount;

    public Master(NodeAddress ownAddress, int nodesCount) {
        super(ownAddress);
        this.nodesCount = nodesCount;
        nodes = Arrayer.createEmptyArray(nodesCount);
        sockets = Arrayer.createEmptyArray(nodesCount);
    }

    private void listenForNode() {
        try {
            DuplexSocket client = this.acceptClient();
            Logger.log("Connection accepted from node IP %s", client.getIpAddr());
            NodeData nodeData = client.receive(NodeData.class);
            if (nodeData.isReceiverNode()) {
                this.nodes.set(nodeData.getNodeIndex(), nodeData.getAddress());
            }
            this.sockets.set(nodeData.getNodeIndex(), client);
            Logger.log("Saved node %s, total", nodeData, this.nodes.size());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to accept node connection", e);
        }
    }

    private void sendAddressesToNodes() {
        for (int i = 0; i < this.nodes.size(); i++) {
            Logger.log("Output stream created for %s",  this.nodes.get(i));
            try {
                sockets.get(i).send(this.nodes);
                sockets.get(i).close();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to send nodes list to all the nodes", e);
            }
            Logger.log("Nodes list sent");
        }
    }

    @Override
    protected void runNode() {
        for (int i = 0; i < nodesCount; i++) {
            this.listenForNode();
        }
        this.sendAddressesToNodes();
    }
}
