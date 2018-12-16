import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ExternalSort {

    private static final int DEFAULT_KEY_SIZE = 10;
    private static final int DEFAULT_RECORD_SIZE = 90;
    private static final int DEFAULT_MAX_TMP_FILES = 512;

    // http://stackoverflow.com/questions/12807797/java-get-available-memory
    private static long estimateAvailableMemory() {
        System.gc();
        Runtime r = Runtime.getRuntime();
        long allocatedMemory = r.totalMemory() - r.freeMemory();
        return r.maxMemory() - allocatedMemory;
    }

    private static long estimateBestSizeOfBlocks(final long sizeoffile,
                                                 final int maxtmpfiles, final long maxMemory) {
        long blocksize = sizeoffile / maxtmpfiles
                + (sizeoffile % maxtmpfiles == 0 ? 0 : 1);

        if (blocksize < maxMemory / 2) {
            blocksize = maxMemory / 2;
        }
        return blocksize;
    }

    public static void executeSort(String inputFileName, String outputFileName) throws IOException {
        System.out.printf("Running external sorting with files: [%s] -> [%s]\n", inputFileName, outputFileName);
        List<File> l = sortFile(
                new File(inputFileName),
                SortingComparators.defaultKeyLocationComparator,
                DEFAULT_MAX_TMP_FILES,
                new File("output")
        );
        System.out.printf("Merging of temporary files started.\n");
        System.out.printf("Merged and sorted files to [%d] rows.\n", mergeSortedFiles(l, new File(outputFileName), SortingComparators.defaultBinaryFileBufferComparator));
    }


    private static List<File> sortFile(File file, Comparator<KeyLocationTuple> cmp, int maxTmpFiles, File tmpDirectory)
            throws IOException {
        FileInputStream is = new FileInputStream(file);

        final long datalength = file.length();


        List<File> files = new ArrayList<>();
        long blocksize = estimateBestSizeOfBlocks(datalength,
                maxTmpFiles, estimateAvailableMemory());

        List<KeyLocationTuple> tmplist = new ArrayList<>();

        long recordInMemSize = KeyLocationTuple.EstimateSize(DEFAULT_KEY_SIZE);
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

    private static File sortAndSave(List<KeyLocationTuple> tmplist,
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

    private static long mergeSortedFiles(List<File> files, File outputfile,
                                         final Comparator<BinaryFileBuffer> cmp) throws IOException {
        ArrayList<BinaryFileBuffer> bfbs = new ArrayList<>();
        for (File f : files) {
            BinaryFileBuffer bfb = new BinaryFileBuffer(new FileInputStream(f), DEFAULT_KEY_SIZE, DEFAULT_RECORD_SIZE);
            bfbs.add(bfb);
        }

        return mergeSortedFiles(new FileOutputStream(outputfile), cmp, bfbs);
    }

    private static long mergeSortedFiles(FileOutputStream outputStream,
                                         final Comparator<BinaryFileBuffer> cmp,
                                         List<BinaryFileBuffer> buffers) throws IOException {

        PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<>(11, cmp);

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