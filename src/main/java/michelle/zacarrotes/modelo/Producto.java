package michelle.zacarrotes.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

import javafx.scene.image.Image;
public class Producto {

    private int idproducto;
    private String nombreproducto;
    private String marca;
    private BigDecimal precio;
    private LocalDate caducidad;
    private int cantidad;
    private String imagenUrl;
    private int idproveedor;
    private String nombreproveedor;
    private String telProveedor;
    private Image imagen;
    private boolean imagenIntentada;

    public Producto() {
    }

    public int getIdproducto() {
        return idproducto;
    }

    public void setIdproducto(int idproducto) {
        this.idproducto = idproducto;
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

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public LocalDate getCaducidad() {
        return caducidad;
    }

    public void setCaducidad(LocalDate caducidad) {
        this.caducidad = caducidad;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
        this.imagen = null;
        this.imagenIntentada = false;
    }

    public int getIdproveedor() {
        return idproveedor;
    }

    public void setIdproveedor(int idproveedor) {
        this.idproveedor = idproveedor;
    }

    public String getNombreproveedor() {
        return nombreproveedor;
    }

    public void setNombreproveedor(String nombreproveedor) {
        this.nombreproveedor = nombreproveedor;
    }

    public String getTelProveedor() {
        return telProveedor;
    }

    public void setTelProveedor(String telProveedor) {
        this.telProveedor = telProveedor;
    }
    public Image getImagen() {
        if (imagen == null && !imagenIntentada) {
            imagenIntentada = true;
            if (imagenUrl != null && !imagenUrl.trim().isEmpty()) {
                try {
                    imagen = new Image(imagenUrl.trim(), 32, 32, true, true, true);
                } catch (Exception e) {
                    imagen = null; 
                }
            }
        }
        return imagen;
    }
}
