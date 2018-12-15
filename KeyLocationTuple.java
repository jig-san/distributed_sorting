import java.util.Arrays;

public class KeyLocationTuple {
    public byte[] key;
    public long location;

    public KeyLocationTuple(byte[]key, long location){
        this.key = Arrays.copyOf(key, key.length);
        this.location = location;
    }

    public static long EstimateSize() {
        boolean is64Bit = true;
        String arch = System.getProperty("sun.arch.data.model");
        if (arch != null) {
            if (arch.contains("32")) {
                // If exists and is 32 bit then we assume a 32bit JVM
                is64Bit = false;
            }
        }
        // java class header, size of key, size of long
        return (is64Bit ? 32 : 32) + (is64Bit ? 16 : 8)  + (is64Bit ? 8 : 8);
    }
}
