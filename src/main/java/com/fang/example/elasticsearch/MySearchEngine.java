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
//        // file可以读取
//        if (file.canRead()) {
//            if (file.isDirectory()) { // 如果file是一个目录(该目录下面可能有文件、目录文件、空文件三种情况)
//                String[] files = file.list(); // 获取file目录下的所有文件(包括目录文件)File对象，放到数组files里
//                // 如果files!=null
//                if (files != null) {
//                    for (int i = 0; i < files.length; i++) { // 对files数组里面的File对象递归索引，通过广度遍历
//                        createIndex(writer, new File(file, files[i]));
//                    }
//                }
//            }
//            else { // 到达叶节点时，说明是一个File，而不是目录，则为该文件建立索引
//                try {
//                    writer.addDocument(FileDocument.Document(file));
//                }
//                catch (FileNotFoundException fnfe) {
//                    fnfe.printStackTrace();
//                }
//            }
//        }
//    }

//    public void searchContent(String type,String keyword){    // 根据指定的检索内容类型type，以及检索关键字keyword进行检索操作
//        try {
//            IndexSearcher searcher = new IndexSearcher(this.indexPath);    // 根据指定路径，构造一个IndexSearcher检索器
//            Term term = new Term(type,keyword);    // 创建词条
//            Query query = new TermQuery(term);    //   创建查询
//            Date startTime = new Date();
//            DocsEnum termDocs = searcher.getIndexReader().(term);    // 执行检索操作
//            while(termDocs.next()){    //   遍历输出根据指定词条检索的结果信息
//                System.out.println("搜索的该关键字【"+keyword+"】在文件\n"+searcher.getIndexReader().document(termDocs.doc()));
//                System.out.println("中，出现过 "+termDocs.freq()+" 次");
//            }
//            Date finishTime = new Date();
//            long timeOfSearch = finishTime.getTime() - startTime.getTime();    //   计算检索花费时间
//            System.out.println("本次搜索所用的时间为 "+timeOfSearch+" ms");
//        } catch (CorruptIndexException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//这里引用了import org.apache.lucene.demo.FileDocument，在创建Field的时候，为每个Field都设置了三种属性：path、modified、contents。在检索的时候，只要指定其中的一个就可以从索引中检索出来。
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
