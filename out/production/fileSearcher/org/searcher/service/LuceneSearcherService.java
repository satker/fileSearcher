package org.searcher.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.searcher.fxml_manager.MainWindowController;
import org.searcher.open_file.GetTextFromFile;
import org.searcher.service.lucene.FileIndexer;
import org.searcher.service.lucene.FileToDocument;
import org.searcher.service.lucene.Searcher;

class LuceneSearcherService {

  static int[] isaTextInFile(File file, String whatSearch, boolean isFuzzy)
      throws Exception {
    FileIndexer fileIndexer = new FileIndexer();

    List<Document> documents = new ArrayList<>();
    List<String> lines = GetTextFromFile.getTextFromFile(file.getAbsolutePath());

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
    if (result.length != 0) {
      MainWindowController.fileAndLines.put(file.getAbsolutePath(), lines);
    }
    return result;
  }

}
