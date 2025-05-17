module PFE {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires transitive javafx.graphics;
    requires org.apache.pdfbox;
    requires javafx.swing;
    requires java.desktop;
    requires transitive javafx.base;

    exports app;
    exports app.frontend to javafx.fxml;
    exports app.backend.utils;
    exports app.backend.models;  // Add this line to export the models package

    opens app to javafx.graphics;
    opens app.frontend to javafx.fxml;
    opens app.backend.utils to jbcrypt;
    opens app.backend.models to javafx.base;  // Open models to javafx.base for property binding
}