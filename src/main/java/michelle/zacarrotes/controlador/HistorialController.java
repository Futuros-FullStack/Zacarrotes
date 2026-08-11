package michelle.zacarrotes.controlador;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import michelle.zacarrotes.dao.VentaDao;
import michelle.zacarrotes.modelo.DetalleVenta;
import michelle.zacarrotes.modelo.Venta;

public class HistorialController implements Initializable {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label lblConteoVentas;
    @FXML private TableView<Venta> tblVentas;
    @FXML private TableColumn<Venta, Integer> colVentaFolio;
    @FXML private TableColumn<Venta, LocalDateTime> colVentaFecha;
    @FXML private TableColumn<Venta, BigDecimal> colVentaTotal;
    @FXML private Label lblClienteDetalle;
    @FXML private TableView<DetalleVenta> tblDetalle;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colDetalleSubtotal;
    @FXML private Label lblTotalDetalle;
    @FXML private Button btnCambiarCantidad;
    @FXML private Button btnQuitarProducto;
    @FXML private Button btnEliminarVenta;

    private final VentaDao ventaDAO = new VentaDao();

    private final ObservableList<Venta> ventas = FXCollections.observableArrayList();
    private final ObservableList<DetalleVenta> detalle = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTablaVentas();
        configurarTablaDetalle();
        configurarBotones();

        tblVentas.setItems(ventas);
        tblDetalle.setItems(detalle);

