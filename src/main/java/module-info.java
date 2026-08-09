module michelle.zacarrotes {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;

    requires org.postgresql.jdbc;
    
    opens michelle.zacarrotes to javafx.fxml;
    opens michelle.zacarrotes.controlador to javafx.fxml;
    exports michelle.zacarrotes;

    opens com.mycompany.zacarrotes to javafx.fxml;
    exports com.mycompany.zacarrotes;
    exports michelle.zacarrotes.controlador;
    exports michelle.zacarrotes.modelo;
    exports michelle.zacarrotes.dao;
}
