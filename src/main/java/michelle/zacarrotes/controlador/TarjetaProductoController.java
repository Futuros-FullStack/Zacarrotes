package michelle.zacarrotes.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import michelle.zacarrotes.modelo.Producto;


public class TarjetaProductoController {

    @FXML private VBox pnlTarjeta;
    @FXML private ImageView imgFoto;
    @FXML private Label lblNombre;
    @FXML private Label lblMarca;
    @FXML private Label lblPrecio;
    @FXML private Label lblExistencia;

    private Producto producto;

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

 
    public void mostrarDisponible(int disponible) {
        lblExistencia.setText(disponible + " pz");

        String estado = disponible <= 0 ? "pill-danger" : disponible <= 5 ? "pill-warn" : "pill-ok";
        lblExistencia.getStyleClass().setAll("pill", estado);

        pnlTarjeta.setOpacity(disponible <= 0 ? 0.45 : 1.0);
    }

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
