package org.searcher.fxml_manager;

import static java.nio.file.Files.readAllLines;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
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
import org.searcher.service.SearchFilesService;

public class MainWindowController implements Initializable {

  public static volatile ObservableList<SearchFilesModel> resultFiles = FXCollections.observableArrayList();

  public static final Map<String, List<String>> fileAndLines = new HashMap<>();

  public static Stage primaryStage;

  public static String currentFilePath;

  private static ObservableList textOpenFile = FXCollections.observableArrayList();

  private Thread mainThread;

  private SearchFilesController memFind;

  private String[] textTypes = ("1ST ABW ACL AFP AMI ANS ASC AWW CCF CSV CWK DBK DITA DOC" +
      " DOCM DOCX DOT DOTX EGT EPUB EZW FDX FTM FTX GDOC HTML HWP HWPML LOG LWP" +
      " MBP MD ME MCW Mobi NB NBP NEIS ODM ODOC ODT OSHEET OTT OMM PAGES PAP PDAX" +
      " PDF QUOX Radix-64RTF RPT SDW SE STW Sxw TeX INFO Troff TXT UOF UOML VIA" +
      " WPD WPS WPT WRD WRF WRI XHTML XML XPS").toLowerCase()
                                               .split(" ");

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
  public void editFileOpenWindow() throws IOException {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(Main.resource);
    AnchorPane rootLayout = loader.load();
    Scene editFileScene = new Scene(rootLayout);
    WindowForEditing.editWindowStage.setTitle("Редактирование файла");
    WindowForEditing.editWindowStage.setScene(editFileScene);
    WindowForEditing.editWindowStage.show();
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

  private void writeNameFileToLabelChangeName(TableRow<SearchFilesModel> row, MouseEvent event) {
    if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY
        && event.getClickCount() == 1) {
      SearchFilesModel clickedRow = row.getItem();
      currentFilePath = clickedRow.getName();
      /////// Вывод имени
      changeName.setText(new File(currentFilePath).getName());
    }
  }

  private void getTextFromFile(String str) {
    try {
      int[] searchedStrings = memFind.getSearchedFiles()
                                     .get(str);
      List<String> linesCurrentFile;
      if (fileAndLines.containsKey(str)) {
        linesCurrentFile = fileAndLines.get(str);
      } else {
        linesCurrentFile = readAllLines(Paths.get(str), Charset.forName("ISO-8859-1"));
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
      fileAndLines.put(str, linesCurrentFile);
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

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    TextFields.bindAutoCompletion(findType, textTypes);

    stopSearch.setDisable(true);

    initializeSearchingResultRow();

    initializeResultFinder();

    textOutputFile.setItems(textOpenFile);

    primaryStage.setOnCloseRequest(event -> {
      stopSearchAction();
    });

    createAndStartThreadListener();
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
        //        if (event.getClickCount() == 2) {
        //          openFile();
        //        } else {
        writeNameFileToLabelChangeName(row, event);
        //}
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
        if (isaTextTypeOfFIle()) {
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

  private boolean isaTextTypeOfFIle() {
    return changeName.getText()
                     .equals("") || Arrays.stream(textTypes)
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
      innerFinder.setDisable(true);
      findType.setDisable(true);
      whatFindText.setDisable(true);
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
      innerFinder.setDisable(false);
      findType.setDisable(false);
      whatFindText.setDisable(false);
      startSearch.setDisable(false);
      chooseSearchType.setDisable(false);
      findNameFile.setDisable(false);
    });
  }
}
