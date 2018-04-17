package controller;

import java.util.Map;
import service.SearchFilesService;

public class SearchFilesController implements Runnable {

  private SearchFilesService searchFilesService;

  public SearchFilesController(String directory, String findText, String findType,
                               String chooseSearchType, String findNameFile) {
    searchFilesService = new SearchFilesService();
    String directoryForAdd = directory.trim()
                                      .equals("") ? "C:\\" : directory;
    searchFilesService.setDirectory(directoryForAdd);
    searchFilesService.setFindText(findText.equals("*") ? "" : findText.trim());
    // если ничего не ввели выставляем по-умолчанию .log
    searchFilesService.setFindType(findType.equals("*") ? "" : findType.trim());
    searchFilesService.setChooseSearchType(chooseSearchType);
    searchFilesService.setFindNameFile(findNameFile.equals("*") ? "" : findNameFile.trim());
  }

  public Map<String, int[]> getSearchedFiles() {
    return searchFilesService.getSearchedFiles();
  }

  @Override
  public void run() {
    searchFilesService.startSearching();
  }
}
