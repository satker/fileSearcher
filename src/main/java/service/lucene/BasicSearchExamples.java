package service.lucene;

import java.io.IOException;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

public class BasicSearchExamples {

  /**
   * Search in body using QueryParser
   *
   * @param toSearch string to search
   * @param limit how many results to return
   */
  private static final QueryParser queryParser = new QueryParser("body", new RussianAnalyzer());
  private final IndexReader reader;

  public BasicSearchExamples(IndexReader reader) {
    this.reader = reader;
  }

  /**
   * Search using TermQuery
   *
   * @param toSearch string to search
   * @param searchField field where to search. We have "body" and "title" fields
   * @param limit how many results to return
   */
  public void searchIndexWithTermQuery(final String toSearch, final String searchField,
                                       final int limit) throws IOException {
    final IndexSearcher indexSearcher = new IndexSearcher(reader);

    final Term term = new Term(searchField, toSearch);
    final Query query = new TermQuery(term);
    final TopDocs search = indexSearcher.search(query, limit);
    final ScoreDoc[] hits = search.scoreDocs;
    showHits(hits);
  }

  public boolean searchInBody(final String toSearch, final int limit)
      throws IOException, ParseException {
    IndexSearcher indexSearcher = new IndexSearcher(reader);

    final Query query = queryParser.parse(toSearch);
    final TopDocs search = indexSearcher.search(query, limit);
    final ScoreDoc[] hits = search.scoreDocs;
    //showHits(hits);
    return hits.length != 0 ? true : false;
  }

  /**
   * Search using FuzzyQuery.
   *
   * @param toSearch string to search
   * @param searchField field where to search. We have "body" and "title" fields
   * @param limit how many results to return
   */
  public boolean fuzzySearch(final String toSearch, final String searchField, final int limit)
      throws IOException, ParseException {
    final IndexSearcher indexSearcher = new IndexSearcher(reader);

    final Term term = new Term(searchField, toSearch);

    final int maxEdits = 2; // This is very important variable. It regulates fuzziness of the query
    final Query query = new FuzzyQuery(term, maxEdits);
    final TopDocs search = indexSearcher.search(query, limit);
    final ScoreDoc[] hits = search.scoreDocs;
    //    showHits(hits);
    return hits.length != 0 ? true : false;
  }

  private void showHits(final ScoreDoc[] hits) throws IOException {
    for (ScoreDoc hit : hits) {
      final String title = reader.document(hit.doc)
                                 .get("title");
      final String body = reader.document(hit.doc)
                                .get("body");
      System.out.println(
          "\n \t Document Id = " + hit.doc + "\n \t title = " + title + "\n \t body = " + body);
    }
  }
}
