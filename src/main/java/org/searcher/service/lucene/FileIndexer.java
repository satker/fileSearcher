package org.searcher.service.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.List;

/**
 * Default Indexer that we will use in tutorial
 * Be default it will use RussianAnalyzer to analyze text
 */
public class FileIndexer {

    private static final Analyzer analyzer = new RussianAnalyzer();
    public static Directory directory;

    /**
     * Indexing documents using provided Analyzer
     *
     * @param create to decide create new or append to previous one
     */
    public Directory index(final Boolean create, List<Document> documents)
            throws IOException {
        directory = new RAMDirectory();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        if (create) {
            // Create a new index in the directory, removing any
            // previously indexed documents:
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        } else {
            // Add new documents to an existing index:
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        }

        IndexWriter w = new IndexWriter(directory, indexWriterConfig);
        w.addDocuments(documents);
        w.close();
        return directory;
    }
}