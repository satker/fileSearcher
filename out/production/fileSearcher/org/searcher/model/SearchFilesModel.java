package org.searcher.model;

/*
Найденные файлы по искомым условиям
 */
public class SearchFilesModel {

    private volatile String name;
    private volatile int id;

    public SearchFilesModel(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public SearchFilesModel() {
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
