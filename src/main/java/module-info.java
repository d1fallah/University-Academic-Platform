module PFE {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires javafx.graphics;
    requires org.apache.pdfbox;
    requires javafx.swing;
    requires java.desktop;

    exports app;
    exports app.frontend to javafx.fxml;
    exports app.backend.utils;

    opens app to javafx.graphics;
    opens app.frontend to javafx.fxml;
    opens app.backend.utils to jbcrypt;
}