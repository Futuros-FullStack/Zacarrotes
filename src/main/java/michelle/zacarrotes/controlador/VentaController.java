package michelle.zacarrotes.controlador;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import michelle.zacarrotes.dao.ClienteDao;
import michelle.zacarrotes.dao.ProductoDao;
import michelle.zacarrotes.dao.VentaDao;
import michelle.zacarrotes.modelo.Cliente;
import michelle.zacarrotes.modelo.DetalleVenta;
import michelle.zacarrotes.modelo.Producto;


public class VentaController implements Initializable {

    private static final String TARJETA = "/vistas/comunes/tarjeta-producto.fxml";
    private static final int CLIENTE_MOSTRADOR = 1; //para el cliente desconocido 

    @FXML private Label lblConteo;
    @FXML private FlowPane boxTarjetas;
    @FXML private Label lblFolio;
    @FXML private ComboBox<Cliente> cboCliente;
    @FXML private TableView<DetalleVenta> tblTicket;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colTicketSubtotal;
    @FXML private Label lblArticulos;
    @FXML private Label lblTotal;
    @FXML private Button btnCobrar;
    @FXML private Button btnCancelar;

    private final ProductoDao productoDAO = new ProductoDao();
    private final ClienteDao clienteDAO = new ClienteDao();
    private final VentaDao ventaDAO = new VentaDao();

    private final ObservableList<DetalleVenta> ticket = FXCollections.observableArrayList();
    private final Map<Integer, Producto> catalogo = new HashMap<>();
    private final Map<Integer, TarjetaProductoController> tarjetas = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarComboClientes();
        configurarTablaTicket();

        tblTicket.setItems(ticket);
        btnCobrar.setOnAction(e -> cobrar());
        btnCancelar.setOnAction(e -> cancelar());

