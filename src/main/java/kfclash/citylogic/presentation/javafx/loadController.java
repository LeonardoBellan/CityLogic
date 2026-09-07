package kfclash.citylogic.presentation.javafx;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

public class loadController {

    @FXML
    private void loadSave(ActionEvent event) throws IOException {
        String saveId = ((Button) event.getSource()).getId().toString();
        int id = Integer.parseInt(saveId.replace("loadSave", ""));
        
        try {
            App.loadGame(id);
            App.setRoot("game");
        } catch (IOException | IllegalArgumentException error) {
            error.printStackTrace();
        }
    }
    @FXML
    private void backToMenu() throws IOException {
        App.setRoot("menu");
    }
}