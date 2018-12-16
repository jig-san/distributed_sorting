package sorting;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * A wrapper on top of a BufferedReader that keeps the record in memory.
 */
public final class BinaryFileBuffer {
    private FileInputStream is;
    private byte[] data;
    private byte[] key;
    private int keyRecordSize;
    private int dataRecordSize;
    private ByteArrayOutputStream outputStream;


    public BinaryFileBuffer(FileInputStream is, int keyRecordSize, int dataRecordSize) throws IOException {
        this.is = is;
        this.key = new byte[keyRecordSize];
        this.data = new byte[dataRecordSize];
        this.keyRecordSize = keyRecordSize;
        this.dataRecordSize = dataRecordSize;
        this.outputStream = new ByteArrayOutputStream();
        reload();
    }

    public void close() throws IOException {
        this.is.close();
    }

    public boolean empty() {
        return this.key == null && this.data == null;
    }

    public byte[] peekRecordKey() {
        return this.key;
    }

    public byte[] pop() throws IOException {
        byte[] answer = outputStream.toByteArray();
        reload();
        return answer;
    }

    private void reload() throws IOException {
        outputStream.reset();
        if (this.is.read(this.key) != this.keyRecordSize) {
            this.key = null;
            this.data = null;
            return;
        }

        if (this.is.read(this.data) != this.dataRecordSize) {
            this.key = null;
            this.data = null;
            return;
        }
        outputStream.write(this.key);
        outputStream.write(this.data);
    }
}