package sample;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import sample.engine.Container;
import sample.engine.SearchFiles;

public class Controller extends Container implements Initializable {

  protected static volatile ObservableList<Container> resultFiles = FXCollections.observableArrayList();

  @FXML
  private TableView<Container> resultFinder;

  @FXML
  private TableColumn<Container, Integer> idFind;

  @FXML
  private TableColumn<Container, String> nameFile;

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

  private String chooseRes;

  @FXML
  private void startFind() {
    SearchFiles memFind = new SearchFiles(innerFinder.getText(), whatFindText.getText(),
        whatFind.getText());

    Thread mainThread = new Thread(memFind);
    mainThread.start();

    idFind.setCellValueFactory(new PropertyValueFactory<>("id"));
    nameFile.setCellValueFactory(new PropertyValueFactory<>("name"));

    resultFinder.setItems(resultFiles);
    resultFinder.setRowFactory(tv -> {
      TableRow<Container> row = new TableRow<>();
      row.setOnMouseClicked(event -> writeNameFileToLabelChangeName(row, event));
      return row;
    });

  }

  private void writeNameFileToLabelChangeName(TableRow<Container> row, MouseEvent event) {
    if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY
        && event.getClickCount() == 1) {
      Container clickedRow = row.getItem();
      chooseRes = clickedRow.getName();
      /////// Вывод имени
      changeName.setText(new File(chooseRes).getName());
    }
  }

  @FXML
  private void openFile() {
    textOutputFile.textProperty().addListener((ChangeListener<Object>) (observable, oldValue, newValue) ->
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
                     .toString();
    } catch (IOException e) {
      result = "error_exception";
    }
    return result;
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    System.out.println("Start");
  }
}
