/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package michelle.zacarrotes.modelo;

import java.math.BigDecimal;

/**
 * Modelo de detalle de venta (renglon del ticket).
 *
 * Refleja la tabla detalleventa (idventa, idproducto, cantidad, total,
 * subtotal) mas nombreproducto y marca que trae la vista v_ticket.
 *
 * Los nombres nombreproducto, cantidad y subtotal coinciden con los
 * PropertyValueFactory de tblTicket en venta.fxml.
 *
 * @author dana
 */
public class DetalleVenta {

    private int idVenta;
    private int idProducto;
    private String nombreproducto;
    private String marca;
    private int cantidad;
    private BigDecimal subtotal;
    private BigDecimal total;

    public DetalleVenta() {

    }

    public DetalleVenta(int idVenta, int idProducto, int cantidad, BigDecimal subtotal, BigDecimal total) {
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
        this.total = total;
    }

    public DetalleVenta(int idProducto, String nombreproducto, int cantidad, BigDecimal subtotal) {
        this.idProducto = idProducto;
        this.nombreproducto = nombreproducto;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreproducto() {
        return nombreproducto;
    }

    public void setNombreproducto(String nombreproducto) {
        this.nombreproducto = nombreproducto;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

}
