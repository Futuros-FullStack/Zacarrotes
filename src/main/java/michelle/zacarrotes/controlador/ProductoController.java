package michelle.zacarrotes.controlador;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import michelle.zacarrotes.dao.ProductoDao;
import michelle.zacarrotes.dao.ProveedorDao;
import michelle.zacarrotes.modelo.Producto;
import michelle.zacarrotes.modelo.Proveedor;

/**
 * Controlador de la vista de Productos (productos.fxml).
 *
 * Un solo boton "Guardar": si NO hay fila seleccionada inserta
 * (p_insertar_producto); si HAY fila seleccionada edita todos los campos con
 * p_editar_producto_completo (nombre, marca, precio, imagen, cantidad, caducidad
 * y proveedor, con semantica de "fijar" el valor, no sumar).
 */
public class ProductoController implements Initializable {

    @FXML private BorderPane pnlRaiz;
    @FXML private ImageView imgPreview;
    @FXML private TextField txtNombre;
    @FXML private TextField txtMarca;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtCantidad;
    @FXML private DatePicker dtpCaducidad;
    @FXML private ComboBox<Proveedor> cboProveedor;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, Image> colIcono;
    @FXML private TableColumn<Producto, String> colProducto;
    @FXML private TableColumn<Producto, String> colMarca;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colCantidad;
    @FXML private TableColumn<Producto, LocalDate> colCaducidad;
    @FXML private TableColumn<Producto, String> colProveedor;

    @FXML private Label lblTotalProductos;

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ProductoDao productoDAO = new ProductoDao();
    private final ProveedorDao proveedorDAO = new ProveedorDao();
    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();

    private Producto productoSeleccionado;
    // URL/ruta de la imagen elegida en el FileChooser (se guarda tal cual en imagen_url).
    private String imagenUrlSeleccionada;

    // Menu contextual (clic izquierdo) para borrar la imagen del preview.
    private ContextMenu menuImagen;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarComboProveedores();
        configurarColumnas();
        configurarMenuImagen();
        configurarDeseleccion();

