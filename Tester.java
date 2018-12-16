import communication.model.NodeAddress;

/**
 * Test util for quick testing from a single process.
 */
public class Tester {
    private static NodeAddress selfAddr(int port) {
        return new NodeAddress("127.0.0.1", port);
    }

    public static void main(String[] args) {
        new Thread(() -> Dispatcher.getMaster(selfAddr(8001), 4).run()).start();
        new Thread(() -> Dispatcher.getWorker(
                selfAddr(9001), selfAddr(8001),
                "uns0", "s0").run()).start();
        new Thread(() -> Dispatcher.getWorker(
                selfAddr(9002), selfAddr(8001),
                "uns1", "s1").run()).start();
        new Thread(() -> Dispatcher.getWorker(
                selfAddr(9003), selfAddr(8001),
                "uns2", "s2").run()).start();
        new Thread(() -> Dispatcher.getWorker(
                selfAddr(9004), selfAddr(8001),
                "uns3", "s3").run()).start();
    }
}
