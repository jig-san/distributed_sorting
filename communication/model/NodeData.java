package communication.model;

import java.io.Serializable;

/**
 * Node address, as well as its properties.
 */
public class NodeData implements Serializable {
    private final NodeAddress nodeAddress;
    private final boolean isReceiverNode;
    private final boolean isProducerNode;

    public NodeData(NodeAddress address, boolean isReceiverNode, boolean isProducerNode) {
        this.nodeAddress = address;
        this.isReceiverNode = isReceiverNode;
        this.isProducerNode = isProducerNode;
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
