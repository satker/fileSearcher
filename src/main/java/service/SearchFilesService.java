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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import model.SearchFilesModel;

public class SearchFilesService {

  private String directory; // Искомая директория
  private String findText; // Искомый текст
  private String findType; // Искомое расширение
  private int id = 1; // ID результата
  private static final int processors = Runtime.getRuntime()
                                               .availableProcessors();
  private ExecutorService executorService = Executors.newFixedThreadPool(processors);

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
      for (String currentDirectory : result) {
        if (currentDirectory != null) {
          Future<List<String>> listFuture = executorService.submit(
              () -> currentDirectories(currentDirectory));
          try {
            readyForAddingToResult.addAll(listFuture.get());
          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (ExecutionException e) {
            e.printStackTrace();
          }
        }
      }
//      result.stream()
//            .filter(Objects::nonNull)
//            .forEach(currentDirectory ->
//                readyForAddingToResult.addAll(currentDirectories(currentDirectory)));
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
            .map(fileOrDirectoryName ->
                getFileFromFullNameFileOrDirectory(path, fileOrDirectoryName))
            .forEach(currentFile ->
                isaRequiredFileOrDirectory(result, currentFile));
    }
    return result;
  }

  private void isaRequiredFileOrDirectory(List<String> result, File currentFile) {
    if (isaFile(currentFile)) {
      String fileForAdd = getCorrectFile(currentFile);
      if (fileForAdd != null) {
        MainWindowController.resultFiles.add(new SearchFilesModel(id, fileForAdd));
        id++;
      }
    } else {
      result.add(currentFile.getAbsolutePath());
    }
  }

  // Если файл делаем сразу проверку
  private boolean isaFile(File file) {
    return file.isFile() && file.canRead();
  }

  private File getFileFromFullNameFileOrDirectory(String fileOrDirectoryPath,
                                                  String fileOrDirectoryName) {
    String fullNameFileOrDirectory = fileOrDirectoryPath + "\\" + fileOrDirectoryName;
    return new File(fullNameFileOrDirectory);
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