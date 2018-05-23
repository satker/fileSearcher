package org.searcher.fxml_manager;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;
import org.searcher.Main;
import org.searcher.controller.SearchFilesController;
import org.searcher.model.SearchFilesModel;
import org.searcher.open_file.GetTextFromFile;
import org.searcher.service.SearchFilesService;

public class MainWindowController implements Initializable {

  public static final Map<String, List<String>> fileAndLines = new HashMap<>();

  public static final String[] TEXT_TYPES = (
      "RTF 1ST ABW ACL AFP AMI ANS ASC AWW CCF CSV CWK DBK DITA DOC" +
          " DOCM DOCX DOT DOTX EGT EPUB EZW FDX FTM FTX GDOC HTML HWP HWPML LOG LWP" +
          " MBP MD ME MCW Mobi NB NBP NEIS ODM ODOC ODT OSHEET OTT OMM PAGES PAP PDAX" +
          " PDF QUOX Radix-64RTF RPT SDW SE STW Sxw TeX INFO Troff TXT UOF UOML VIA" +
          " WPD WPS WPT WRD WRF WRI XHTML XML XPS").toLowerCase()
                                                   .split(" ");

  public static volatile ObservableList<SearchFilesModel> resultFiles = FXCollections.observableArrayList();

  public static Stage primaryStage;

  static String currentFilePath;

  private static ObservableList textOpenFile = FXCollections.observableArrayList();

  private Thread mainThread;

  private SearchFilesController memFind;

  @FXML
  private TableView<SearchFilesModel> resultFinder;

  @FXML
  private TableColumn<SearchFilesModel, Integer> idFind;

  @FXML
  private TableColumn<SearchFilesModel, String> nameFile;

  @FXML
  private TextField innerFinder;

  @FXML
  private TextField findType;

  @FXML
  private TextField whatFindText;

  @FXML
  private Label changeName;

  @FXML
  private Button startSearch;

  @FXML
  private ProgressIndicator progressSearching;

  @FXML
  private ListView textOutputFile;

  @FXML
  private ChoiceBox chooseSearchType;

  @FXML
  private TextField findNameFile;

  @FXML
  private Button stopSearch;

  @FXML
  private Button editFile;

  @FXML
  private Button openFile;

  @FXML
  private Button chooseDirectory;


  static void getWindow() {
    primaryStage.show();
  }

  @FXML
  private void startFind() {
    changeName.setText("");
    textOpenFile.clear();
    resultFiles.clear();

    memFind = new SearchFilesController(innerFinder.getText(),
        whatFindText.getText(),
        findType.getText(),
        (String) chooseSearchType.getValue(),
        findNameFile.getText());
    mainThread = new Thread(memFind);
    mainThread.start();
  }

  @FXML
  public void stopSearchAction() {
    if (mainThread != null && mainThread.isAlive()) {
      mainThread.stop();
      SearchFilesService.searchIsAlive = false;
    }
  }

  @FXML
  private void openFile() {
    Platform.runLater(() -> {
      textOpenFile.clear();
      getTextFromFile(currentFilePath);
    });
  }

