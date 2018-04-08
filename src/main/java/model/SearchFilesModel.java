package model;

/*
Найденные файлы по искомым условиям
 */
public class SearchFilesModel {

  private volatile String name;

  public String getName() {
    return name;
  }

  private volatile int id;

  public int getId() {
    return id;
  }

  public SearchFilesModel(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public SearchFilesModel() {
  }
}
