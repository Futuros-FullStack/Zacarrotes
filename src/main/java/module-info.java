module michelle.zacarrotes {
    requires javafx.controls;
    requires javafx.fxml;

    opens michelle.zacarrotes to javafx.fxml;
    exports michelle.zacarrotes;
}
