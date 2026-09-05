module kfclash.citylogic {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop; // for PropertyChangeSupport

    opens kfclash.citylogic.presentation.javafx to javafx.fxml;
    exports kfclash.citylogic.presentation.javafx;
}
