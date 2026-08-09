package michelle.zacarrotes.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * cambiar el nombre de la variable de entorno y
 * cambiarle el nombre de su base a zacarrotes para no tener problemas
 */
public class ConexionBD {

    private static final String URL = "jdbc:postgresql://localhost:5432/zacarrotes";
    private static final String USER = "postgres";
    private static final String VARIABLE_PASSWORD = "password_zacarrotes";

    public static Connection obtenerConexion() throws SQLException {

        String password = System.getenv(VARIABLE_PASSWORD);

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Falta la variable de entorno '" + VARIABLE_PASSWORD + "'.");
        }
        return DriverManager.getConnection(URL, USER, password);
    }

    public Connection conectar() {
        try {
            return obtenerConexion();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo conectar a la base: " + e.getMessage(), e);
        }
    }
}
