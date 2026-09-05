package kfclash.citylogic.presentation.javafx;

import java.io.IOException;
import javafx.fxml.FXML;

public class menuController {

    @FXML
    private void startNewGame() throws IOException {
        App.setRoot("game");
    }
    @FXML
    private void loadGame() throws IOException {
        App.setRoot("load");
    }
    @FXML
    private void exitGame() {
        System.exit(0);
    }
}
 