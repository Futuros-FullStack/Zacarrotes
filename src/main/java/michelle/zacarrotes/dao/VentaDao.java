package michelle.zacarrotes.dao;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import michelle.zacarrotes.modelo.DetalleVenta;
import michelle.zacarrotes.modelo.Venta;

public class VentaDao {

    public int registrarTicket(int idCliente, List<DetalleVenta> partidas) throws SQLException {
        String sql = "SELECT f_registrar_ticket(?, ?, ?)";

        Integer[] idProductos = new Integer[partidas.size()];
        Integer[] cantidades = new Integer[partidas.size()];
        for (int i = 0; i < partidas.size(); i++) {
            idProductos[i] = partidas.get(i).getIdProducto();
            cantidades[i] = partidas.get(i).getCantidad();
        }

        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)) {
            Array arrProductos = conexion.createArrayOf("integer", idProductos);
            Array arrCantidades = conexion.createArrayOf("integer", cantidades);
            statement.setInt(1, idCliente);
            statement.setArray(2, arrProductos);
            statement.setArray(3, arrCantidades);
            try (ResultSet resultado = statement.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }

    public void registrarVenta(int idCliente, int idProducto, int cantidad) throws SQLException {
        String sql = "CALL p_registro_venta(?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
                CallableStatement statement = conexion.prepareCall(sql)) {
            statement.setInt(1, idCliente);
            statement.setInt(2, idProducto);
            statement.setInt(3, cantidad);
            statement.execute();
        }
    }

    public void agregarProducto(int idVenta, int idProducto, int cantidad) throws SQLException {
        String sql = "CALL p_agregar_producto_venta(?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
                CallableStatement statement = conexion.prepareCall(sql)) {
            statement.setInt(1, idVenta);
            statement.setInt(2, idProducto);
            statement.setInt(3, cantidad);
            statement.execute();
        }
    }

    public void quitarProducto(int idVenta, int idProducto) throws SQLException {
        String sql = "CALL p_quitar_producto_venta(?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
                CallableStatement statement = conexion.prepareCall(sql)) {
            statement.setInt(1, idVenta);
            statement.setInt(2, idProducto);
            statement.execute();
        }
    }

    public void editarVenta(int idVenta, int idProducto, int cantidadNueva) throws SQLException {
        String sql = "CALL p_editar_venta(?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
                CallableStatement statement = conexion.prepareCall(sql)) {
            statement.setInt(1, idVenta);
            statement.setInt(2, idProducto);
            statement.setInt(3, cantidadNueva);
            statement.execute();
        }
    }

    public void eliminarVenta(int idVenta) throws SQLException {
        String sql = "CALL p_eliminar_venta(?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
                CallableStatement statement = conexion.prepareCall(sql)) {
            statement.setInt(1, idVenta);
            statement.execute();
        }
    }

    public List<Venta> listarVentas() throws SQLException {
        String sql = "SELECT DISTINCT idventa, fecha, total, nombre_cliente "
                + "FROM v_ticket ORDER BY fecha DESC";
        List<Venta> ventas = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql);
                ResultSet resultado = statement.executeQuery()) {
            while (resultado.next()) {
                Venta venta = new Venta();
                venta.setIdVenta(resultado.getInt("idventa"));
                Timestamp fecha = resultado.getTimestamp("fecha");
                venta.setFecha(fecha == null ? null : fecha.toLocalDateTime());
                venta.setTotal(resultado.getBigDecimal("total"));
                venta.setNombreCliente(resultado.getString("nombre_cliente"));
                ventas.add(venta);
            }
        }
        return ventas;
    }

    public List<DetalleVenta> listarTicket(int idVenta) throws SQLException {
        String sql = "SELECT idproducto, producto, marca, cantidad_comprada, subtotal "
                + "FROM v_ticket WHERE idventa = ? ORDER BY producto";
        List<DetalleVenta> partidas = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, idVenta);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    DetalleVenta partida = new DetalleVenta();
                    partida.setIdVenta(idVenta);
                    partida.setIdProducto(resultado.getInt("idproducto"));
                    partida.setNombreproducto(resultado.getString("producto"));
                    partida.setMarca(resultado.getString("marca"));
                    partida.setCantidad(resultado.getInt("cantidad_comprada"));
                    partida.setSubtotal(resultado.getBigDecimal("subtotal"));
                    partidas.add(partida);
                }
            }
        }
        return partidas;
    }
}
