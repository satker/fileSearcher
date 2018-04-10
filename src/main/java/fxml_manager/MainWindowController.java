package fxml_manager;

import controller.SearchFilesController;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import model.SearchFilesModel;
import service.SearchFilesService;

public class MainWindowController implements Initializable {

  public static volatile ObservableList<SearchFilesModel> resultFiles = FXCollections.observableArrayList();

  @FXML
  private TableView<SearchFilesModel> resultFinder;

  @FXML
  private TableColumn<SearchFilesModel, Integer> idFind;

  @FXML
  private TableColumn<SearchFilesModel, String> nameFile;

  @FXML
  private TextField innerFinder;

  @FXML
  private TextArea textOutputFile;

  @FXML
  private TextField whatFind;

  @FXML
  private TextField whatFindText;

  @FXML
  private Label changeName;

  @FXML
  private Label progressSearch;

  @FXML
  private Button startSearch;

  private String chooseRes;

  @FXML
  private void startFind() {
    SearchFilesController memFind = new SearchFilesController(innerFinder.getText(),
        whatFindText.getText(),
        whatFind.getText());
    Thread mainThread = new Thread(memFind);
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
    textOutputFile.textProperty()
                  .addListener((ChangeListener<Object>) (observable, oldValue, newValue) ->
                      textOutputFile.setScrollTop(Double.MAX_VALUE));
    textOutputFile.setText(getTextFromFile(chooseRes));
  }

  private String getTextFromFile(String str) {
    String result;
    try (Stream<String> stream = Files.lines(Paths.get(str), Charset.forName("ISO-8859-1"))) {
      result = stream.collect(Collectors.toList())
                     .stream()
                     .map(a -> "\n" + a)
                     .reduce(String::concat)
                     .orElse("");
    } catch (IOException e) {
      result = "Error to have access to file";
    }
    return result;
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    textOutputFile.setEditable(false);
    idFind.setCellValueFactory(new PropertyValueFactory<>("id"));
    nameFile.setCellValueFactory(new PropertyValueFactory<>("name"));

    resultFinder.setItems(resultFiles);
    resultFinder.setRowFactory(tv -> {
      TableRow<SearchFilesModel> row = new TableRow<>();
      row.setOnMouseClicked(event -> writeNameFileToLabelChangeName(row, event));
      return row;
    });
    Thread listenerThread = new Thread(() -> {
      while (true) {
        try {
          TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if (SearchFilesService.searchIsAlive) {
          if (!progressSearch.getText()
                             .equals("Search start")) {
            Platform.runLater(() -> {
              progressSearch.setText("Search start");
              innerFinder.setDisable(true);
              whatFind.setDisable(true);
              whatFindText.setDisable(true);
              startSearch.setDisable(true);
            });
          }
        } else {
          if (!progressSearch.getText()
                             .equals("Search end")
              && !progressSearch.getText()
                                .equals("")) {
            Platform.runLater(() -> {
              progressSearch.setText("Search end");
              innerFinder.setDisable(false);
              whatFind.setDisable(false);
              whatFindText.setDisable(false);
              startSearch.setDisable(false);
            });
          }
        }
      }
    });
    listenerThread.setDaemon(true);
    listenerThread.start();
  }
}
