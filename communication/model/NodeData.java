package communication.model;

import java.io.Serializable;

/**
 * Node address, as well as its properties.
 */
public class NodeData implements Serializable {
    private final NodeAddress nodeAddress;
    private final boolean isReceiverNode;
    private final boolean isProducerNode;
    private final int nodeIndex;

    public NodeData(NodeAddress address, int nodeIndex, boolean isReceiverNode, boolean isProducerNode) {
        this.nodeAddress = address;
        this.nodeIndex = nodeIndex;
        this.isReceiverNode = isReceiverNode;
        this.isProducerNode = isProducerNode;
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    public NodeAddress getAddress() {
        return nodeAddress;
    }

    public boolean isReceiverNode() {
        return isReceiverNode;
    }

    @Override
    public String toString() {
        return String.format(
                "%s%s%s", this.nodeAddress.toString(), isReceiverNode ? "<" : "", isProducerNode ? "<" : "");
    }
}
