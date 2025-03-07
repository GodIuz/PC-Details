module com.example.randomshit {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens com.example.randomshit to javafx.fxml;
    exports com.example.randomshit;
}