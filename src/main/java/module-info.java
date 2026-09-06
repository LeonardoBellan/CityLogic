module kfclash.citylogic {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop; // for PropertyChangeSupport

    opens kfclash.citylogic.presentation.javafx to javafx.fxml;
    exports kfclash.citylogic.domain.core;
    exports kfclash.citylogic.ports;
    exports kfclash.citylogic.presentation.javafx;
}
