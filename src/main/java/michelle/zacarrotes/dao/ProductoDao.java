package michelle.zacarrotes.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import michelle.zacarrotes.modelo.Producto;

// esta clase es la que habla directo con la base de datos pa los productos
public class ProductoDao {

    // trae todos los productos de la vista v_catalogo_completo y me los regresa en una lista
    public List<Producto> listarTodos() throws SQLException {
        String sql = "SELECT idproducto, producto, marca, precio, caducidad, "
                + "cantidad, imagen_url, proveedor, tel_proveedor "
                + "FROM v_catalogo_completo ORDER BY producto"; // ordenados por nombre
        List<Producto> productos = new ArrayList<>();
        // el try-with-resources cierra solito la conexion cuando termina
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql);
                ResultSet resultado = statement.executeQuery()) {
            // recorro renglon por renglon lo que me trajo la consulta
            while (resultado.next()) {
                Producto producto = new Producto();
                producto.setIdproducto(resultado.getInt("idproducto"));
                producto.setNombreproducto(resultado.getString("producto"));
                producto.setMarca(resultado.getString("marca"));
                producto.setPrecio(resultado.getBigDecimal("precio"));
                Date caducidad = resultado.getDate("caducidad");
                // la fecha puede venir null, por eso el chequeo antes de convertirla
                producto.setCaducidad(caducidad == null ? null : caducidad.toLocalDate());
                producto.setCantidad(resultado.getInt("cantidad"));
                producto.setImagenUrl(resultado.getString("imagen_url"));
                producto.setNombreproveedor(resultado.getString("proveedor"));
                producto.setTelProveedor(resultado.getString("tel_proveedor"));
                productos.add(producto); // lo meto a la lista
            }
        }
        return productos;
    }
    // manda el producto nuevo a la bd llamando al procedimiento p_insertar_producto
    public void insertar(Producto producto) throws SQLException {
        String sql = "CALL p_insertar_producto(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
                CallableStatement statement = conexion.prepareCall(sql)) {
            // le voy pasando cada dato en el orden que pide el procedimiento (los ?)
            statement.setString(1, producto.getNombreproducto());
            statement.setString(2, producto.getMarca());
            statement.setBigDecimal(3, producto.getPrecio());
            statement.setDate(4, Date.valueOf(producto.getCaducidad()));
            statement.setInt(5, producto.getCantidad());
            statement.setString(6, producto.getImagenUrl());
            statement.setInt(7, producto.getIdproveedor());
            statement.execute(); // lo ejecuto
        }
    }
    // edita el producto completo y me regresa cuantas filas quedaron (pa saber si de verdad cambio)
    public int editarCompleto(Producto producto) throws SQLException {
        String sqlCall = "CALL p_editar_producto_completo(?, ?, ?, ?, ?, ?, ?, ?)"; // el que edita
        // esta consulta es pa verificar despues que si quedaron los cambios
        String sqlVerif = "SELECT COUNT(*) FROM producto "
                + "WHERE idproducto = ? AND nombreproducto = ? AND marca = ? "
                + "AND cantidad = ? AND idproveedor = ?";

        Connection conexion = null;
        try {
            conexion = ConexionBD.obtenerConexion();

            // primero llamo al procedimiento que hace la edicion
            try (CallableStatement statement = conexion.prepareCall(sqlCall)) {
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
            // si la conexion no guarda solita, le hago commit pa que si se guarde
            if (!conexion.getAutoCommit()) {
                conexion.commit();
            }
            // ahora verifico: cuento cuantos productos quedaron con esos datos exactos
            try (PreparedStatement verif = conexion.prepareStatement(sqlVerif)) {
                verif.setInt(1, producto.getIdproducto());
                verif.setString(2, producto.getNombreproducto());
                verif.setString(3, producto.getMarca());
                verif.setInt(4, producto.getCantidad());
                verif.setInt(5, producto.getIdproveedor());
                try (ResultSet resultado = verif.executeQuery()) {
                    // regreso el conteo (si es 0 es que no se guardo)
                    return resultado.next() ? resultado.getInt(1) : 0;
                }
            }
        } finally {
            // pase lo que pase cierro la conexion pa no dejarla abierta
            if (conexion != null) {
                conexion.close();
            }
        }
    }
    // borra el producto por su id
    public void eliminar(int idProducto) throws SQLException {
        String sql = "DELETE FROM producto WHERE idproducto = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, idProducto);
            statement.executeUpdate(); // ejecuto el borrado
        }
    }
}
