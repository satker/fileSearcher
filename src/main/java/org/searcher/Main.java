package org.searcher;

import java.net.URL;
import java.util.Objects;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.searcher.fxml_manager.MainWindowController;

public class Main extends Application {

  public static final URL resource = Main.class.getClassLoader()
                                               .getResource("window_for_editing.fxml");

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    initializeAndShowMainWindow(primaryStage);
  }

  private void initializeAndShowMainWindow(Stage primaryStage) throws java.io.IOException {
    MainWindowController.primaryStage = primaryStage;
    Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader()
                                                                   .getResource(
                                                                       "main_window.fxml")));
    primaryStage.setTitle("Searcher");
    primaryStage.setScene(new Scene(root));
    primaryStage.show();
  }

}
