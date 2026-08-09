module michelle.zacarrotes {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens michelle.zacarrotes to javafx.fxml;
    opens michelle.zacarrotes.controlador to javafx.fxml;
    exports michelle.zacarrotes;
    exports michelle.zacarrotes.controlador;
    exports michelle.zacarrotes.modelo;
    exports michelle.zacarrotes.dao;
}
