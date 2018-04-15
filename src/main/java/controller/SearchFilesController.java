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
    searchFilesService.setFindText(findText);
    // если ничего не ввели выставляем по-умолчанию .log
    searchFilesService.setFindType(findType.trim());
    searchFilesService.setChooseSearchType(chooseSearchType);
    searchFilesService.setFindNameFile(findNameFile.trim());
  }

  public Map<String, int[]> getSearchedFiles() {
    return searchFilesService.getSearchedFiles();
  }

  @Override
  public void run() {
    searchFilesService.startSearching();
  }
}
