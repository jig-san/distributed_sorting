import communication.model.NodeAddress;

/**
 * Start a worker or a master.
 *
 * Usage:
 * %app% own.ip.address ownport master nodes_count
 * or
 * %app% own.ip.address ownport master.ip.address masterport input/file/path output/file/path
 */
public class Main {
    public static void main(String[] args) {
        NodeAddress ownAddress = new NodeAddress(args[0], Integer.parseInt(args[1]));
        if ("master".equals(args[2])) {
            int nodesCount = Integer.parseInt(args[3]);
            Dispatcher.getMaster(ownAddress, nodesCount).run();
        } else {
            NodeAddress masterAddress = new NodeAddress(args[2], Integer.parseInt(args[3]));
            String inputFile = args[4];
            String outputFile = args[5];
            Dispatcher.getWorker(ownAddress, masterAddress, inputFile, outputFile).run();
        }
    }
}
