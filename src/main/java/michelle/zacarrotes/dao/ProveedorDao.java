/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package michelle.zacarrotes.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import michelle.zacarrotes.modelo.Proveedor;

/**
 *
 * @author angel
 */
public class ProveedorDao {
    
    private ConexionBD conexion;
    
    public ProveedorDao()
    {
        this.conexion = new ConexionBD();
    }
    
    
    public boolean insertarProveedor(Proveedor proveedor)
    {
        String sql = "CALL p_insertar_proveedor(?, ?, ?)";
        
        try (Connection con = conexion.conectar();
             CallableStatement cs = con.prepareCall(sql))
        {
            cs.setString(1, proveedor.getNombreProveedor());
            cs.setString(2, proveedor.getDireccion());
            cs.setString(3, proveedor.getTelefono());
            
            cs.execute();
            return true;
        }
        catch(Exception e)
        {
            System.err.println("Error al insertar proveedor en puntoventarosi: " + e.getMessage());
            return false;
        }
    }
    
    
    public List<Proveedor> ListarTodos()
    {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT idproveedor, nombreproveedor, direccion, telefono FROM proveedor ORDER BY idproveedor ASC";
        
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while(rs.next())
            {
                Proveedor p = new Proveedor();
                p.setIdProveedor(rs.getInt("idproveedor"));
                p.setNombreProveedor(rs.getString("nombreproveedor"));
                p.setDireccion(rs.getString("direccion"));
                p.setTelefono(rs.getString("telefono"));
                
                lista.add(p);
            }
        }
        catch(Exception e)
        {
            System.err.println("Error al listar proveedores: " + e.getMessage());
        }
        return lista;
    }
    
    
    public boolean editar(Proveedor proveedor)
    {
        String sql = "CALL p_editar_proveedor(?, ?, ?, ?)";
        
        try (Connection con = conexion.conectar();
             CallableStatement cs = con.prepareCall(sql))
        {
            cs.setInt(1, proveedor.getIdProveedor());
            cs.setString(2, proveedor.getNombreProveedor());
            cs.setString(3, proveedor.getDireccion());
            cs.setString(4, proveedor.getTelefono());
            
            cs.execute();
            return true;
        }
        catch(Exception e)
        {
            System.err.println("Error al editar proveedor: " + e.getMessage());
            return false;
        }
    }
    
    
    public boolean eliminar(int idProveedor)
    {
        String sql = "DELETE FROM proveedor WHERE idproveedor = ?";
        
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, idProveedor);
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
        }
        catch(SQLException e)
        {
            if ("23503".equals(e.getSQLState())) {
                System.err.println("Aviso: No se puede eliminar el proveedor porque ya está ligado a otros registros.");
                
            } else {
                System.err.println("Error general al eliminar proveedor: " + e.getMessage());
            }
            return false;
        }
    }
    
}
