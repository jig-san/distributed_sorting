package sorting;

import java.io.IOException;

/**
 * Test of sort.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        ExternalSort.executeSort("testfile.crc.1G","sorted.file.out");
    }
}
