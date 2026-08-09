/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package michelle.zacarrotes.dao;

/**
 *
 * @author angel
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class ConexionBD {
    private final String URL = "jdbc:postgresql://localhost:5432/puntoventarosi";
    private final String USER = "postgres";
    
    // Aquí manda a llamar tu variable de entorno
    private final String PASSWORD = System.getenv("Password_zacarrotes");

    public Connection conectar() {
        Connection con = null;
        try {
            // Validación por si NetBeans no detecta la variable
            if (PASSWORD == null) {
                System.err.println("ERROR FATAL: La variable de entorno BD_PASSWORD no está configurada o hace falta reiniciar NetBeans.");
                return null;
            }
            
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            // Mensaje opcional en consola para confirmar que todo funciona
            System.out.println("¡Conexión exitosa a la base de datos!");
            
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
        }
        return con;
    }
    
    // Método útil para cerrar la conexión cuando termines de usarla en tu DAO
    public void desconectar(Connection con) {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}
