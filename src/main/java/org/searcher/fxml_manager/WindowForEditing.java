package org.searcher.fxml_manager;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.searcher.open_file.GetTextFromFile;

public class WindowForEditing implements Initializable {

  public static final Stage editWindowStage = new Stage();

  public static List<String> linesCurrentFile = new ArrayList<>();

  public String filePath;

  static {
    editWindowStage.initModality(Modality.APPLICATION_MODAL);
    editWindowStage.setResizable(false);
    editWindowStage.getIcons().add(new Image("/icon_docs.png"));
  }

  @FXML
  private TextArea textForEdit;

  @FXML
  public void saveFile() {
    MainWindowController.fileAndLines.get(filePath)
                                     .clear();
    MainWindowController.fileAndLines.get(filePath)
                                     .add(textForEdit.getText());
    try (FileWriter fw = new FileWriter(MainWindowController.currentFilePath, false)) {
      fw.write(textForEdit.getText());
    } catch (IOException e) {
      e.printStackTrace();
    }
    closeEditWindow();
  }

  @FXML
  public void closeEditWindow() {
    MainWindowController.fileAndLines.put(filePath, linesCurrentFile);
    editWindowStage.close();
    MainWindowController.getWindow();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    filePath = MainWindowController.currentFilePath;
    boolean textPresentInFile = false;
    for (String key : MainWindowController.fileAndLines.keySet()) {
      if (key.equals(filePath)) {
        linesCurrentFile = MainWindowController.fileAndLines.get(key);
        textPresentInFile = true;
        break;
      }
    }
    if (!textPresentInFile) {
      try {
        if (MainWindowController.fileAndLines.containsKey(filePath)) {
          linesCurrentFile = MainWindowController.fileAndLines.get(
              filePath);
        } else {
          linesCurrentFile = GetTextFromFile.getTextFromFile(filePath);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
    for (String line : linesCurrentFile) {
      textForEdit.appendText(line + "\n");
    }
  }
}
