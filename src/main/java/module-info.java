module michelle.zacarrotes {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.postgresql.jdbc;
    
    opens michelle.zacarrotes to javafx.fxml;
    exports michelle.zacarrotes;
    
    opens com.mycompany.controllercliente to javafx.fxml;
    exports com.mycompany.controllercliente;
    exports com.mycompany.modelocliente;
}
