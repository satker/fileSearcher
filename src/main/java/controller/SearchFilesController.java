package controller;

import service.SearchFilesService;

public class SearchFilesController implements Runnable {

  private SearchFilesService searchFilesService;

  public SearchFilesController(String directory, String findText, String findType,
                               String chooseSearchType) {
    searchFilesService = new SearchFilesService();
    String directoryForAdd = directory.trim()
                                      .equals("") ? "C:\\" : directory;
    searchFilesService.setDirectory(directoryForAdd);
    searchFilesService.setFindText(findText);
    // если ничего не ввели выставляем по-умолчанию .log
    String typeForAdd = findType.trim()
                                .equals("") ? "log" : findType;
    searchFilesService.setFindType(typeForAdd);
    searchFilesService.setChooseSearchType(chooseSearchType);
  }

  @Override
  public void run() {
    searchFilesService.startSearching();
  }
}