        cargarClientes();
        cargarCatalogo();
        refrescarTotales();
    }


    private void configurarComboClientes() {
        cboCliente.setConverter(new StringConverter<Cliente>() {
            @Override
            public String toString(Cliente cliente) {
                return cliente == null ? "" : cliente.getNombre();
            }

            @Override
            public Cliente fromString(String string) {
                return null;
            }
        });
        cboCliente.setCellFactory(cb -> new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });
    }

    private void configurarTablaTicket() {
        colTicketSubtotal.setCellFactory(col -> new TableCell<DetalleVenta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal subtotal, boolean empty) {
                super.updateItem(subtotal, empty);
                setText(empty || subtotal == null ? null : String.format("$ %.2f", subtotal));
            }
        });

        tblTicket.setRowFactory(tv -> {
            TableRow<DetalleVenta> fila = new TableRow<>();

            MenuItem itemMas = new MenuItem("Agregar una pieza");
            itemMas.setOnAction(e -> sumarPiezas(fila.getItem(), 1));

            MenuItem itemMenos = new MenuItem("Quitar una pieza");
            itemMenos.setOnAction(e -> sumarPiezas(fila.getItem(), -1));

            MenuItem itemCantidad = new MenuItem("Cambiar cantidad...");
            itemCantidad.setOnAction(e -> pedirCantidad(fila.getItem()));

            MenuItem itemQuitar = new MenuItem("Quitar del ticket");
            itemQuitar.setOnAction(e -> quitarDelTicket(fila.getItem()));

            ContextMenu menu = new ContextMenu(itemMas, itemMenos, itemCantidad,
                    new SeparatorMenuItem(), itemQuitar);

            fila.contextMenuProperty().bind(
                    Bindings.when(fila.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(menu));
            return fila;
        });
    }


    private void cargarClientes() {
        List<Cliente> clientes = clienteDAO.listarClientes();
        cboCliente.setItems(FXCollections.observableArrayList(clientes));
    }

    private void cargarCatalogo() {
        boxTarjetas.getChildren().clear();
        catalogo.clear();
        tarjetas.clear();

        try {
            List<Producto> productos = productoDAO.listarTodos();
            for (Producto producto : productos) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(TARJETA));
                VBox tarjeta = loader.load();

                TarjetaProductoController control = loader.getController();
                control.mostrar(producto);
                tarjeta.setOnMouseClicked(e -> agregarAlTicket(producto));

                boxTarjetas.getChildren().add(tarjeta);
                catalogo.put(producto.getIdproducto(), producto);
                tarjetas.put(producto.getIdproducto(), control);
            }
            lblConteo.setText(productos.size() + (productos.size() == 1 ? " artículo" : " artículos"));
        } catch (SQLException e) {
            alerta(Alert.AlertType.ERROR, "Catálogo",
                    "No se pudo cargar el catálogo de productos.\n" + e.getMessage());
        } catch (IOException e) {
            alerta(Alert.AlertType.ERROR, "Catálogo",
                    "No se pudo cargar el diseño de la tarjeta de producto.\n" + e.getMessage());
        } catch (RuntimeException e) {
            alerta(Alert.AlertType.ERROR, "Conexión", e.getMessage());
        }
    }

 
    private void agregarAlTicket(Producto producto) {
        
        int disponible = producto.getCantidad() - cantidadEnTicket(producto.getIdproducto());
        if (disponible <= 0) {
            alerta(Alert.AlertType.WARNING, "Sin existencia",
                    "Ya no quedan piezas de \"" + producto.getNombreproducto() + "\".");
            return;
        }

        DetalleVenta renglon = buscarEnTicket(producto.getIdproducto());
        if (renglon == null) {
            renglon = new DetalleVenta();
            renglon.setIdProducto(producto.getIdproducto());
            renglon.setNombreproducto(producto.getNombreproducto());
            renglon.setMarca(producto.getMarca());
            ticket.add(renglon);
        }
        fijarCantidad(renglon, producto, renglon.getCantidad() + 1);

        refrescarTotales();
    }

    private void sumarPiezas(DetalleVenta renglon, int piezas) {
        if (renglon != null) {
            cambiarCantidad(renglon, renglon.getCantidad() + piezas);
        }
    }

    private void pedirCantidad(DetalleVenta renglon) {
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
        try {
            cambiarCantidad(renglon, Integer.parseInt(respuesta.get().trim()));
        } catch (NumberFormatException e) {
            alerta(Alert.AlertType.WARNING, "Validación", "La cantidad debe ser un número entero.");
        }
    }

    private void cambiarCantidad(DetalleVenta renglon, int cantidadNueva) {
        if (renglon == null) {
            return;
        }
        if (cantidadNueva <= 0) {
            quitarDelTicket(renglon);
            return;
        }

        Producto producto = catalogo.get(renglon.getIdProducto());
        if (cantidadNueva > producto.getCantidad()) {
            alerta(Alert.AlertType.WARNING, "Sin existencia",
                    "Solo hay " + producto.getCantidad() + " piezas de \""
                    + producto.getNombreproducto() + "\".");
            return;
        }

        fijarCantidad(renglon, producto, cantidadNueva);
        refrescarTotales();
    }

    private void fijarCantidad(DetalleVenta renglon, Producto producto, int cantidad) {
        renglon.setCantidad(cantidad);
        renglon.setSubtotal(precioDe(producto).multiply(BigDecimal.valueOf(cantidad)));
    }

    private void quitarDelTicket(DetalleVenta renglon) {
        if (renglon != null) {
            ticket.remove(renglon);
            refrescarTotales();
        }
    }

    private DetalleVenta buscarEnTicket(int idProducto) {
        for (DetalleVenta renglon : ticket) {
            if (renglon.getIdProducto() == idProducto) {
                return renglon;
            }
        }
        return null;
    }

    private int cantidadEnTicket(int idProducto) {
        DetalleVenta renglon = buscarEnTicket(idProducto);
        return renglon == null ? 0 : renglon.getCantidad();
    }

    private BigDecimal precioDe(Producto producto) {
        return producto.getPrecio() == null ? BigDecimal.ZERO : producto.getPrecio();
    }

    
    private void refrescarTotales() {
        int articulos = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleVenta renglon : ticket) {
            articulos += renglon.getCantidad();
            if (renglon.getSubtotal() != null) {
                total = total.add(renglon.getSubtotal());
            }
        }

        lblArticulos.setText(String.valueOf(articulos));
        lblTotal.setText(String.format("$ %.2f", total));
        tblTicket.refresh();

        for (Map.Entry<Integer, TarjetaProductoController> entrada : tarjetas.entrySet()) {
            Producto producto = catalogo.get(entrada.getKey());
            entrada.getValue().mostrarDisponible(
                    producto.getCantidad() - cantidadEnTicket(entrada.getKey()));
        }
    }


    private void cobrar() {
        if (ticket.isEmpty()) {
            alerta(Alert.AlertType.WARNING, "Ticket vacío",
                    "Agrega al menos un producto antes de cobrar.");
            return;
        }

        Cliente cliente = cboCliente.getValue();
        int idCliente = cliente == null ? CLIENTE_MOSTRADOR : cliente.getIdCliente();

        try {
            int folio = ventaDAO.registrarTicket(idCliente, new ArrayList<>(ticket));
            lblFolio.setText("#" + folio);
            alerta(Alert.AlertType.INFORMATION, "Venta",
                    "Venta registrada con el folio #" + folio + ".");

            limpiarTicket();
            cargarCatalogo();
            refrescarTotales();
        } catch (SQLException e) {
       
            alerta(Alert.AlertType.ERROR, "No se pudo cobrar", e.getMessage());
        } catch (RuntimeException e) {
            alerta(Alert.AlertType.ERROR, "Conexión", e.getMessage());
        }
    }

    private void cancelar() {
        if (ticket.isEmpty()) {
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cancelar venta");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Seguro que quieres vaciar el ticket?");
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            limpiarTicket();
            refrescarTotales();
        }
    }

    private void limpiarTicket() {
        ticket.clear();
        cboCliente.getSelectionModel().clearSelection();
    }

    private void alerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
