package controller;

import service.SearchFilesService;

public class SearchFilesController implements Runnable {

  private SearchFilesService searchFilesService;

  public SearchFilesController(String directory, String findText, String findType) {
    searchFilesService = new SearchFilesService();
    searchFilesService.setDirectory(directory.trim()
                                             .equals("") ? "C:\\" : directory);
    searchFilesService.setFindText(findText);
    // если ничего не ввели выставляем по-умолчанию .log
    searchFilesService.setFindType(findType.trim()
                                           .equals("") ? "log" : findType);
  }

  @Override
  public void run() {
    searchFilesService.startSearching();
  }
}
