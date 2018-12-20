import communication.model.NodeAddress;

import java.util.concurrent.TimeUnit;

/**
 * Test util for quick testing from a single process.
 */
public class Tester {
    private static NodeAddress selfAddr(int port) {
        return new NodeAddress("127.0.0.1", port);
    }

    private static void runWorker(int i) {
        new Thread(() -> Dispatcher.getWorker(
                selfAddr(9001 + i), i, selfAddr(8001),
                "uns" + i, "s" + i).run()).start();
    }

    public static void main(String[] args) {
        int numWorkers = 4;
        new Thread(() -> Dispatcher.getMaster(selfAddr(8001), numWorkers).run()).start();
        for (int i = 0; i < numWorkers; i++) {
            runWorker(i);
        }
    }
}
