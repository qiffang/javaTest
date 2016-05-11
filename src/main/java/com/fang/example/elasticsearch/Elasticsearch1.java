package com.fang.example.elasticsearch;

import junit.framework.Assert;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.CodecUtil;
import org.apache.lucene.codecs.lucene3x.Lucene3xSegmentInfoReader;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexFileNames;
import org.apache.lucene.index.SegmentCommitInfo;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.store.ChecksumIndexInput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.util.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by andy on 4/27/16.
 */
public class Elasticsearch1 {

    @Test
    public void getSegmentGen() throws IOException {
        Assert.assertEquals(5l, generationFromSegmentsFileName("segement_5"));


        read(FSDirectory.open(new File("/Users/andy/Documents/estest")), "segments_4");



    }

    public static long getLastCommitGeneration(String[] files) {
        if (files == null) {
            return -1;
        }
        long max = -1;
        for (String file : files) {
            if (file.startsWith(IndexFileNames.SEGMENTS) && !file.equals(IndexFileNames.SEGMENTS_GEN)) {
                long gen = generationFromSegmentsFileName(file);
                if (gen > max) {
                    max = gen;
                }
            }
        }
        return max;
    }

    public static  long generationFromSegmentsFileName(String file) {
        int index = file.indexOf("_");
        if (index < 0 )
            return 0;
        return Long.parseLong(file.substring(index + 1, file.length()), Character.MAX_RADIX);
    }

    public static String fileNameFromGeneration(String base, String ext, long gen) {

        StringBuilder sb = new StringBuilder(base.length() + 6 + ext.length());
        sb.append(base).append("_").append(Long.toString(gen, Character.MAX_RADIX));

        if (ext.length() > 0) {
            sb.append(".").append(ext);
        }

        return sb.toString();
    }

    /** The file format version for the segments_N codec header, up to 4.5. */
    public static final int VERSION_40 = 0;

    /** The file format version for the segments_N codec header, since 4.6+. */
    public static final int VERSION_46 = 1;

    /** The file format version for the segments_N codec header, since 4.8+ */
    public static final int VERSION_48 = 2;

    /** The file format version for the segments_N codec header, since 4.9+ */
    public static final int VERSION_49 = 3;

    public static final void read(Directory directory, String segmentFileName) throws IOException {
        boolean success = false;

        // Clear any previous segments:
//        this.clear();

        long  generation = generationFromSegmentsFileName(segmentFileName);

        long lastGeneration = generation;

        ChecksumIndexInput input = directory.openChecksumInput(segmentFileName, IOContext.READ);
        try {
            final int format = input.readInt();
            final int actualFormat;
            if (format == CodecUtil.CODEC_MAGIC) {
                // 4.0+
                actualFormat = CodecUtil.checkHeaderNoMagic(input, "segments", VERSION_40, VERSION_49);
                long version = input.readLong();
                int counter = input.readInt();
                int numSegments = input.readInt();
                if (numSegments < 0) {
                    throw new CorruptIndexException("invalid segment count: " + numSegments + " (resource: " + input + ")");
                }
                for (int seg = 0; seg < numSegments; seg++) {
                    String segName = input.readString();
                    Codec codec = Codec.forName(input.readString());
                    //System.out.println("SIS.read seg=" + seg + " codec=" + codec);
                    SegmentInfo info = codec.segmentInfoFormat().getSegmentInfoReader().read(directory, segName, IOContext.READ);
                    info.setCodec(codec);
                    long delGen = input.readLong();
                    int delCount = input.readInt();
                    if (delCount < 0 || delCount > info.getDocCount()) {
                        throw new CorruptIndexException("invalid deletion count: " + delCount + " vs docCount=" + info.getDocCount() + " (resource: " + input + ")");
                    }
                    long fieldInfosGen = -1;
                    if (actualFormat >= VERSION_46) {
                        fieldInfosGen = input.readLong();
                    }
                    long dvGen = -1;
                    if (actualFormat >= VERSION_49) {
                        dvGen = input.readLong();
                    } else {
                        dvGen = fieldInfosGen;
                    }
                    SegmentCommitInfo siPerCommit = new SegmentCommitInfo(info, delCount, delGen, fieldInfosGen, dvGen);
                    if (actualFormat >= VERSION_46) {
                        if (actualFormat < VERSION_49) {
                            // Recorded per-generation files, which were buggy (see
                            // LUCENE-5636). We need to read and keep them so we continue to
                            // reference those files. Unfortunately it means that the files will
                            // be referenced even if the fields are updated again, until the
                            // segment is merged.
                            final int numGensUpdatesFiles = input.readInt();
                            final Map<Long,Set<String>> genUpdatesFiles;
                            if (numGensUpdatesFiles == 0) {
                                genUpdatesFiles = Collections.emptyMap();
                            } else {
                                genUpdatesFiles = new HashMap<>(numGensUpdatesFiles);
                                for (int i = 0; i < numGensUpdatesFiles; i++) {
                                    genUpdatesFiles.put(input.readLong(), input.readStringSet());
                                }
                            }
                            siPerCommit.setGenUpdatesFiles(genUpdatesFiles);
                        } else {
                            siPerCommit.setFieldInfosFiles(input.readStringSet());
                            final Map<Integer,Set<String>> dvUpdateFiles;
                            final int numDVFields = input.readInt();
                            if (numDVFields == 0) {
                                dvUpdateFiles = Collections.emptyMap();
                            } else {
                                dvUpdateFiles = new HashMap<>(numDVFields);
                                for (int i = 0; i < numDVFields; i++) {
                                    dvUpdateFiles.put(input.readInt(), input.readStringSet());
                                }
                            }
                            siPerCommit.setDocValuesUpdatesFiles(dvUpdateFiles);
                        }
                    }
                    System.out.println(siPerCommit.toString());
                }
                Map<String, String> userData = input.readStringStringMap();
            } else {
                actualFormat = -1;
//                Lucene3xSegmentInfoReader.readLegacyInfos(this, directory, input, format);
//                Codec codec = Codec.forName("Lucene3x");
//                for (SegmentCommitInfo info : this) {
//                    info.info.setCodec(codec);
//                }
            }

            if (actualFormat >= VERSION_48) {
                CodecUtil.checkFooter(input);
            } else {
                final long checksumNow = input.getChecksum();
                final long checksumThen = input.readLong();
                if (checksumNow != checksumThen) {
                    throw new CorruptIndexException("checksum mismatch in segments file (resource: " + input + ")");
                }
                CodecUtil.checkEOF(input);
            }

            success = true;
        } finally {
            if (!success) {
                // Clear any segment infos we had loaded so we
                // have a clean slate on retry:
                IOUtils.closeWhileHandlingException(input);
            } else {
                input.close();
            }
        }
    }



}
