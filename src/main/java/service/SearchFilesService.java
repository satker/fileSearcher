package service;
/*
     Модуль поиска (мозг программы)
 */

import fxml_manager.MainWindowController;
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
import model.SearchFilesModel;

public class SearchFilesService {

  private String directory; // Искомая директория
  private String findText; // Искомый текст
  private String findType; // Искомое расширение
  private int id = 1; // ID результата

  public void setDirectory(String directory) {
    this.directory = directory;
  }

  public void setFindText(String findText) {
    this.findText = findText;
  }

  public void setFindType(String findType) {
    this.findType = findType;
  }

  public void startSearching() {
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
  private List<String> currentDirectories(String path) {
    List<String> result = new ArrayList<>();
    // Список файлов текущей директории
    String[] currentFiles = new File(path).list();
    if (currentFiles != null) {
      Arrays.stream(currentFiles)
            .map(fileOrDirectoryName -> getFullNameOrDirectoryFile(path, fileOrDirectoryName))
            .forEach(fullNameOrDirectoryFile -> {
              File fileOrDirectory = new File(fullNameOrDirectoryFile);
              if (isaFile(fileOrDirectory)) {
                String fileForAdd = getCorrectFile(fileOrDirectory);
                if (fileForAdd != null) {
                  MainWindowController.resultFiles.add(new SearchFilesModel(id, fileForAdd));
                  id++;
                }
              } else {
                result.add(fullNameOrDirectoryFile);
              }
            });
    }
    return result;
  }

  // Если файл делаем сразу проверку
  private boolean isaFile(File file) {
    return file.isFile() && file.canRead();
  }

  private String getFullNameOrDirectoryFile(String filePath, String fileName) {
    return filePath + "\\" + fileName;
  }

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