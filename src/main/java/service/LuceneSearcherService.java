package service;

import static java.nio.file.Files.readAllLines;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import service.lucene.BasicSearchExamples;
import service.lucene.MessageIndexer;
import service.lucene.MessageToDocument;

public class LuceneSearcherService {

  public boolean isaTextInFile(File file, String whatSearch, boolean isFuzzy)
      throws Exception {
    MessageIndexer messageIndexer = new MessageIndexer();

    List<Document> documents = new ArrayList<>();
    List<String> lines = readAllLines(Paths.get(file.getAbsolutePath()),
        Charset.forName("ISO-8859-1"));

    int count = 1;
    for (String line : lines) {
      documents.add(MessageToDocument.createWith(String.valueOf(count), line));
      count++;
    }

    messageIndexer.index(false, documents);

    BasicSearchExamples basicSearchExamples = new BasicSearchExamples(
        DirectoryReader.open(MessageIndexer.directory));
    boolean result = isFuzzy ? basicSearchExamples.fuzzySearch(whatSearch, "body", 10)
        : basicSearchExamples.searchInBody(whatSearch, 10);
    return result;
  }

}
