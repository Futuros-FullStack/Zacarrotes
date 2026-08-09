/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package michelle.zacarrotes.dao;

import michelle.zacarrotes.modelo.Cliente;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author CORDOVA
 */
public class ClienteDao {
    private Connection conexion = new ConexionBD().conectar();
    
    public void insertar(Cliente cliente){
       try{
            CallableStatement cs = conexion.prepareCall("CALL p_insertar_cliente(?)");
            cs.setString(1, cliente.getNombre());
            cs.execute();
            System.out.println("¡Cliente guardado con éxito!");
        }catch (Exception e){
            System.out.println("Error al insertar: " + e);
        }
    }
    
   public List<Cliente> listarClientes() {
        List<Cliente> lista = new ArrayList<>();
        try {
            PreparedStatement ps = conexion.prepareStatement("SELECT * FROM cliente");
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                lista.add(new Cliente(rs.getInt("idcliente"), rs.getString("nombre")));
            }
        } catch (Exception e) {
            System.out.println("Error al listar: " + e);
        }
        return lista;
    }
    
    public void editar(Cliente cliente) {
        try {
            CallableStatement cs = conexion.prepareCall("{CALL p_editar_cliente(?, ?)}");
            cs.setInt(1, cliente.getIdCliente());
            cs.setString(2, cliente.getNombre());
            cs.execute();
        } catch (Exception e) {
            System.out.println("Error al editar: " + e);
        }
    }
    
    public void eliminar(int idCliente) {
        try {
            PreparedStatement ps = conexion.prepareStatement("DELETE FROM cliente WHERE idcliente = ?");
            ps.setInt(1, idCliente);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error al eliminar: " + e);
        }
    }
}
