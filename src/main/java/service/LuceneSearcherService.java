package service;

import static java.nio.file.Files.readAllLines;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import service.lucene.FileIndexer;
import service.lucene.FileToDocument;
import service.lucene.Searcher;

public class LuceneSearcherService {

  public static int[] isaTextInFile(File file, String whatSearch, boolean isFuzzy)
      throws Exception {
    FileIndexer fileIndexer = new FileIndexer();

    List<Document> documents = new ArrayList<>();
    List<String> lines = readAllLines(Paths.get(file.getAbsolutePath()),
        Charset.forName("ISO-8859-1"));

    int count = 0;
    for (String line : lines) {
      documents.add(FileToDocument.createWith(String.valueOf(count), line));
      count++;
    }

    fileIndexer.index(false, documents);

    Searcher.setReader(DirectoryReader.open(FileIndexer.directory));
    int[] result;
    if (isFuzzy) {
      result = Searcher.fuzzySearch(whatSearch, "body", 10);
    } else {
      result = Searcher.searchInBody(whatSearch, 10);
    }
    return result;
  }

}
