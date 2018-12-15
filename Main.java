import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static final int DEFAULT_KEY_SIZE = 10;
    private static final int DEFAULT_RECORD_SIZE = 90;
    public static final int DEFAULT_MAX_TMP_FILES = 512;

    private static long estimateAvailableMemory() {
        System.gc();
        // http://stackoverflow.com/questions/12807797/java-get-available-memory
        Runtime r = Runtime.getRuntime();
        long allocatedMemory = r.totalMemory() - r.freeMemory();
        return r.maxMemory() - allocatedMemory;
    }

    public static long estimateBestSizeOfBlocks(final long sizeoffile,
                                                final int maxtmpfiles, final long maxMemory) {
        long blocksize = sizeoffile / maxtmpfiles
                + (sizeoffile % maxtmpfiles == 0 ? 0 : 1);

        if (blocksize < maxMemory / 2) {
            blocksize = maxMemory / 2;
        }
        return blocksize;
    }


    public static Comparator<KeyLocationTuple> defaultKeyLocationComparator = new Comparator<KeyLocationTuple>() {
        @Override
        public int compare(KeyLocationTuple left, KeyLocationTuple right) {
            for (int i = 0, j = 0; i < left.key.length && j < right.key.length; i++, j++) {
                int a = (left.key[i] & 0xff);
                int b = (right.key[j] & 0xff);
                if (a != b) {
                    return a - b;
                }
            }
            return left.key.length - right.key.length;
        }
    };

    public static Comparator<byte[]> defaultByteArrayComparator = new Comparator<byte[]>() {
        @Override
        public int compare(byte[] left, byte[] right) {
            for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
                int a = (left[i] & 0xff);
                int b = (right[j] & 0xff);
                if (a != b) {
                    return a - b;
                }
            }
            return left.length - right.length;
        }
    };

    public static void main(String[] args) throws IOException {
        executeSort("testfile.crc.1G","sorted.file.out");
    }

    public static void executeSort(String inputFileName, String outputFileName) throws IOException {
        System.out.printf("Running external sorting with files: [%s] -> [%s]\n", inputFileName, outputFileName);
        List<File> l = sortFile(
                new File(inputFileName),
                defaultKeyLocationComparator,
                DEFAULT_MAX_TMP_FILES,
                new File("output")
        );
        System.out.printf("Merging of temporary files started.\n");
        System.out.printf("Merged and sorted files to [%d] rows.\n", mergeSortedFiles(l, new File(outputFileName), defaultByteArrayComparator));
    }


    public static List<File> sortFile(File file, Comparator<KeyLocationTuple> cmp, int maxTmpFiles, File tmpDirectory)
            throws IOException {
        FileInputStream is = new FileInputStream(file);

        final long datalength = file.length();


        List<File> files = new ArrayList<>();
        long blocksize = estimateBestSizeOfBlocks(datalength,
                maxTmpFiles, estimateAvailableMemory());

        List<KeyLocationTuple> tmplist = new ArrayList<>();

        long recordInMemSize = KeyLocationTuple.EstimateSize();
        System.out.printf("Splitting and sorting started:\n" +
                "Max numbers of blocks [%d]\n" +
                "Max size per block [%d] bytes\n" +
                "Size per record [%d] bytes\n", maxTmpFiles, blocksize, recordInMemSize);
        byte[] data = new byte[DEFAULT_KEY_SIZE];
        int bytesRead = 0;
        long currentOffset = 0;
        try {
            while(bytesRead != -1) {
                long currentblocksize = 0;

                while ((currentblocksize < blocksize) && ((bytesRead = is.read(data)) != -1)) {
                    currentOffset += DEFAULT_KEY_SIZE;
                    KeyLocationTuple klt = new KeyLocationTuple(data, currentOffset);
                    tmplist.add(klt);
                    currentOffset += DEFAULT_RECORD_SIZE;
                    is.skip(DEFAULT_RECORD_SIZE);
                    currentblocksize += recordInMemSize;
                }
                files.add(sortAndSave(tmplist, cmp, tmpDirectory, is));
                is.getChannel().position(currentOffset);
                tmplist.clear();
            }
        } catch (EOFException oef) {
            if (tmplist.size() > 0) {
                files.add(sortAndSave(tmplist, cmp, tmpDirectory, is));
                tmplist.clear();
            }
        }

        return files;

    }

    public static File sortAndSave(List<KeyLocationTuple> tmplist,
                                   Comparator<KeyLocationTuple> cmp, File tmpdirectory, FileInputStream is) throws IOException {
        //tmplist.sort(cmp);
        tmplist = tmplist.parallelStream().sorted(cmp).collect(Collectors.toCollection(ArrayList::new));
        File newtmpfile = File.createTempFile("tmp_sort_",
                "", tmpdirectory);
        newtmpfile.deleteOnExit();

        try (FileOutputStream stream = new FileOutputStream(newtmpfile)) {
            byte[] record = new byte[DEFAULT_RECORD_SIZE];
            int bytesRead = 0;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            for (KeyLocationTuple kl : tmplist) {
                outputStream.write(kl.key);
                is.getChannel().position(kl.location);
                bytesRead = is.read(record);
                if (bytesRead != DEFAULT_RECORD_SIZE){
                    break;
                }
                outputStream.write(record);
                stream.write(outputStream.toByteArray());
                outputStream.reset();
            }
        }

        return newtmpfile;
    }

    public static long mergeSortedFiles(List<File> files, File outputfile,
                                        final Comparator<byte[]> cmp) throws IOException {
        ArrayList<BinaryFileBuffer> bfbs = new ArrayList<>();
        for (File f : files) {
            BinaryFileBuffer bfb = new BinaryFileBuffer(new FileInputStream(f), DEFAULT_KEY_SIZE, DEFAULT_RECORD_SIZE);
            bfbs.add(bfb);
        }

        return mergeSortedFiles(new FileOutputStream(outputfile), cmp, bfbs);
    }

    public static long mergeSortedFiles(FileOutputStream outputStream,
                                        final Comparator<byte[]> cmp,
                                        List<BinaryFileBuffer> buffers) throws IOException {

        PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<>(
                11, new Comparator<BinaryFileBuffer>() {
            @Override
            public int compare(BinaryFileBuffer i,
                               BinaryFileBuffer j) {
                return cmp.compare(i.peekRecordKey(), j.peekRecordKey());
            }
        });

        for (BinaryFileBuffer bfb : buffers) {
            if (!bfb.empty()) {
                pq.add(bfb);
            }
        }

        long rowcounter = 0;

        try {
                while (pq.size() > 0) {
                    BinaryFileBuffer bfb = pq.poll();

                    outputStream.write(bfb.pop());
                    ++rowcounter;
                    if (bfb.empty()) {
                        bfb.close();
                    } else {
                        pq.add(bfb);
                    }
                }
        } finally {
            outputStream.close();
            for (BinaryFileBuffer bfb : pq) {
                bfb.close();
            }
        }
        return rowcounter;

    }
}