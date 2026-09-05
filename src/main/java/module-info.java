module test.jfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop; // for PropertyChangeSupport

    opens test.jfx to javafx.fxml;
    exports test.jfx;
}
