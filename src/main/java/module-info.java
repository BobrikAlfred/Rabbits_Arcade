module com.example.rabbits_arcade {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.media;
    requires java.desktop;

    opens com.example.rabbits_arcade to javafx.fxml;
    exports com.example.rabbits_arcade;
}