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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableRow;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import michelle.zacarrotes.dao.ClienteDao;
import michelle.zacarrotes.dao.ProductoDao;
import michelle.zacarrotes.dao.VentaDao;
import michelle.zacarrotes.modelo.Cliente;
import michelle.zacarrotes.modelo.DetalleVenta;
import michelle.zacarrotes.modelo.Producto;


public class VentaController implements Initializable {

    private static final String TARJETA = "/vistas/comunes/tarjeta-producto.fxml";
    private static final int CLIENTE_GENERAL = 1; //Definimos que el cliente general será el #1 siempre en la base 

    @FXML private FlowPane boxTarjetas;
    @FXML private ComboBox<Cliente> cboCliente;
    @FXML private TableView<DetalleVenta> tblTicket;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colTicketSubtotal;
    @FXML private Label lblTotal;
    @FXML private Button btnCobrar;
    @FXML private Button btnCancelar;

    private final ProductoDao productoDAO = new ProductoDao(); //son los encargados de hablar con la base (instancio para tener acceso a ellos)
    private final ClienteDao clienteDAO = new ClienteDao();
    private final VentaDao ventaDAO = new VentaDao();

    private final ObservableList<DetalleVenta> ticket = FXCollections.observableArrayList(); //liste observable que cambia automaticamente en la interfaz, final porque nunca va a cambiar siempre será la misma 
    private final Map<Integer, Producto> catalogo = new HashMap<>(); //integer porque id es entero, producto objeto completo con nombre, stock, es como un diccionario que buscar rapidamente 

    @Override
    public void initialize(URL location, ResourceBundle resources) {
 
        configurarTablaTicket();
        configurarMenuClicDerecho();
        
        tblTicket.setItems(ticket); //conecta la tabla del ticket con la lista ticket 
        btnCobrar.setOnAction(e -> cobrar()); //cuando ocurra este metodo e tomara el valor o ejecutara cobrar
        btnCancelar.setOnAction(e -> cancelar());

        cargarClientes();
        cargarCatalogo();
        refrescarTotales();
    }