        tblProductos.setItems(listaProductos);
        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                cargarEnFormulario(seleccionado);
            }
        });

        cargarProveedores();
        cargarTabla();
    }

    // ================= Configuracion de la UI =================

    private void configurarComboProveedores() {
        cboProveedor.setConverter(new StringConverter<Proveedor>() {
            @Override
            public String toString(Proveedor proveedor) {
                return proveedor == null ? "" : proveedor.getNombreProveedor();
            }

            @Override
            public Proveedor fromString(String string) {
                return null; // No editable.
            }
        });
        cboProveedor.setCellFactory(cb -> new ListCell<Proveedor>() {
            @Override
            protected void updateItem(Proveedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreProveedor());
            }
        });
    }

    private void configurarColumnas() {
        // Miniatura: la columna entrega el Image (Producto.getImagen()); aqui solo
        // se mete en un ImageView de 32 px.
        colIcono.setCellFactory(col -> new TableCell<Producto, Image>() {
            private final ImageView vista = new ImageView();
            {
                vista.setFitWidth(32);
                vista.setFitHeight(32);
                vista.setPreserveRatio(true);
            }
            @Override
            protected void updateItem(Image imagen, boolean empty) {
                super.updateItem(imagen, empty);
                vista.setImage(imagen);
                setGraphic(empty || imagen == null ? null : vista);
            }
        });

        colPrecio.setCellFactory(col -> new TableCell<Producto, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal precio, boolean empty) {
                super.updateItem(precio, empty);
                setText(empty || precio == null ? null : String.format("$ %.2f", precio));
            }
        });

        // Semaforo de caducidad como pastilla de color.
        colCaducidad.setCellFactory(col -> new TableCell<Producto, LocalDate>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean empty) {
                super.updateItem(fecha, empty);
                getStyleClass().removeAll("pill", "pill-ok", "pill-warn", "pill-danger");
                if (empty || fecha == null) {
                    setText(null);
                    return;
                }
                setText(fecha.format(FECHA));
                long dias = ChronoUnit.DAYS.between(LocalDate.now(), fecha);
                String estado = dias <= 7 ? "pill-danger" : dias <= 30 ? "pill-warn" : "pill-ok";
                getStyleClass().addAll("pill", estado);
            }
        });
    }

    /**
     * Menu contextual del preview de imagen. Con clic IZQUIERDO sobre una imagen
     * ya cargada aparece un ContextMenu con la unica opcion "Borrar imagen".
     * Si el recuadro esta vacio (placeholder) el clic no hace nada.
     *
     * Este flujo es independiente del boton "Elegir imagen": borrar solo limpia
     * el preview y la variable interna; en modo edicion la baja real de imagen_url
     * (a null) ocurre hasta que el usuario presiona "Guardar".
     */
    private void configurarMenuImagen() {
        MenuItem itemBorrar = new MenuItem("Borrar imagen");
        itemBorrar.setOnAction(e -> borrarImagen());

        menuImagen = new ContextMenu(itemBorrar);
        menuImagen.getStyleClass().add("menu-imagen");

        imgPreview.setOnMouseClicked(e -> {
            // Solo clic izquierdo y solo si hay una imagen que borrar.
            if (e.getButton() == MouseButton.PRIMARY && imgPreview.getImage() != null) {
                menuImagen.show(imgPreview, e.getScreenX(), e.getScreenY());
            }
        });
    }

    private void borrarImagen() {
        // Por seguridad: si ya no hay imagen, no hay nada que hacer.
        if (imgPreview.getImage() == null) {
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Quitar imagen");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Seguro que quieres quitar esta imagen?");
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            // Se limpia el preview (vuelve el placeholder) y la ruta interna. En
            // modo edicion, imagen_url se pondra a null al presionar "Guardar",
            // sin borrar la fila de la base de datos.
            imgPreview.setImage(null);
            imagenUrlSeleccionada = null;
        }
    }

    /**
     * Clic "fuera de la tabla": al pulsar en cualquier zona de la vista que no
     * sea una fila de la tabla ni un control de entrada (formulario en blanco,
     * areas vacias, etiquetas...), se deselecciona la fila y el formulario
     * vuelve a modo "nuevo producto" reutilizando {@link #limpiar()}.
     *
     * El manejador va en el panel raiz y los eventos de los hijos llegan por
     * propagacion. Para no estorbar el uso normal, {@link #conservaSeleccion}
     * ignora los clics sobre TextField, DatePicker, ComboBox, Button, la tabla
     * y el preview de imagen (que ya tiene su propio menu contextual).
     *
     * Solo se limpia cuando hay un producto seleccionado (modo edicion); asi un
     * clic en un area vacia no borra un alta que el usuario este capturando.
     */
    private void configurarDeseleccion() {
        pnlRaiz.setOnMouseClicked(e -> {
            if (conservaSeleccion(e.getTarget())) {
                return;
            }
            if (productoSeleccionado != null) {
                limpiar();
            }
        });
    }

    /**
     * @return true si el nodo pulsado (o alguno de sus ancestros) es un control
     *         con el que el usuario debe poder interactuar sin perder la
     *         seleccion: la tabla, los campos del formulario, los botones o el
     *         preview de imagen.
     */
    private boolean conservaSeleccion(Object destino) {
        Node nodo = (destino instanceof Node) ? (Node) destino : null;
        while (nodo != null) {
            if (nodo instanceof TableView
                    || nodo instanceof TextField
                    || nodo instanceof DatePicker
                    || nodo instanceof ComboBox
                    || nodo instanceof Button
                    || nodo instanceof ImageView) {
                return true;
            }
            nodo = nodo.getParent();
        }
        return false;
    }

    // ================= Carga de datos =================

    private void cargarProveedores() {
        try {
            List<Proveedor> proveedores = proveedorDAO.ListarTodos();
            cboProveedor.setItems(FXCollections.observableArrayList(proveedores));
        } catch (RuntimeException e) {
            // p.ej. falta una variable de entorno de la conexion.
            alerta(Alert.AlertType.ERROR, "Conexión", e.getMessage());
        }
    }

    private void cargarTabla() {
        try {
            List<Producto> productos = productoDAO.listarTodos();
            listaProductos.setAll(productos);
            lblTotalProductos.setText(String.valueOf(listaProductos.size()));
        } catch (SQLException e) {
            alerta(Alert.AlertType.ERROR, "Catálogo",
                    "No se pudo cargar el catálogo de productos.\n" + e.getMessage());
        } catch (RuntimeException e) {
            alerta(Alert.AlertType.ERROR, "Conexión", e.getMessage());
        }
    }

    private void cargarEnFormulario(Producto producto) {
        productoSeleccionado = producto;
        txtNombre.setText(producto.getNombreproducto());
        txtMarca.setText(producto.getMarca());
        txtPrecio.setText(producto.getPrecio() == null ? "" : producto.getPrecio().toPlainString());
        // Edicion "fija": lo que se ve es lo que se guarda, por eso se precarga la
        // cantidad total actual (p_editar_producto_completo la reemplaza, no la suma).
        txtCantidad.setText(String.valueOf(producto.getCantidad()));
        dtpCaducidad.setValue(producto.getCaducidad());
        seleccionarProveedorPorNombre(producto.getNombreproveedor());
        imagenUrlSeleccionada = producto.getImagenUrl();
        mostrarVistaPrevia(imagenUrlSeleccionada);
    }

    private void seleccionarProveedorPorNombre(String nombre) {
        if (nombre != null) {
            for (Proveedor proveedor : cboProveedor.getItems()) {
                if (nombre.equals(proveedor.getNombreProveedor())) {
                    cboProveedor.getSelectionModel().select(proveedor);
                    return;
                }
            }
        }
        cboProveedor.getSelectionModel().clearSelection();
    }

    private void mostrarVistaPrevia(String url) {
        if (url == null || url.trim().isEmpty()) {
            imgPreview.setImage(null);
            return;
        }
        try {
            imgPreview.setImage(new Image(url.trim(), 70, 70, true, true, true));
        } catch (Exception e) {
            imgPreview.setImage(null);
        }
    }

    // ================= Acciones =================

    @FXML
    private void elegirImagen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Elegir imagen del producto");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes PNG o JPG", "*.png", "*.jpg", "*.jpeg"));
        File archivo = chooser.showOpenDialog(imgPreview.getScene().getWindow());
        if (archivo != null) {
            // Se guarda la URI absoluta del archivo (file:/...): se carga directo con
            // Image(url) y se persiste tal cual en imagen_url.
            imagenUrlSeleccionada = archivo.toURI().toString();
            mostrarVistaPrevia(imagenUrlSeleccionada);
        }
    }

    @FXML
    private void guardar() {
        if (productoSeleccionado == null) {
            insertar();
        } else {
            editar();
        }
    }

    private void insertar() {
        String nombre = valorTrim(txtNombre);
        String marca = valorTrim(txtMarca);
        if (nombre.isEmpty() || marca.isEmpty()) {
            alerta(Alert.AlertType.WARNING, "Validación", "Nombre y marca son obligatorios.");
            return;
        }
        BigDecimal precio = parsearPrecio();
        if (precio == null) {
            return;
        }
        Integer cantidad = parsearCantidad(true);
        if (cantidad == null) {
            return;
        }
        LocalDate caducidad = validarCaducidad();
        if (caducidad == null) {
            return;
        }
        Proveedor proveedor = cboProveedor.getValue();
        if (proveedor == null) {
            alerta(Alert.AlertType.WARNING, "Validación", "Selecciona un proveedor.");
            return;
        }

        Producto nuevo = new Producto();
        nuevo.setNombreproducto(nombre);
        nuevo.setMarca(marca);
        nuevo.setPrecio(precio);
        nuevo.setCantidad(cantidad);
        nuevo.setCaducidad(caducidad);
        nuevo.setImagenUrl(imagenUrlSeleccionada);
        nuevo.setIdproveedor(proveedor.getIdProveedor());

        try {
            productoDAO.insertar(nuevo);
            cargarTabla();
            limpiar();
            alerta(Alert.AlertType.INFORMATION, "Productos", "Producto agregado correctamente.");
        } catch (SQLException e) {
            alerta(Alert.AlertType.ERROR, "Productos", "No se pudo agregar el producto.\n" + e.getMessage());
        }
    }

    private void editar() {
        String nombre = valorTrim(txtNombre);
        String marca = valorTrim(txtMarca);
        if (nombre.isEmpty() || marca.isEmpty()) {
            alerta(Alert.AlertType.WARNING, "Validación", "Nombre y marca son obligatorios.");
            return;
        }
        BigDecimal precio = parsearPrecio();
        if (precio == null) {
            return;
        }
        Integer cantidad = parsearCantidad(true);
        if (cantidad == null) {
            return;
        }
        LocalDate caducidad = validarCaducidad();
        if (caducidad == null) {
            return;
        }
        Proveedor proveedor = cboProveedor.getValue();
        if (proveedor == null) {
            alerta(Alert.AlertType.WARNING, "Validación", "Selecciona un proveedor.");
            return;
        }

        // Confirmacion previa: solo en modo edicion, antes de tocar la base de
        // datos. Si el usuario elige "Cancelar" no se ejecuta nada, no se limpia
        // el formulario y no se deselecciona la fila.
        if (!confirmarModificacion(productoSeleccionado.getNombreproducto())) {
            return;
        }

        productoSeleccionado.setNombreproducto(nombre);
        productoSeleccionado.setMarca(marca);
        productoSeleccionado.setPrecio(precio);
        productoSeleccionado.setImagenUrl(imagenUrlSeleccionada);
        productoSeleccionado.setCantidad(cantidad);
        productoSeleccionado.setCaducidad(caducidad);
        productoSeleccionado.setIdproveedor(proveedor.getIdProveedor());

        try {
            // Edicion completa: fija nombre, marca, precio, imagen, cantidad,
            // caducidad y proveedor en una sola llamada. El DAO devuelve cuantas
            // filas quedaron con los datos esperados (verificacion real, no se
            // asume exito).
            int filasActualizadas = productoDAO.editarCompleto(productoSeleccionado);
            if (filasActualizadas == 0) {
                alerta(Alert.AlertType.ERROR, "Productos",
                        "No se aplicó ningún cambio. El producto pudo haber sido "
                        + "modificado o eliminado por otra persona. Vuelve a cargar "
                        + "el catálogo e inténtalo de nuevo.");
                return;
            }
            cargarTabla();
            limpiar();
            alerta(Alert.AlertType.INFORMATION, "Productos", "Producto actualizado correctamente.");
        } catch (SQLException e) {
            alerta(Alert.AlertType.ERROR, "Productos", "No se pudo editar el producto.\n" + e.getMessage());
        }
    }

    /**
     * Alert de confirmacion para la edicion. Botones personalizados "Sí" y
     * "Cancelar".
     *
     * @return true solo si el usuario presiono "Sí"; false si cancelo o cerro
     *         el dialogo.
     */
    private boolean confirmarModificacion(String nombre) {
        ButtonType btnSi = new ButtonType("Sí", ButtonBar.ButtonData.YES);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar modificación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Desea modificar el producto '" + nombre + "'?");
        confirmacion.getButtonTypes().setAll(btnSi, btnCancelar);

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        return resultado.isPresent() && resultado.get() == btnSi;
    }

    @FXML
    private void eliminar() {
        if (productoSeleccionado == null) {
            alerta(Alert.AlertType.WARNING, "Eliminar", "Selecciona un producto de la tabla para eliminarlo.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar producto");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Eliminar el producto \""
                + productoSeleccionado.getNombreproducto() + "\"? Esta acción no se puede deshacer.");
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                productoDAO.eliminar(productoSeleccionado.getIdproducto());
                cargarTabla();
                limpiar();
                alerta(Alert.AlertType.INFORMATION, "Productos", "Producto eliminado correctamente.");
            } catch (SQLException e) {
                if ("23503".equals(e.getSQLState())) {
                    alerta(Alert.AlertType.ERROR, "No se puede eliminar",
                            "Este producto tiene ventas relacionadas, por eso no se puede eliminar.\n"
                            + "Elimina o reasigna primero esas ventas.");
                } else {
                    alerta(Alert.AlertType.ERROR, "Productos",
                            "No se pudo eliminar el producto.\n" + e.getMessage());
                }
            }
        }
    }

    private void limpiar() {
        productoSeleccionado = null;
        imagenUrlSeleccionada = null;
        tblProductos.getSelectionModel().clearSelection();
        txtNombre.clear();
        txtMarca.clear();
        txtPrecio.clear();
        txtCantidad.clear();
        dtpCaducidad.setValue(null);
        cboProveedor.getSelectionModel().clearSelection();
        imgPreview.setImage(null);
    }

    // ================= Validaciones =================

    private String valorTrim(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    private BigDecimal parsearPrecio() {
        String texto = valorTrim(txtPrecio);
        if (texto.isEmpty()) {
            alerta(Alert.AlertType.WARNING, "Validación", "El precio es obligatorio.");
            return null;
        }
        try {
            BigDecimal precio = new BigDecimal(texto.replace(",", "."));
            if (precio.signum() < 0) {
                alerta(Alert.AlertType.WARNING, "Validación", "El precio no puede ser negativo.");
                return null;
            }
            return precio;
        } catch (NumberFormatException e) {
            alerta(Alert.AlertType.WARNING, "Validación", "El precio debe ser un número, ej. 28.50");
            return null;
        }
    }

    /**
     * @param obligatoria true en alta (la cantidad es requerida); false en edicion
     *                    (vacia = 0, no se agrega stock). Si hay texto, siempre debe
     *                    ser un entero >= 0.
     * @return la cantidad, o null si hubo error de validacion.
     */
    private Integer parsearCantidad(boolean obligatoria) {
        String texto = valorTrim(txtCantidad);
        if (texto.isEmpty()) {
            if (obligatoria) {
                alerta(Alert.AlertType.WARNING, "Validación", "La cantidad es obligatoria.");
                return null;
            }
            return 0;
        }
        try {
            int cantidad = Integer.parseInt(texto);
            if (cantidad < 0) {
                alerta(Alert.AlertType.WARNING, "Validación", "La cantidad no puede ser negativa.");
                return null;
            }
            return cantidad;
        } catch (NumberFormatException e) {
            alerta(Alert.AlertType.WARNING, "Validación", "La cantidad debe ser un número entero.");
            return null;
        }
    }

    private LocalDate validarCaducidad() {
        LocalDate caducidad = dtpCaducidad.getValue();
        if (caducidad == null) {
            alerta(Alert.AlertType.WARNING, "Validación", "Selecciona la fecha de caducidad.");
            return null;
        }
        if (caducidad.isBefore(LocalDate.now())) {
            alerta(Alert.AlertType.WARNING, "Validación", "La caducidad no puede ser una fecha pasada.");
            return null;
        }
        return caducidad;
    }

    private void alerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
