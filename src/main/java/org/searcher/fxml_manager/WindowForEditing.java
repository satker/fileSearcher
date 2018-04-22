package org.searcher.fxml_manager;

import static java.nio.file.Files.readAllLines;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class WindowForEditing implements Initializable {

  public static final WindowForEditing editWindow = new WindowForEditing();
  public static final Stage editWindowStage = new Stage();

  static {
    editWindowStage.initModality(Modality.APPLICATION_MODAL);
  }

  @FXML
  private TextArea textForEdit;

  @FXML
  public void saveFile() {
    try (FileWriter fw = new FileWriter(MainWindowController.currentFilePath, false)) {
      fw.write(textForEdit.getText());
    } catch (IOException e) {
      e.printStackTrace();
    }
    closeEditWindow();
  }

  @FXML
  public void closeEditWindow() {
    editWindowStage.close();
    MainWindowController.getWindow();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    StringBuilder textFile = new StringBuilder();
    boolean textPresentInFile = false;
    for (String key : MainWindowController.fileWithText.keySet()) {
      if (key.equals(MainWindowController.currentFilePath)) {
        textFile = MainWindowController.fileWithText.get(key);
        textPresentInFile = true;
        break;
      }
    }
    if (!textPresentInFile) {
      try {
        getTextFromFile(textFile);
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
    textForEdit.setText(textFile.toString());
  }

  private void getTextFromFile(StringBuilder textFile) throws IOException {
    List<String> linesCurrentFile = readAllLines(Paths.get(MainWindowController.currentFilePath),
        Charset.forName("ISO-8859-1"));
    for (String line : linesCurrentFile) {
      textFile.append(line)
              .append('\n');
    }
  }
}
