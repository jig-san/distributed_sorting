import communication.model.NodeAddress;

/**
 * Start a worker or a master.
 *
 * Usage:
 * %app% own.ip.address ownport master nodes_count
 * or
 * %app% own.ip.address ownport nodeindex master.ip.address masterport input/file/path output/file/path
 *
 * To clarify: node has to know its own index in order to remove uncertainty in order of resulting files.
 * In other words, we want to know for each node, which part of entire result it produced.
 */
public class Main {
    public static void main(String[] args) {
        NodeAddress ownAddress = new NodeAddress(args[0], Integer.parseInt(args[1]));
        if ("master".equals(args[2])) {
            int nodesCount = Integer.parseInt(args[3]);
            Dispatcher.getMaster(ownAddress, nodesCount).run();
        } else {
            NodeAddress masterAddress = new NodeAddress(args[3], Integer.parseInt(args[4]));
            String inputFile = args[5];
            String outputFile = args[6];
            Dispatcher.getWorker(ownAddress, Integer.parseInt(args[2]), masterAddress, inputFile, outputFile).run();
        }
    }
}
