import java.util.Comparator;

public  class SortingComparators {
    private static final int UNSIGNED_MASK = 0xFF;

    public static Comparator<KeyLocationTuple> defaultKeyLocationComparator = (klt1, klt2) -> byteArrayCompare(klt1.key, klt2.key);

    public static Comparator<BinaryFileBuffer> defaultBinaryFileBufferComparator = (bfb1, bfb2) -> byteArrayCompare(bfb1.peekRecordKey(), bfb2.peekRecordKey());

    public static Comparator<byte[]> defaultByteArrayComparator = SortingComparators::byteArrayCompare;


    private static int byteArrayCompare(byte[] left, byte[] right) {
        int minLength = Math.min(left.length, right.length);
        for (int i = 0; i < minLength; i++) {
            int result = compare(left[i], right[i]);
            if (result != 0) {
                return result;
            }
        }
        return left.length - right.length;
    }

    private static int compare(byte a, byte b) {
        return toInt(a) - toInt(b);
    }

    private static int toInt(byte value) {
        return value & UNSIGNED_MASK;
    }

    /*
    //Old implementation of byte array comparator

    private static int byteArrayCompare(byte[] b1, byte[] b2){
        for (int i = 0, j = 0; i < b1.length && j < b2.length; i++, j++) {
            int a = (b1[i] & 0xff);
            int b = (b2[j] & 0xff);
            if (a != b) {
                return a - b;
            }
        }
        return b1.length - b2.length;
    }
    */
}
