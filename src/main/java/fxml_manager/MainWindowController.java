package fxml_manager;

import static java.nio.file.Files.readAllLines;
import static service.SearchFilesService.executorService;

import controller.SearchFilesController;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import javafx.stage.Stage;
import model.SearchFilesModel;
import service.SearchFilesService;

public class MainWindowController implements Initializable {

  public static volatile ObservableList<SearchFilesModel> resultFiles = FXCollections.observableArrayList();
  public static Stage primaryStage;

  @FXML
  private TableView<SearchFilesModel> resultFinder;

  @FXML
  private TableColumn<SearchFilesModel, Integer> idFind;

  @FXML
  private TableColumn<SearchFilesModel, String> nameFile;

  @FXML
  private TextField innerFinder;
  private ObservableList<String> textOpenFile = FXCollections.observableArrayList();

  @FXML
  private TextField whatFind;

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

  private String chooseRes;
  @FXML
  private ChoiceBox chooseSearchType;
  private Thread mainThread;

  @FXML
  private void startFind() {
    resultFiles.clear();
    SearchFilesController memFind = new SearchFilesController(innerFinder.getText(),
        whatFindText.getText(),
        whatFind.getText(), (String) chooseSearchType.getValue());
    mainThread = new Thread(memFind);
    mainThread.start();
  }

  private void writeNameFileToLabelChangeName(TableRow<SearchFilesModel> row, MouseEvent event) {
    if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY
        && event.getClickCount() == 1) {
      SearchFilesModel clickedRow = row.getItem();
      chooseRes = clickedRow.getName();
      /////// Вывод имени
      changeName.setText(new File(chooseRes).getName());
    }
  }

  @FXML
  private void openFile() {
    textOpenFile.clear();
    getTextFromFile(chooseRes);
  }

  private void getTextFromFile(String str) {
    try {
      textOpenFile.addAll(readAllLines(Paths.get(str), Charset.forName("ISO-8859-1")));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    initializeSearchingResultRow();

    initializeResultFinder();

    textOutputFile.setItems(textOpenFile);

    primaryStage.setOnCloseRequest(event -> executorService.shutdown());

    createAndStartThreadListener();
  }

  private void initializeSearchingResultRow() {
    textOutputFile.setEditable(false);
    idFind.setCellValueFactory(new PropertyValueFactory<>("id"));
    nameFile.setCellValueFactory(new PropertyValueFactory<>("name"));
  }

  private void initializeResultFinder() {
    resultFinder.setItems(resultFiles);
    resultFinder.setRowFactory(tv -> {
      TableRow<SearchFilesModel> row = new TableRow<>();
      row.setOnMouseClicked(event -> writeNameFileToLabelChangeName(row, event));
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
        count[0]++;
      }
    });
    listenerThread.setDaemon(true);
    listenerThread.start();
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
      progressSearching.setProgress(-1);
      progressSearching.setVisible(true);
      innerFinder.setDisable(true);
      whatFind.setDisable(true);
      whatFindText.setDisable(true);
      startSearch.setDisable(true);
    });
  }

  private void enableAllElementsAfterSearch(int x) {
    Platform.runLater(() -> {
      System.out.println(x);
      progressSearching.setVisible(false);
      progressSearching.setProgress(0);
      innerFinder.setDisable(false);
      whatFind.setDisable(false);
      whatFindText.setDisable(false);
      startSearch.setDisable(false);
    });
  }
}
