package sample.engine;

/*
Найденные файлы по искомым условиям
 */
public class Container {

  private volatile String name;

  public String getName() {
    return name;
  }

  private volatile int id;

  public int getId() {
    return id;
  }

  Container(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public Container() {
  }
}
