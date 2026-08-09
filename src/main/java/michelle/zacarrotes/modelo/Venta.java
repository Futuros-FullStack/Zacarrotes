/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package michelle.zacarrotes.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo de venta.
 *
 * Refleja la tabla venta (idventa, fecha, idcliente, total) mas el
 * nombreCliente que trae la vista v_ticket para mostrarlo en el ticket.
 *
 * @author dana
 */
public class Venta {

    private int idVenta;
    private LocalDateTime fecha;
    private int idCliente;
    private BigDecimal total;
    private String nombreCliente;

    public Venta() {

    }

    public Venta(int idVenta, LocalDateTime fecha, int idCliente, BigDecimal total) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.idCliente = idCliente;
        this.total = total;
    }

    public Venta(int idCliente, BigDecimal total) {
        this.idCliente = idCliente;
        this.total = total;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

}