  @FXML
  public void editFileOpenWindow() {
    try {
      Desktop.getDesktop()
             .edit(new File(currentFilePath));
    } catch (IOException e) {
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(Main.resource);
      AnchorPane rootLayout = null;
      try {
        rootLayout = loader.load();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
      Scene editFileScene = new Scene(rootLayout);
      WindowForEditing.editWindowStage.setTitle("Редактирование файла");
      WindowForEditing.editWindowStage.setScene(editFileScene);
      WindowForEditing.editWindowStage.show();
    }
  }

  @FXML
  private void chooseDirectory() {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle("Choose directory");
    File defaultDirectory = new File("C:/");
    directoryChooser.setInitialDirectory(defaultDirectory);
    File selectedDirectory = directoryChooser.showDialog(primaryStage);
    if (selectedDirectory != null) {
      innerFinder.setText(selectedDirectory.getAbsolutePath());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    innerFinder.setDisable(true);

    TextFields.bindAutoCompletion(findType, TEXT_TYPES);

    stopSearch.setDisable(true);

    initializeSearchingResultRow();

    initializeResultFinder();

    textOutputFile.setItems(textOpenFile);

    primaryStage.setOnCloseRequest(event -> {
      stopSearchAction();
    });

    createAndStartThreadListener();
  }

  private void writeNameFileToLabelChangeName(TableRow<SearchFilesModel> row, MouseEvent event) {
    if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY
        && event.getClickCount() == 1) {
      SearchFilesModel clickedRow = row.getItem();
      currentFilePath = clickedRow.getName();
      /////// Вывод имени
      changeName.setText(new File(currentFilePath).getName());
    }
  }

  private void getTextFromFile(String pathOfFile) {
    try {
      int[] searchedStrings = memFind.getSearchedFiles()
                                     .get(pathOfFile);
      List<String> linesCurrentFile;
      if (fileAndLines.containsKey(pathOfFile)) {
        linesCurrentFile = fileAndLines.get(pathOfFile);
      } else {
        linesCurrentFile = GetTextFromFile.getTextFromFile(pathOfFile);
      }
      StringBuilder currentLines = new StringBuilder();
      if (searchedStrings != null) {
        enterTextToListViewIfTextPresent(searchedStrings, linesCurrentFile, currentLines);
      } else {
        for (String lineOfCurrentFile : linesCurrentFile) {
          currentLines.append(lineOfCurrentFile)
                      .append('\n');
          textOpenFile.add(lineOfCurrentFile);
        }
      }
      fileAndLines.put(pathOfFile, linesCurrentFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void enterTextToListViewIfTextPresent(int[] searchedStrings,
                                                List<String> linesCurrentFile,
                                                StringBuilder currentLines) {
    mainLoop:
    for (int i = 0; i < linesCurrentFile.size(); i++) {
      String currentLine = linesCurrentFile.get(i);
      currentLines.append(currentLine)
                  .append('\n');
      for (int j = 0; j < searchedStrings.length; j++) {
        if (searchedStrings[j] != -1) {
          if (searchedStrings[j] == i) {
            Label label = new Label();
            label.setText(currentLine);
            label.setTextFill(Color.RED);
            textOpenFile.add(label);
            searchedStrings[j] = -1;
            continue mainLoop;
          }
        }
      }
      textOpenFile.add(currentLine);
    }
  }

  private void initializeSearchingResultRow() {
    textOutputFile.setEditable(true);
    idFind.setCellValueFactory(new PropertyValueFactory<>("id"));
    nameFile.setCellValueFactory(new PropertyValueFactory<>("name"));
  }

  private void initializeResultFinder() {
    resultFinder.setItems(resultFiles);
    resultFinder.setRowFactory(tv -> {
      TableRow<SearchFilesModel> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2) {
          if (!isTextTypeOfFIle()) {
            openFile();
          }
        } else {
          writeNameFileToLabelChangeName(row, event);
        }
      });
      return row;
    });
  }

  private void createAndStartThreadListener() {
    Thread listenerThread = new Thread(() -> {
      final int[] count = {1};
      while (true) {
        try {
          TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        checkDisableOrEnableElements(count);
        if (changeName.getText()
                      .equals("") || isTextTypeOfFIle()) {
          editFile.setDisable(true);
          openFile.setDisable(true);
        } else {
          openFile.setDisable(false);
          editFile.setDisable(false);
        }
        count[0]++;
      }
    });
    listenerThread.setDaemon(true);
    listenerThread.start();
  }

  private boolean isTextTypeOfFIle() {
    return Arrays.stream(TEXT_TYPES)
                 .noneMatch(
                     type -> SearchFilesService.isFileTypeGood(
                         changeName.getText(),
                         type.toLowerCase()));
  }

  private void checkDisableOrEnableElements(int[] count) {
    if (SearchFilesService.searchIsAlive) {
      if (progressSearching.getProgress() != -1) {
        disableAllElementsBeforeSearching(count);
      }
    } else {
      if (progressSearching.getProgress() != 0) {
        enableAllElementsAfterSearch(count[0]);
      }
    }
  }

  private void disableAllElementsBeforeSearching(int[] count) {
    Platform.runLater(() -> {
      count[0] = 0;
      stopSearch.setDisable(false);
      progressSearching.setProgress(-1);
      progressSearching.setVisible(true);
      findType.setDisable(true);
      whatFindText.setDisable(true);
      chooseDirectory.setDisable(true);
      startSearch.setDisable(true);
      chooseSearchType.setDisable(true);
      findNameFile.setDisable(true);
    });
  }

  private void enableAllElementsAfterSearch(int x) {
    Platform.runLater(() -> {
      System.out.println(x / 2 + " сек.");
      stopSearch.setDisable(true);
      progressSearching.setVisible(false);
      progressSearching.setProgress(0);
      findType.setDisable(false);
      chooseDirectory.setDisable(false);
      whatFindText.setDisable(false);
      startSearch.setDisable(false);
      chooseSearchType.setDisable(false);
      findNameFile.setDisable(false);
    });
  }
}
