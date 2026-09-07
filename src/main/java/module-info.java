module kfclash.citylogic {
    requires com.fasterxml.jackson.databind;
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.fxml;
    requires java.desktop; // for PropertyChangeSupport

    opens kfclash.citylogic.presentation.javafx to javafx.fxml;
    exports kfclash.citylogic.persistence;
    exports kfclash.citylogic.application;
    exports kfclash.citylogic.domain.buildings;
    exports kfclash.citylogic.domain.core;
    exports kfclash.citylogic.domain.map;
    exports kfclash.citylogic.ports;
    exports kfclash.citylogic.presentation.javafx;
}