    private void configurarTablaTicket() {
    colTicketSubtotal.setCellFactory(col -> new TableCell<DetalleVenta, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal subtotal, boolean empty) {
            super.updateItem(subtotal, empty);
            
            if (empty == true || subtotal == null) {
                // Si está vacío o no hay dinero, no ponemos nada en el texto
                setText(null);
            } else {
                // Si sí hay datos, le damos formato.
                setText(String.format("$ %.2f", subtotal));
            }
        }
    });
}

     private void limpiarTicket() {
        ticket.clear(); //limpia la lista de memoria 
        cboCliente.getSelectionModel().clearSelection(); //limpia el cliente seleccionado
    }
    
    private void agregarAlTicket(Producto producto) {
        
        int piezasEnTicket = cantidadEnTicket(producto.getIdproducto());
        int disponible = producto.getCantidad() - piezasEnTicket;
        
        if (disponible <= 0) {
            alerta(Alert.AlertType.WARNING, "Sin existencia", "Ya no quedan piezas de \"" + producto.getNombreproducto() + "\".");
            return;
        }

     
        DetalleVenta renglon = buscarEnTicket(producto.getIdproducto());
        
        if (renglon == null) {
            // Caso a: Es la primera vez que agregamos este producto
            renglon = new DetalleVenta();
            renglon.setIdProducto(producto.getIdproducto());
            renglon.setNombreproducto(producto.getNombreproducto());
            renglon.setMarca(producto.getMarca());
            
            // Le ponemos 1 pieza y calculamos su subtotal (Precio x 1)
            renglon.setCantidad(1);
            renglon.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(1)));
            
            ticket.add(renglon);
            
        } else {
            // Caso b: El producto ya estaba, solo le sumamos 1
            int nuevaCantidad = renglon.getCantidad() + 1;
            renglon.setCantidad(nuevaCantidad);
            
            // Volvemos a calcular su subtotal (Precio x la nueva cantidad)
            renglon.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(nuevaCantidad)));
        }

        refrescarTotales();
    }

    private DetalleVenta buscarEnTicket(int idProducto) { //me entregará un objeto de detalle venta pero necesita que le de el idproducto
        for (DetalleVenta renglon : ticket) { //cada elemento que vaya revisando del ticket llamalo renglon remporalmente,
            if (renglon.getIdProducto() == idProducto) { //si el renglon que reviso coincide con el id del producto original retorna ese renglon y rompe el ciclo
                return renglon;
            }
        }
        return null;
    }

   private int cantidadEnTicket(int idProducto) {
        DetalleVenta renglonticket = buscarEnTicket(idProducto);
        
        if (renglonticket == null) {
            return 0; // Si no lo encontró, hay 0 piezas en el ticket
        } else {
            return renglonticket.getCantidad(); // Si sí lo encontró, nos dice cuántas hay
        }
    }
    
    
    private void refrescarTotales() {
        int articulos = 0; 
        BigDecimal total = BigDecimal.ZERO; //prepara y pon en 0 esa variable
    
        for (DetalleVenta renglon : ticket) { //recorre uno por uno todos los productos del ticket
            articulos = articulos + renglon.getCantidad(); //la cantidad de articulos sumale la canditad que está revisando.
            if (renglon.getSubtotal() != null) {
                total = total.add(renglon.getSubtotal()); //si el subtotal no está en blanco añadelo al total
            }
        }
        lblTotal.setText(String.format("$ %.2f", total));
        tblTicket.refresh();
    }

    private void cargarCatalogo() {
        boxTarjetas.getChildren().clear(); //limpia antes de mostrar
        catalogo.clear(); //limpia el catalogo

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
            }
        } catch (SQLException e) {
            alerta(Alert.AlertType.ERROR, "Catalogo","No se pudo cargar el catalogo de productos.\n" + e.getMessage());
        } catch (IOException e) {
            alerta(Alert.AlertType.ERROR, "Catalogo", "No se pudo cargar el diseño de la tarjeta de producto.\n" + e.getMessage());
        }
    }
    
    
    
    private void cargarClientes() {
        List<Cliente> clientes = clienteDAO.listarClientes(); //lista los clientes y los pinta en la lista automatica
        cboCliente.setItems(FXCollections.observableArrayList(clientes));
    }

    
    
    private void cobrar() {
        if (ticket.isEmpty()) {
            alerta(Alert.AlertType.WARNING, "Ticket vacío", "Agrega al menos un producto antes de cobrar.");
            return;
        }
        Cliente cliente = cboCliente.getValue();
        int idCliente = cliente == null ? CLIENTE_GENERAL : cliente.getIdCliente();

        try {
            int folio = ventaDAO.registrarTicket(idCliente, new ArrayList<>(ticket));
            alerta(Alert.AlertType.INFORMATION, "Venta", "Venta registrada con el folio #" + folio + ".");
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
        confirmacion.setContentText("¿Seguro que quieres vaciar el ticket?"); //pregunta en la ventanita
        Optional<ButtonType> resultado = confirmacion.showAndWait(); // muestra y congela la pantalla
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) { //el usuario presiono un boton y fue el de ok ejecuta limpiar y refescartotales
            limpiarTicket();
            refrescarTotales();
        }
    }

    
    // Método para sumar 1 pieza al renglón seleccionado
    private void AgregarUnaPieza(DetalleVenta renglonSeleccionado) {
        if (renglonSeleccionado == null) {
            return; 
        }

        // 1. Buscamos el producto original en el catálogo para ver su precio y stock
        Producto productoOriginal = catalogo.get(renglonSeleccionado.getIdProducto());
        
        // 2. Calculamos cuántos quedan disponibles
        int piezasEnTicket = renglonSeleccionado.getCantidad();
        int disponibles = productoOriginal.getCantidad() - piezasEnTicket;
        
        // 3. Revisamos si podemos sumarle
        if (disponibles <= 0) {
            alerta(Alert.AlertType.WARNING, "Sin existencia", "No hay más piezas en el inventario.");
        } else {
            int nuevaCantidad = piezasEnTicket + 1;
            renglonSeleccionado.setCantidad(nuevaCantidad);
            
            BigDecimal nuevoSubtotal = productoOriginal.getPrecio().multiply(BigDecimal.valueOf(nuevaCantidad));
            renglonSeleccionado.setSubtotal(nuevoSubtotal);
            
            refrescarTotales();
        }
    }

    // Método para restar 1 pieza al renglón seleccionado
    private void QuitarUnaPieza(DetalleVenta renglonSeleccionado) {
        if (renglonSeleccionado == null) {
            return;
        }

        Producto productoOriginal = catalogo.get(renglonSeleccionado.getIdProducto());
        int cantidadActual = renglonSeleccionado.getCantidad();

        // Si solo tiene 1 pieza y le restamos, mejor lo eliminamos de la lista
        if (cantidadActual == 1) {
            eliminarDelTicket(renglonSeleccionado);
        } else {
            int nuevaCantidad = cantidadActual - 1;
            renglonSeleccionado.setCantidad(nuevaCantidad);
            
            BigDecimal nuevoSubtotal = productoOriginal.getPrecio().multiply(BigDecimal.valueOf(nuevaCantidad));
            renglonSeleccionado.setSubtotal(nuevoSubtotal);
            
            refrescarTotales();
        }
    }

    // Método para eliminar todo el renglón del ticket
    private void eliminarDelTicket(DetalleVenta renglonSeleccionado) {
        if (renglonSeleccionado == null) {
            return;
        }
        
        // Removemos el producto de la lista en memoria
        ticket.remove(renglonSeleccionado);
        refrescarTotales();
    }
    
    // Método que crea el menú de clic derecho en la tabla
    private void configurarMenuClicDerecho() {
        
        // Le decimos a la tabla cómo construir sus renglones
        tblTicket.setRowFactory(tabla -> {
            TableRow<DetalleVenta> renglon = new TableRow<>();

            // 1. Creamos el menú y sus 3 opciones de texto
            ContextMenu menu = new ContextMenu();
            MenuItem opcionSumar = new MenuItem("Sumar 1 pieza");
            MenuItem opcionRestar = new MenuItem("Restar 1 pieza");
            MenuItem opcionEliminar = new MenuItem("Eliminar del ticket");

            // 2. Conectamos las opciones con los métodos que creamos arriba
            opcionSumar.setOnAction(evento -> {
                AgregarUnaPieza(renglon.getItem());
            });

            opcionRestar.setOnAction(evento -> {
                QuitarUnaPieza(renglon.getItem());
            });

            opcionEliminar.setOnAction(evento -> {
                eliminarDelTicket(renglon.getItem());
            });

            // 3. Metemos las opciones adentro del menú
            menu.getItems().add(opcionSumar);
            menu.getItems().add(opcionRestar);
            menu.getItems().add(opcionEliminar);

            // 4. Lógica tradicional: Si el renglón está vacío, quitamos el menú. Si tiene datos, se lo pegamos.
            renglon.emptyProperty().addListener((observable, estabaVacio, estaVacio) -> {
                if (estaVacio == true) {
                    renglon.setContextMenu(null);
                } else {
                    renglon.setContextMenu(menu);
                }
            });

            return renglon;
        });
    }
   

    private void alerta(Alert.AlertType tipo, String titulo, String mensaje) { //pinta las alertas del programa
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
