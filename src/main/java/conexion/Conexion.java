package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Conexion {

    private Conexion() {
    }

    public static Connection obtenerConexion() throws SQLException {
        String host = requerir("DB_HOST");
        String port = requerir("DB_PORT");
        String dbName = requerir("DB_NAME");
        String user = requerir("USER_ZACARROTES");
        String password = requerir("PASSWORD_ZACARROTES");

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
        return DriverManager.getConnection(url, user, password);
    }

   
    private static String requerir(String nombreVariable) {
        String valor = System.getenv(nombreVariable);
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Falta la variable de entorno '" + nombreVariable + "'. "
                    + "Configurala en Windows y reinicia NetBeans. "
                    + "Requeridas: DB_HOST, DB_PORT, DB_NAME, USER_ZACARROTES, PASSWORD_ZACARROTES.");
        }
        return valor;
    }

    
    public static void main(String[] args) {
        try (Connection conexion = obtenerConexion()) {
            System.out.println("[OK] Conexion establecida a: " + conexion.getMetaData().getURL());
            System.out.println("[OK] Motor: " + conexion.getMetaData().getDatabaseProductName()
                    + " " + conexion.getMetaData().getDatabaseProductVersion());
        } catch (IllegalStateException e) {
            System.err.println("[ERROR de configuracion] " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[ERROR de conexion] " + e.getMessage());
        }
    }
}