        cargarVentas();
    }

    private void configurarTablaVentas() {
        colVentaFolio.setCellFactory(col -> {
            TableCell<Venta, Integer> celda = new TableCell<Venta, Integer>() {
                @Override
                protected void updateItem(Integer folio, boolean empty) {
                    super.updateItem(folio, empty);
                    setText(empty || folio == null ? null : "#" + folio);
                }
            };
            celda.getStyleClass().add("celda-folio");
            return celda;
        });

        colVentaFecha.setCellFactory(col -> new TableCell<Venta, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime fecha, boolean empty) {
                super.updateItem(fecha, empty);
                setText(empty || fecha == null ? null : FORMATO_FECHA.format(fecha));
            }
        });

        colVentaTotal.setCellFactory(col -> new TableCell<Venta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal total, boolean empty) {
                super.updateItem(total, empty);
                setText(empty || total == null ? null : String.format("$ %.2f", total));
            }
        });

        tblVentas.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, actual) -> cargarDetalle(actual));
    }

    private void configurarTablaDetalle() {
        colDetalleSubtotal.setCellFactory(col -> new TableCell<DetalleVenta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal subtotal, boolean empty) {
                super.updateItem(subtotal, empty);
                setText(empty || subtotal == null ? null : String.format("$ %.2f", subtotal));
            }
        });
    }

    private void configurarBotones() {
        btnCambiarCantidad.setOnAction(e -> cambiarCantidadDelRenglon());
        btnQuitarProducto.setOnAction(e -> quitarProductoDelTicket());
        btnEliminarVenta.setOnAction(e -> eliminarVentaCompleta());

        BooleanBinding sinVenta = tblVentas.getSelectionModel().selectedItemProperty().isNull();
        BooleanBinding sinRenglon = tblDetalle.getSelectionModel().selectedItemProperty().isNull();

        btnEliminarVenta.disableProperty().bind(sinVenta);
        btnCambiarCantidad.disableProperty().bind(sinRenglon);
        btnQuitarProducto.disableProperty().bind(sinRenglon);
    }


    private void cargarVentas() {
        Venta seleccionada = tblVentas.getSelectionModel().getSelectedItem();
        int folioPrevio = seleccionada == null ? 0 : seleccionada.getIdVenta();

        try {
            ventas.setAll(ventaDAO.listarVentas());
            lblConteoVentas.setText(String.valueOf(ventas.size()));
            reseleccionar(folioPrevio);
        } catch (SQLException e) {
            alerta(Alert.AlertType.ERROR, "Historial", "No se pudieron cargar las ventas.\n" + e.getMessage());
        } 
    }

    private void reseleccionar(int folio) {
        for (Venta venta : ventas) {
            if (venta.getIdVenta() == folio) {
                tblVentas.getSelectionModel().select(venta);
                return;
            }
        }
        tblVentas.getSelectionModel().clearSelection();
        limpiarDetalle();
    }

    private void cargarDetalle(Venta venta) {
        if (venta == null) {
            limpiarDetalle();
            return;
        }
        lblClienteDetalle.setText("Cliente: " + venta.getNombreCliente());      
        lblTotalDetalle.setText(String.format("$ %.2f", venta.getTotal())); 

        try {
            detalle.setAll(ventaDAO.listarTicket(venta.getIdVenta()));
          
            int articulos = 0;
            for (DetalleVenta renglon : detalle) {
                articulos = articulos + renglon.getCantidad();
            }
            
        } catch (SQLException e) {
            alerta(Alert.AlertType.ERROR, "Ticket", "No se pudo cargar el detalle de la venta #" + venta.getIdVenta()+ ".\n" + e.getMessage());
        }
    }
    private void limpiarDetalle() {
        detalle.clear();
        lblClienteDetalle.setText("Selecciona una venta de la lista.");
        lblTotalDetalle.setText("$ 0.00");
    }


    private void cambiarCantidadDelRenglon() {
        DetalleVenta renglon = tblDetalle.getSelectionModel().getSelectedItem();
        if (renglon == null) {
            return;
        }

        TextInputDialog dialogo = new TextInputDialog(String.valueOf(renglon.getCantidad()));
        dialogo.setTitle("Cambiar cantidad");
        dialogo.setHeaderText(null);
        dialogo.setContentText("Piezas de \"" + renglon.getNombreproducto() + "\":");

        Optional<String> respuesta = dialogo.showAndWait();
        if (!respuesta.isPresent()) {
            return;
        }

        int cantidadNueva;
        try {
            cantidadNueva = Integer.parseInt(respuesta.get().trim());
        } catch (NumberFormatException e) {
            alerta(Alert.AlertType.WARNING, "Validación", "La cantidad debe ser un número entero.");
            return;
        }
        
        if (cantidadNueva <= 0) {
            alerta(Alert.AlertType.WARNING, "Validación", "La cantidad debe ser mayor a cero.\n");
            return;
        }

        try {
            ventaDAO.editarVenta(renglon.getIdVenta(), renglon.getIdProducto(), cantidadNueva);
            cargarVentas();
        } catch (SQLException e) {
            alerta(Alert.AlertType.ERROR, "No se pudo cambiar la cantidad", mensajeDeError(e));
        } 
    }

    private void quitarProductoDelTicket() {
        DetalleVenta renglon = tblDetalle.getSelectionModel().getSelectedItem();
        if (renglon == null) {
            return;
        }

        if (detalle.size() == 1) {
            alerta(Alert.AlertType.WARNING, "Último producto", "\"" + renglon.getNombreproducto() + "\" es el único producto de la venta #" + renglon.getIdVenta() + ".\n" + "Si lo quitas la venta se quedaría vacía.");
            return;
        }

        if (!confirmar("Quitar producto", "¿Quitar \"" + renglon.getNombreproducto() + "\" de la venta #" + renglon.getIdVenta() + "?\nLas " + renglon.getCantidad() + " piezas regresan al inventario.")) {
            return;
        }

        try {
            ventaDAO.quitarProducto(renglon.getIdVenta(), renglon.getIdProducto());
            cargarVentas();
        } catch (SQLException e) {
            alerta(Alert.AlertType.ERROR, "No se pudo quitar el producto", mensajeDeError(e));
        }
    }

    private void eliminarVentaCompleta() {
        Venta venta = tblVentas.getSelectionModel().getSelectedItem();
        if (venta == null) {
            return;
        }

        if (!confirmar("Eliminar venta", "¿Eliminar la venta #" + venta.getIdVenta() + " por " + String.format("$ %.2f", venta.getTotal()) + "?\n" + "Esta opcion no se puede revertir")) {
            return;
        }

        try {
            ventaDAO.eliminarVenta(venta.getIdVenta());
            cargarVentas();
        } catch (SQLException e) {
            alerta(Alert.AlertType.ERROR, "No se pudo eliminar la venta", mensajeDeError(e));
        } 
    }

    
    private String mensajeDeError(SQLException e) {
        if ("23514".equals(e.getSQLState())) {
            return "No hay suficiente inventario para esa cantidad.";
        }
        return e.getMessage();
    }

    private boolean confirmar(String titulo, String mensaje) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle(titulo);
        confirmacion.setHeaderText(null);
        confirmacion.setContentText(mensaje);
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }

    private void alerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
