module test.jfx {
    requires javafx.controls;
    requires javafx.fxml;

    opens test.jfx to javafx.fxml;
    exports test.jfx;
}
