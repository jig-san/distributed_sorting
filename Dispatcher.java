import communication.Master;
import communication.Worker;
import communication.model.NodeAddress;
import sorting.ExternalSort;
import util.DuplexSocket;
import util.Logger;

import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Main job of distributed sorting, sends records to nodes and saved received nodes to a file.
 */
public class Dispatcher extends Worker {
    private final String inputFile;
    private final String outputFile;
    private final String tmpOutputFile;
    private BigInteger spanSize;
    private BufferedOutputStream outputStream;

    static Master getMaster(NodeAddress masterAddress, int nodesCount) {
        return new Master(masterAddress, nodesCount);
    }

    static Dispatcher getWorker(NodeAddress ownAddress, NodeAddress masterAddress, String inputFile, String outputFile) {
        return new Dispatcher(ownAddress, masterAddress, inputFile, outputFile);
    }

    private Dispatcher(NodeAddress ownAddress, NodeAddress masterAddress, String inputFile, String outputFile) {
        super(ownAddress, masterAddress);
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.tmpOutputFile = outputFile != null ? outputFile + ".tmp" : null;
    }

    private static BigInteger getSpanSize(int nodeNumber) {
        return BigInteger.valueOf(2).pow(80).divide(BigInteger.valueOf(nodeNumber));
    }

    @Override
    protected void listenForRecords(DuplexSocket socket) {
        while (true) {
            try {
                byte[] data = socket.receive(byte[].class);
                if (data.length == 0) {
                    Logger.log("Received EOF from node");
                    break;
                }
                processOwnRecord(data);
            } catch (IOException e) {
                throw new IllegalStateException("Node failed to provide data", e);
            }
        }
    }

    @Override
    protected void preProcess() {
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(new File(tmpOutputFile)));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Failed to create output file", e);
        }
        spanSize = getSpanSize(getSockets().size());
    }

    private int getNodeIndexForRecord(byte[] record) {
        return new BigInteger(1, Arrays.copyOfRange(record, 0, 10)).divide(spanSize).intValue();
    }

    @Override
    protected void process() {
        File file = new File(inputFile);
        try (RandomAccessFile data = new RandomAccessFile(file, "r")) {
            for (long i = 0, len=data.length()/100; i < len; i++) {
                byte[] record = new byte[100];
                data.readFully(record);
                int nodeIndex = getNodeIndexForRecord(record);
                DuplexSocket socket = getSockets().get(nodeIndex);
                if (socket != null) {
                    processAlienRecord(socket, record);
                } else {
                    processOwnRecord(record);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading input file", e);
        }
        for (DuplexSocket socket : getSockets()) {
            try {
                if (socket != null) {
                    socket.send(new byte[]{});
                }
            } catch (IOException e) {
                throw new IllegalStateException("Node failed to consume eof symbol", e);
            }
        }
    }

    @Override
    protected void postProcess() {
        try {
            outputStream.close();
        } catch (IOException e) {
            // Does not matter anymore.
        }
        try {
            ExternalSort.executeSort(tmpOutputFile, outputFile);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to sort", e);
        }
        //noinspection ResultOfMethodCallIgnored
        new File(tmpOutputFile).delete();
    }

    private void processAlienRecord(DuplexSocket socket, byte[] record) {
        try {
            socket.send(record);
        } catch (IOException e) {
            throw new IllegalStateException("Node failed to consume data", e);
        }
    }

    private synchronized void processOwnRecord(byte[] record) {
        try {
            outputStream.write(record);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write to output file", e);
        }
    }
}
