package util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Convenience util that wraps a Socket and automatically creates both Object*Streams.
 * Usage:
 * socket.send(m)
 * MyClass m = socket.receive(MyClass.class);
 */
public class DuplexSocket {
    private final Socket socket;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    DuplexSocket(Socket socket, boolean isClient) throws IOException {
        this.socket = socket;
        if (isClient) {
            this.inputStream = new ObjectInputStream(socket.getInputStream());
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        } else {
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());
        }
    }

    public void send(Object data) throws IOException {
        this.outputStream.writeObject(data);
    }

    public <T> T receive(Class<T> type) throws IOException {
        try {
            return type.cast(this.inputStream.readObject());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Programming error", e);
        }
    }

    public void close() throws IOException {
        socket.close();
    }

    public String getIpAddr() {
        return socket.getInetAddress().getHostAddress();
    }
}
