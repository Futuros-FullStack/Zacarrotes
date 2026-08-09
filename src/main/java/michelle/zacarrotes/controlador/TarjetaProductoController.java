package michelle.zacarrotes.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import michelle.zacarrotes.modelo.Producto;

/**
 * Controlador de UNA tarjeta de producto (comunes/tarjeta-producto.fxml).
 *
 * Solo rellena los fx:id de la plantilla; no arma nodos a mano. El
 * VentaController carga esta vista una vez por producto del catalogo con un
 * FXMLLoader nuevo y le pasa el producto con mostrar().
 *
 * La pastilla de existencia NO muestra la cantidad de la base tal cual, sino lo
 * que queda DISPONIBLE (cantidad menos lo que ya se llevan en el ticket); por eso
 * mostrarDisponible() es publico y el VentaController lo vuelve a llamar cada vez
 * que cambia el ticket.
 */
public class TarjetaProductoController {

    @FXML private VBox pnlTarjeta;
    @FXML private ImageView imgFoto;
    @FXML private Label lblNombre;
    @FXML private Label lblMarca;
    @FXML private Label lblPrecio;
    @FXML private Label lblExistencia;

    private Producto producto;

    /** Pinta los datos del producto. Se llama una sola vez por tarjeta. */
    public void mostrar(Producto producto) {
        this.producto = producto;
        lblNombre.setText(producto.getNombreproducto());
        lblMarca.setText(producto.getMarca());
        lblPrecio.setText(producto.getPrecio() == null
                ? "$ 0.00"
                : String.format("$ %.2f", producto.getPrecio()));
        mostrarFoto(producto.getImagenUrl());
        mostrarDisponible(producto.getCantidad());
    }

    /**
     * Actualiza la pastilla con las piezas que quedan disponibles. En 0 la
     * tarjeta se ve apagada para que se note que ya no se puede agregar.
     */
    public void mostrarDisponible(int disponible) {
        lblExistencia.setText(disponible + " pz");

        String estado = disponible <= 0 ? "pill-danger" : disponible <= 5 ? "pill-warn" : "pill-ok";
        lblExistencia.getStyleClass().setAll("pill", estado);

        pnlTarjeta.setOpacity(disponible <= 0 ? 0.45 : 1.0);
    }

    /**
     * La foto se carga aparte de Producto.getImagen() porque esa viene a 32 px
     * (es la miniatura de la tabla de Productos) y aqui el recuadro es de 88.
     */
    private void mostrarFoto(String url) {
        if (url == null || url.trim().isEmpty()) {
            imgFoto.setImage(null);
            return;
        }
        try {
            imgFoto.setImage(new Image(url.trim(), 88, 88, true, true, true));
        } catch (Exception e) {
            imgFoto.setImage(null); // URL invalida: la tarjeta se queda sin foto.
        }
    }

    public Producto getProducto() {
        return producto;
    }
}
