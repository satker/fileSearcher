package service;
/*
     Модуль поиска (мозг программы)
*/

import fxml_manager.MainWindowController;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import model.SearchFilesModel;

public class SearchFilesService {

  private String directory; // Искомая директория
  private String findText; // Искомый текст
  private String findType; // Искомое расширение
  private int id = 1; // ID результата

  private String chooseSearchType;
  public static volatile boolean searchIsAlive = false;


  public void setDirectory(String directory) {
    this.directory = directory;
  }

  public void setFindText(String findText) {
    this.findText = findText;
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
    if (isaGoodFileAtAll(fullNameFile, file)) {
      resultFullName = fullNameFile;
    }
    return resultFullName;
  }

  private boolean isaGoodFileAtAll(String fullNameFile, File file) {
    return file.canRead() && isFileTypeGood(findType, file.getName()) &&
        (findText.equals("") || isFileContainCurrentText(file));
  }

  // Проверка расширения
  private boolean isFileTypeGood(String what, String testString) {
    return Pattern.compile(".+\\." + what + "$")
                  .matcher(testString)
                  .matches();
  }

  // Проверка наличия искомого текста в файле
  private boolean isFileContainCurrentText(File file) {
    boolean isFuzzy = chooseSearchType.equals("Fuzzy search") ? true : false;
    try {
      return new LuceneSearcherService().isaTextInFile(file, findText, isFuzzy);
    } catch (Exception e) {
      return false;
    }
  }
}