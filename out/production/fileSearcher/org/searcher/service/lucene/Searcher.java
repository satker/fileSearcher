package org.searcher.service.lucene;

import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class Searcher {

    /**
     * Search in body using QueryParser
     *
     * @param toSearch string to search
     * @param limit how many results to return
     */
    private static final QueryParser queryParser = new QueryParser("body", new RussianAnalyzer());

    private static IndexReader reader;

    public static void setReader(IndexReader reader) {
        Searcher.reader = reader;
    }

    public static int[] searchInBody(final String toSearch, final int limit)
            throws IOException, ParseException {
        IndexSearcher indexSearcher = new IndexSearcher(reader);

        final Query query = queryParser.parse(toSearch);
        final TopDocs search = indexSearcher.search(query, limit);
        final ScoreDoc[] hits = search.scoreDocs;
        return getMassiveIntFromScoreDoc(hits);
    }

    /**
     * Search using FuzzyQuery.
     *
     * @param toSearch    string to search
     * @param searchField field where to search. We have "body" and "title" fields
     * @param limit       how many results to return
     */
    public static int[] fuzzySearch(final String toSearch, final String searchField,
                                    final int limit)
            throws IOException {
        final IndexSearcher indexSearcher = new IndexSearcher(reader);

        final Term term = new Term(searchField, toSearch);

        final int maxEdits = 2; // This is very important variable. It regulates fuzziness of the query
        final Query query = new FuzzyQuery(term, maxEdits);
        final TopDocs search = indexSearcher.search(query, limit);
        final ScoreDoc[] hits = search.scoreDocs;
        return getMassiveIntFromScoreDoc(hits);
    }

    private static int[] getMassiveIntFromScoreDoc(ScoreDoc[] hits) {
        int[] indexStrWhichAreSearch = new int[0];
        if (hits.length != 0) {
            indexStrWhichAreSearch = Arrays.stream(hits)
                    .map(element -> {
                        try {
                            return reader.document(element.doc)
                                    .get("title");
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::parseInt)
                    .toArray();
        }
        return indexStrWhichAreSearch;
    }
}
