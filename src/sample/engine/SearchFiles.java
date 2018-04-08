package sample.engine;
/*
     Модуль поиска (мозг программы)
 */

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sample.Controller;

public class SearchFiles extends Controller implements Runnable {

  public SearchFiles(@NotNull String directory, String findText, @NotNull String findType) {
    this.directory = directory.trim()
                              .equals("") ? "C:\\" : directory;
    this.findText = findText;
    // если ничего не ввели выставляем по-умолчанию .log
    this.findType = findType.trim()
                            .equals("") ? "log" : findType;
  }

  private String directory; // Искомая директория
  private String findText; // Искомый текст
  private String findType; // Искомое расширение
  private int id = 1; // ID результата

  @Override
  public void run() {
    List<String> result = currentDirectories(directory);
    List<String> readyForAddingToResult = new ArrayList<>();
    while (!result.isEmpty()) {
      // Параллельные стримы для перебора коллекции
      result.parallelStream()
            .filter(Objects::nonNull)
            .forEach(s -> readyForAddingToResult.addAll(currentDirectories(s)));
      result.clear();
      result.addAll(readyForAddingToResult);
      readyForAddingToResult.clear();
    }
  }

  // Возвращает список директорий в папке
  private List<String> currentDirectories(String filePath) {
    List<String> result = new ArrayList<>();
    // Список файлов текущей директории
    String[] currentFiles = new File(filePath).list();
    if (currentFiles != null) {
      Arrays.stream(currentFiles)
            .map(fileName -> getFullNameFile(filePath, fileName))
            .forEach(fullNameFile -> {
              File file = new File(fullNameFile);
              String fileForAdd = getCorrectFile(file);
              if (isaFile(file, fileForAdd)) {
                resultFiles.add(new Container(id, fileForAdd));
                id++;
              }
              // Если каталог записываем в колекцию и продолжаем поиск
              else {
                result.add(fullNameFile);
              }
            });
    }
    return result;
  }

  // Если файл делаем сразу проверку
  private boolean isaFile(File file, String fileForAdd) {
    return file.isFile() && fileForAdd != null && file.canRead();
  }

  @NotNull
  private String getFullNameFile(String filePath, String fileName) {
    return filePath + "\\" + fileName;
  }

  @Nullable
  private String getCorrectFile(File file) {
    String result = null;
    String fullNameFile = file.getAbsolutePath();
    if (isaGoodFileAtAll(fullNameFile, file)) {
      result = fullNameFile;
    }
    return result;
  }

  private boolean isaGoodFileAtAll(String fullNameFile, File file) {
    return file.canRead() && isFileTypeGood(findType, file.getName()) &&
        (findText.equals("") || isFileContainCurrentText(fullNameFile, findText));
  }

  // Проверка расширения
  private boolean isFileTypeGood(String what, String testString) {
    return Pattern.compile(".+\\." + what + "$")
                  .matcher(testString)
                  .matches();
  }

  // Проверка наличия искомого текста в файле
  private boolean isFileContainCurrentText(String fileName, String findText) {
    boolean result = false;
    try (Stream<String> stream = Files.lines(Paths.get(fileName), Charset.forName("ISO-8859-1"))) {
      result = stream.anyMatch(s -> s.contains(findText));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }
}