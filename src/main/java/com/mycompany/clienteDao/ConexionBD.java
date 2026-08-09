/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.clienteDao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author CORDOVA
 */
public class ConexionBD {
    
    private final String URL = "jdbc:postgresql://localhost:5432/puntoventarosi";
    private final String USER = "postgres";
    private final String PASSWORD = System.getenv("Password_zacarrotes");
    
    public Connection conectar() {
        Connection con = null;
        try {
            if (PASSWORD == null) {
                System.err.println("ERROR FATAL: La variable de entorno Password_zacarrotes no está configurada.");
                return null;
            }
            
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("¡Conexión exitosa a la base de datos!");
            
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
        }
        return con;
    }
    
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
