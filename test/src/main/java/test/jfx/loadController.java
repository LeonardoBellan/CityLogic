package test.jfx;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

public class loadController {

    @FXML
    private void loadSave(ActionEvent event) throws IOException {
        String saveId = ((Button) event.getSource()).getId().toString();
        int id = Integer.parseInt(saveId.replace("loadSave", ""));
        
        //game load logic

        App.setRoot("game");    
    }
    @FXML
    private void backToMenu() throws IOException {
        App.setRoot("menu");
    }
}