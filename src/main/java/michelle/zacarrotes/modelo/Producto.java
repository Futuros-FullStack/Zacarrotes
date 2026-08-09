package michelle.zacarrotes.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

import javafx.scene.image.Image;

/**
 * Modelo de producto.
 *
 * Refleja las columnas de la vista v_catalogo_completo (idproducto, producto,
 * marca, precio, caducidad, cantidad, imagen_url, proveedor, tel_proveedor)
 * mas la FK idproveedor que necesitan los procedures.
 *
 * Los nombres de las propiedades (imagen, nombreproducto, marca, precio,
 * cantidad, caducidad, nombreproveedor) coinciden con los PropertyValueFactory
 * declarados en productos.fxml.
 */
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

    // Imagen cargada de forma perezosa a partir de imagenUrl (puede quedar null).
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
        // Al cambiar la URL se invalida la imagen cacheada.
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

    /**
     * Imagen para la miniatura de la tabla y la vista previa. Se carga una sola
     * vez desde imagenUrl; si la URL es nula o falla, devuelve null y la celda
     * simplemente no muestra miniatura.
     */
    public Image getImagen() {
        if (imagen == null && !imagenIntentada) {
            imagenIntentada = true;
            if (imagenUrl != null && !imagenUrl.trim().isEmpty()) {
                try {
                    imagen = new Image(imagenUrl.trim(), 32, 32, true, true, true);
                } catch (Exception e) {
                    imagen = null; // URL invalida o no accesible: sin miniatura.
                }
            }
        }
        return imagen;
    }
}
