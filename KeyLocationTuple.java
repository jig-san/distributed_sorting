import java.util.Arrays;

public class KeyLocationTuple {
    public byte[] key;
    public long location;

    public KeyLocationTuple(byte[]key, long location){
        this.key = Arrays.copyOf(key, key.length);
        this.location = location;
    }

    public static long EstimateSize(int keySize) {
        if (keySize < 1) keySize = 0;
        boolean is64Bit = true;
        String arch = System.getProperty("sun.arch.data.model");
        if (arch != null) {
            if (arch.contains("32")) {
                is64Bit = false;
            }
        }


        long keyMemorySize;
        if (is64Bit) {
            int modKeySize = (keySize - 1) / 8;
            keyMemorySize = 8 + 8 * (modKeySize);
        } else {
            keySize += 4;
            int modKeySize = (keySize - 1) / 8;
            keyMemorySize = 8 * (modKeySize);
        }


        // java class header, size of key, size of long
        return (is64Bit ? 32 : 32) + keyMemorySize  + (is64Bit ? 8 : 8);
    }
}
