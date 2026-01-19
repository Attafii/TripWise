package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FXUtil {
    public static void info(String header, String content){
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Info");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    public static void error(String header, String content){
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    public static void openModal(String fxml, String title, int w, int h) throws Exception {
        FXMLLoader loader = new FXMLLoader(FXUtil.class.getResource(fxml));
        Parent root = loader.load();
        Stage s = new Stage();
        s.setTitle(title);
        s.initModality(Modality.APPLICATION_MODAL);
        s.setScene(new Scene(root, w, h));
        s.showAndWait();
    }
}
