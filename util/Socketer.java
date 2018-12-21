package util;

import communication.model.NodeAddress;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Simple socket manager that returns DuplexSocket.
 */
public class Socketer {
    public static DuplexSocket client(NodeAddress addr) throws IOException {
        return new DuplexSocket(new Socket(addr.getIpAddr(), addr.getPort()), true);
    }
    public static Socketer server(int port) throws IOException {
        return new Socketer(new ServerSocket(port, 512));
    }

    private final ServerSocket serverSocket;
    private Socketer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
    public DuplexSocket accept() throws IOException {
        return new DuplexSocket(serverSocket.accept(), false);
    }
    public String getIpAddr() {
        return serverSocket.getInetAddress().getHostAddress();
    }
    public int getPort() {
        return serverSocket.getLocalPort();
    }
}
