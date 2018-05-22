package org.searcher.service.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

/**
 * We will use this class to convert messages to Lucene documents
 */
public class FileToDocument {

    public static final String TITLE = "title";
    /**
     * Creates Lucene Document using two strings: body and title
     *
     * @return resulted document
     */

    private static final FieldType textIndexedType = new FieldType();

    static {
        textIndexedType.setStored(true);
        textIndexedType.setIndexOptions(IndexOptions.DOCS);
        textIndexedType.setTokenized(true);
    }

    public static Document createWith(final String titleStr, final String bodyStr) {
        final Document document = new Document();
        //index title
        Field title = new Field(TITLE, titleStr, textIndexedType);
        //index body
        Field body = new Field("body", bodyStr, textIndexedType);
        document.add(title);
        document.add(body);
        return document;
    }
}
