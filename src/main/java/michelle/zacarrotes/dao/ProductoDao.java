package michelle.zacarrotes.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import michelle.zacarrotes.modelo.Producto;

/**
 * DAO de productos. Los procedures ya existen en PostgreSQL; aqui solo se
 * invocan. Se usa prepareCall("CALL nombre(?...)") (sin el escape "{call ...}")
 * porque en PostgreSQL los PROCEDURE se llaman con la sentencia CALL, y esa
 * forma es la que interpreta de forma fiable el driver pgjdbc.
 */
public class ProductoDao {

    /** Lista el catalogo con el nombre del proveedor usando la vista. */
    public List<Producto> listarTodos() throws SQLException {
        String sql = "SELECT idproducto, producto, marca, precio, caducidad, "
                + "cantidad, imagen_url, proveedor, tel_proveedor "
                + "FROM v_catalogo_completo ORDER BY producto";
        List<Producto> productos = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql);
                ResultSet resultado = statement.executeQuery()) {
            while (resultado.next()) {
                Producto producto = new Producto();
                producto.setIdproducto(resultado.getInt("idproducto"));
                producto.setNombreproducto(resultado.getString("producto"));
                producto.setMarca(resultado.getString("marca"));
                producto.setPrecio(resultado.getBigDecimal("precio"));
                Date caducidad = resultado.getDate("caducidad");
                producto.setCaducidad(caducidad == null ? null : caducidad.toLocalDate());
                producto.setCantidad(resultado.getInt("cantidad"));
                producto.setImagenUrl(resultado.getString("imagen_url"));
                producto.setNombreproveedor(resultado.getString("proveedor"));
                producto.setTelProveedor(resultado.getString("tel_proveedor"));
                productos.add(producto);
            }
        }
        return productos;
    }

    /**
     * Alta. Llama a p_insertar_producto(nombre, marca, precio, caducidad,
     * cantidad, imagen_url, idproveedor).
     */
    public void insertar(Producto producto) throws SQLException {
        String sql = "CALL p_insertar_producto(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
                CallableStatement statement = conexion.prepareCall(sql)) {
            statement.setString(1, producto.getNombreproducto());
            statement.setString(2, producto.getMarca());
            statement.setBigDecimal(3, producto.getPrecio());
            statement.setDate(4, Date.valueOf(producto.getCaducidad()));
            statement.setInt(5, producto.getCantidad());
            statement.setString(6, producto.getImagenUrl());
            statement.setInt(7, producto.getIdproveedor());
            statement.execute();
        }
    }

    /**
     * Edicion de datos descriptivos. Llama a p_editar_producto(idproducto,
     * nombre, marca, precio, imagen_url). NO toca cantidad ni caducidad (para
     * eso esta abastecer()).
     */
    public void editar(Producto producto) throws SQLException {
        String sql = "CALL p_editar_producto(?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
                CallableStatement statement = conexion.prepareCall(sql)) {
            statement.setInt(1, producto.getIdproducto());
            statement.setString(2, producto.getNombreproducto());
            statement.setString(3, producto.getMarca());
            statement.setBigDecimal(4, producto.getPrecio());
            statement.setString(5, producto.getImagenUrl());
            statement.execute();
        }
    }

    /**
     * Edicion COMPLETA. Llama a p_editar_producto_completo(idproducto, nombre,
     * marca, precio, imagen_url, cantidad, caducidad, idproveedor). Fija cantidad
     * y caducidad con el valor exacto (no suma) y permite cambiar el proveedor.
     */
    public void editarCompleto(Producto producto) throws SQLException {
        String sql = "CALL p_editar_producto_completo(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
                CallableStatement statement = conexion.prepareCall(sql)) {
            statement.setInt(1, producto.getIdproducto());
            statement.setString(2, producto.getNombreproducto());
            statement.setString(3, producto.getMarca());
            statement.setBigDecimal(4, producto.getPrecio());
            statement.setString(5, producto.getImagenUrl());
            statement.setInt(6, producto.getCantidad());
            statement.setDate(7, Date.valueOf(producto.getCaducidad()));
            statement.setInt(8, producto.getIdproveedor());
            statement.execute();
        }
    }

    /**
     * Abastecimiento. Llama a p_abastecer(idproducto, cantidad_agregada,
     * nueva_caducidad): SUMA la cantidad al inventario existente y actualiza la
     * caducidad. Con cantidad 0 sirve para actualizar solo la caducidad.
     */
    public void abastecer(int idProducto, int cantidadAgregada, LocalDate nuevaCaducidad) throws SQLException {
        String sql = "CALL p_abastecer(?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
                CallableStatement statement = conexion.prepareCall(sql)) {
            statement.setInt(1, idProducto);
            statement.setInt(2, cantidadAgregada);
            statement.setDate(3, Date.valueOf(nuevaCaducidad));
            statement.execute();
        }
    }

    /**
     * Baja. No hay procedure: DELETE directo. Puede lanzar SQLException con
     * SQLState 23503 (foreign_key_violation) si el producto tiene ventas
     * relacionadas (ON DELETE RESTRICT); el controlador traduce ese caso a un
     * mensaje claro para el usuario.
     */
    public void eliminar(int idProducto) throws SQLException {
        String sql = "DELETE FROM producto WHERE idproducto = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, idProducto);
            statement.executeUpdate();
        }
    }
}
