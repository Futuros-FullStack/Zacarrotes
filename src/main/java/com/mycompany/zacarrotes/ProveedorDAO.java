package com.mycompany.zacarrotes;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    public void insertar(Proveedor proveedor) throws SQLException {
        String sql = "INSERT INTO proveedor (nombreproveedor, direccion, telefono) VALUES (?, ?, ?)";
        try (Connection conexion = Conexion.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, proveedor.getNombre());
            statement.setString(2, proveedor.getDireccion());
            statement.setString(3, proveedor.getTelefono());
            statement.executeUpdate();
        }
    }

    public void actualizar(Proveedor proveedor) throws SQLException {
        String sql = "UPDATE proveedor SET nombreproveedor = ?, direccion = ?, telefono = ? WHERE idproveedor = ?";
        try (Connection conexion = Conexion.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, proveedor.getNombre());
            statement.setString(2, proveedor.getDireccion());
            statement.setString(3, proveedor.getTelefono());
            statement.setInt(4, proveedor.getId());
            statement.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM proveedor WHERE idproveedor = ?";
        try (Connection conexion = Conexion.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public List<Proveedor> listarTodos() throws SQLException {
        String sql = "SELECT idproveedor, nombreproveedor, direccion, telefono FROM proveedor ORDER BY nombreproveedor";
        List<Proveedor> proveedores = new ArrayList<>();
        try (Connection conexion = Conexion.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql);
                ResultSet resultado = statement.executeQuery()) {
            while (resultado.next()) {
                Proveedor proveedor = new Proveedor();
                proveedor.setId(resultado.getInt("idproveedor"));
                proveedor.setNombre(resultado.getString("nombreproveedor"));
                proveedor.setDireccion(resultado.getString("direccion"));
                proveedor.setTelefono(resultado.getString("telefono"));
                proveedores.add(proveedor);
            }
        }
        return proveedores;
    }

    /**
     * Proveedores para el ComboBox de la vista de Productos. Lee de la tabla
     * proveedor (singular: idproveedor, nombreproveedor), que es la que referencia
     * producto.idproveedor. Solo se poblan id y nombre (lo que necesita el combo:
     * muestra el nombre, guarda el id).
     */
    public List<Proveedor> listarParaCombo() throws SQLException {
        String sql = "SELECT idproveedor, nombreproveedor FROM proveedor ORDER BY nombreproveedor";
        List<Proveedor> proveedores = new ArrayList<>();
        try (Connection conexion = Conexion.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql);
                ResultSet resultado = statement.executeQuery()) {
            while (resultado.next()) {
                Proveedor proveedor = new Proveedor();
                proveedor.setId(resultado.getInt("idproveedor"));
                proveedor.setNombre(resultado.getString("nombreproveedor"));
                proveedores.add(proveedor);
            }
        }
        return proveedores;
    }

    /**
     * @param idExcluir id del proveedor que se está editando (null en modo inserción),
     *                  para no chocar contra su propio registro al validar unicidad.
     */
    public boolean existeNombreODireccion(String nombre, String direccion, Integer idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM proveedor WHERE (nombreproveedor = ? OR direccion = ?) AND idproveedor <> ?";
        try (Connection conexion = Conexion.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setString(2, direccion);
            statement.setInt(3, idExcluir == null ? -1 : idExcluir);
            try (ResultSet resultado = statement.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        }
    }
}
