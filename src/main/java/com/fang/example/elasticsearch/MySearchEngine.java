package com.fang.example.elasticsearch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.codecs.SegmentInfoReader;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * Created by andy on 4/24/16.
 */
public class MySearchEngine {

    private File file;
    private String indexPath;

    public File getFile() {
        return file;
    }
    public void setFile(File file) {
        this.file = file;
    }
    public String getIndexPath() {
        return indexPath;
    }
    public void setIndexPath(String indexPath) {
        this.indexPath = indexPath;
    }



//    public void createIndex(IndexWriter writer, File file) throws IOException {
//
//        if (file.canRead()) {
//            if (file.isDirectory()) {
//                String[] files = file.list();
//                // 如果files!=null
//                if (files != null) {
//                    for (int i = 0; i < files.length; i++) {
//                        createIndex(writer, new File(file, files[i]));
//                    }
//                }
//            }
//            else {
//                try {
//                    writer.addDocument(FileDocument.Document(file));
//                }
//                catch (FileNotFoundException fnfe) {
//                    fnfe.printStackTrace();
//                }
//            }
//        }
//    }

//    public void searchContent(String type,String keyword){
//        try {
//            IndexSearcher searcher = new IndexSearcher(this.indexPath);
//            Term term = new Term(type,keyword);
//            Query query = new TermQuery(term);
//            Date startTime = new Date();
//            DocsEnum termDocs = searcher.getIndexReader().(term);
//            while(termDocs.next()){
//                System.out.println(""+searcher.getIndexReader().document(termDocs.doc()));
//
//            }
//            Date finishTime = new Date();
//            long timeOfSearch = finishTime.getTime() - startTime.getTime();
//
//        } catch (CorruptIndexException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

public static void main(String[] args) throws IOException {
    MySearchEngine mySearcher = new MySearchEngine();

    File file =new File("/Users/andy/Documents/estest");
    IndexReader r = IndexReader.open(FSDirectory.open(file));

    int num = r.numDocs();
    for ( int i = 0; i < num; i++)
    {
//        if ( ! r.( i))
//        {
            Document d = r.document( i);
            System.out.println( "d=" +d);
//        }
    }
    r.close();



//    final String entriesFileName = IndexFileNames.segmentFileName(
//            IndexFileNames.stripExtension(name), "",
//            IndexFileNames.COMPOUND_FILE_ENTRIES_EXTENSION);

//    String indexPath = "E:\\Lucene\\myindex";
//    File file = new File("E:\\Lucene\\txt");
//    mySearcher.setIndexPath(indexPath);
//    mySearcher.setFile(file);
//    IndexWriter writer;
//    try {
//        writer = new IndexWriter(mySearcher.getIndexPath(), new StandardAnalyzer(), true);
//        mySearcher.createIndex(writer, mySearcher.getFile());
//        mySearcher.searchContent("contents", "server");
//    } catch (CorruptIndexException e) {
//        e.printStackTrace();
//    } catch (LockObtainFailedException e) {
//        e.printStackTrace();
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
}


}
