package org.searcher.service;
/*
     Модуль поиска (мозг программы)
*/

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.searcher.fxml_manager.MainWindowController;
import org.searcher.model.SearchFilesModel;

public class SearchFilesService {

  private String directory; // Искомая директория

  private String findText; // Искомый текст

  private String findType; // Искомое расширение

  private String findNameFile;

  private int id = 1; // ID результата

  private String chooseSearchType;

  private Map<String, int[]> searchedFiles = new HashMap<>();

  public static volatile boolean searchIsAlive = false;

  public Map<String, int[]> getSearchedFiles() {
    return searchedFiles;
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }

  public void setFindText(String findText) {
    this.findText = findText;
  }

  public void setFindNameFile(String findNameFile) {
    this.findNameFile = findNameFile;
  }

  public void setFindType(String findType) {
    this.findType = findType;
  }

  public void setChooseSearchType(String chooseSearchType) {
    this.chooseSearchType = chooseSearchType;
  }

  public void startSearching() {
    List<String> result = currentDirectories(directory);
    List<String> readyForAddingToResult = new ArrayList<>();
    searchIsAlive = true;
    while (!result.isEmpty()) {
      for (String currentDirectory : result) {
        if (currentDirectory != null) {
          readyForAddingToResult.addAll(currentDirectories(currentDirectory));
        }
      }
      result.clear();
      result.addAll(readyForAddingToResult);
      readyForAddingToResult.clear();
    }
    searchIsAlive = false;
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
    if (currentFile.isFile()) {
      String fileForAdd = getCorrectFile(currentFile);
      if (fileForAdd != null) {
        MainWindowController.resultFiles.add(new SearchFilesModel(id, fileForAdd));
        id++;
      }
    } else {
      result.add(currentFile.getAbsolutePath());
    }
  }

  private File getFileFromFullNameFileOrDirectory(String fileOrDirectoryPath,
                                                  String fileOrDirectoryName) {
    String fullNameFileOrDirectory = fileOrDirectoryPath + "\\" + fileOrDirectoryName;
    return new File(fullNameFileOrDirectory);
  }

  private String getCorrectFile(File file) {
    String resultFullName = null;
    String fullNameFile = file.getAbsolutePath();
    if (isaGoodFileAtAll(file)) {
      resultFullName = fullNameFile;
    }
    return resultFullName;
  }

  // Проверка расширения
  public static boolean isFileTypeGood(String fileName, String findType) {
    return Pattern.compile(".+\\." + findType + "$")
                  .matcher(fileName)
                  .matches();
  }

  private boolean isaGoodFileAtAll(File file) {
    return file.canRead() && (findType.equals("") || isFileTypeGood(file.getName(), findType)) &&
        (findText.equals("") || isFileContainCurrentText(file)) && (findNameFile.equals("")
        || isFileNameGood(file.getName()));
  }

  private boolean isFileNameGood(String nameFile) {
    String nameWithoutType = nameFile.split("\\.(?=[^.]+$)")[0];
    return nameWithoutType.equals(findNameFile);
  }

  // Проверка наличия искомого текста в файле
  private boolean isFileContainCurrentText(File file) {
    boolean isFuzzy = chooseSearchType.equals("Fuzzy search");
    try {
      int[] findIndexesWithSearchedStrings = LuceneSearcherService.isaTextInFile(file, findText,
          isFuzzy);
      searchedFiles.put(file.getAbsolutePath(), findIndexesWithSearchedStrings);
      return findIndexesWithSearchedStrings.length != 0;
    } catch (Exception e) {
      return false;
    }
  }
}