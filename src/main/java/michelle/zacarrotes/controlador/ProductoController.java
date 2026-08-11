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
import java.util.function.UnaryOperator;

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
import javafx.scene.control.TextFormatter;
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
// esta clase controla toda la pantalla de productos (los campos, la tabla, botones, etc)
public class ProductoController implements Initializable {

    // todo esto son los campos de la pantalla que agarro del fxml
    @FXML private BorderPane pnlRaiz;
    @FXML private ImageView imgPreview;
    @FXML private TextField txtNombre;
    @FXML private TextField txtMarca;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtCantidad;
    @FXML private DatePicker dtpCaducidad;
    @FXML private ComboBox<Proveedor> cboProveedor;

    // esta es la tabla y sus columnas
    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, Image> colIcono;
    @FXML private TableColumn<Producto, String> colProducto;
    @FXML private TableColumn<Producto, String> colMarca;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colCantidad;
    @FXML private TableColumn<Producto, LocalDate> colCaducidad;
    @FXML private TableColumn<Producto, String> colProveedor;

    @FXML private Label lblTotalProductos; // el numerito de cuantos productos hay

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // pa mostrar la fecha bonita
    private static final String REGEX_LETRAS = "[\\p{L} ]*";           // solo letras y espacios
    private static final String REGEX_PRECIO = "\\d*\\.?\\d*";          // digitos y como maximo un punto
    private static final String REGEX_ENTERO = "\\d*";                  // solo digitos

    // estos son mis ayudantes pa hablar con la base de datos
    private final ProductoDao productoDAO = new ProductoDao();
    private final ProveedorDao proveedorDAO = new ProveedorDao();
    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList(); // la lista que ve la tabla

    private Producto productoSeleccionado; // el producto que agarre de la tabla (null si estoy creando uno nuevo)
    private String imagenUrlSeleccionada; // la ruta de la imagen que elegi
    private ContextMenu menuImagen; // el menucito de "borrar imagen"

    // esto corre solito cuando abre la pantalla, aqui prendo todo
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarComboProveedores();
        configurarColumnas();
        configurarMenuImagen();
        configurarDeseleccion();
        configurarValidacionCampos();

        tblProductos.setItems(listaProductos); // le digo a la tabla que muestre mi lista
        // cuando le pican a un renglon lo cargo en el formulario
        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                cargarEnFormulario(seleccionado);
            }
        });

        cargarProveedores(); // lleno el combo de proveedores
        cargarTabla(); // lleno la tabla con lo que hay en la bd
    }
    // aqui le digo al combo como mostrar el nombre del proveedor (si no, saldria cosa rara)
    private void configurarComboProveedores() {
        cboProveedor.setConverter(new StringConverter<Proveedor>() {
            @Override
            public String toString(Proveedor proveedor) {
                return proveedor == null ? "" : proveedor.getNombreProveedor();
            }

            @Override
            public Proveedor fromString(String string) {
                return null;
            }
        });
        // esto es pa que en la lista desplegable tambien salga el puro nombre
        cboProveedor.setCellFactory(cb -> new ListCell<Proveedor>() {
            @Override
            protected void updateItem(Proveedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreProveedor());
            }
        });
    }

    // aqui le doy formato a las columnas raras (imagen, precio y caducidad)
    private void configurarColumnas() {
        // la columna del iconito: le pongo la imagen chiquita del producto
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

        // el precio lo muestro con signo de peso y 2 decimales
        colPrecio.setCellFactory(col -> new TableCell<Producto, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal precio, boolean empty) {
                super.updateItem(precio, empty);
                setText(empty || precio == null ? null : String.format("$ %.2f", precio));
            }
        });
        // la caducidad la pinto de color segun que tan cerca este de caducar (rojo/amarillo/verde)
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
                long dias = ChronoUnit.DAYS.between(LocalDate.now(), fecha); // cuantos dias faltan
                // 7 dias o menos = rojo, 30 o menos = amarillo, mas = verde
                String estado = dias <= 7 ? "pill-danger" : dias <= 30 ? "pill-warn" : "pill-ok";
                getStyleClass().addAll("pill", estado);
            }
        });
    }
    // el menucito que sale al darle click a la imagen pa poder borrarla
    private void configurarMenuImagen() {
        MenuItem itemBorrar = new MenuItem("Borrar imagen");
        itemBorrar.setOnAction(e -> borrarImagen());

        menuImagen = new ContextMenu(itemBorrar);
        menuImagen.getStyleClass().add("menu-imagen");

        // si le pican con el click izquierdo y si hay imagen, le abro el menu
        imgPreview.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && imgPreview.getImage() != null) {
                menuImagen.show(imgPreview, e.getScreenX(), e.getScreenY());
            }
        });
    }

    // borra la imagen del preview pero antes pregunta si de verdad quiere
    private void borrarImagen() {
        if (imgPreview.getImage() == null) {
            return; // si no hay imagen ni le muevo
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Quitar imagen");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Seguro que quieres quitar esta imagen?");
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            imgPreview.setImage(null);
            imagenUrlSeleccionada = null; // tambien borro la ruta guardada
        }
    }
    // si le pican a un lado (fuera de los campos) limpio el formulario
    private void configurarDeseleccion() {
        pnlRaiz.setOnMouseClicked(e -> {
            if (conservaSeleccion(e.getTarget())) {
                return; // si le pico a un campo o boton no hago nada
            }
            if (productoSeleccionado != null) {
                limpiar();
            }
        });
    }

    // reviso si donde pico es un campo/boton/tabla, pa NO limpiar en esos casos
    private boolean conservaSeleccion(Object destino) {
        Node nodo = (destino instanceof Node) ? (Node) destino : null;
        // voy subiendo de padre en padre pa ver que fue lo que toco
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
    // aqui le pongo el filtro a cada campo pa que no dejen escribir cosas invalidas
    private void configurarValidacionCampos() {
        txtNombre.setTextFormatter(new TextFormatter<>(filtroPorRegex(REGEX_LETRAS)));
        txtMarca.setTextFormatter(new TextFormatter<>(filtroPorRegex(REGEX_LETRAS)));
        txtPrecio.setTextFormatter(new TextFormatter<>(filtroPorRegex(REGEX_PRECIO)));
        txtCantidad.setTextFormatter(new TextFormatter<>(filtroPorRegex(REGEX_ENTERO)));
    }

    // este es el filtro: deja pasar la tecla solo si el texto queda valido segun el regex
    private UnaryOperator<TextFormatter.Change> filtroPorRegex(String regex) {
        return cambio -> {
            if (productoSeleccionado != null) {
                return cambio; // si estoy editando no filtro, dejo escribir libre
            }
            return cambio.getControlNewText().matches(regex) ? cambio : null;
        };
    }

    // trae los proveedores de la bd y los mete al combo
    private void cargarProveedores() {
        try {
            List<Proveedor> proveedores = proveedorDAO.ListarTodos();
            cboProveedor.setItems(FXCollections.observableArrayList(proveedores));
        } catch (RuntimeException e) {
            alerta(Alert.AlertType.ERROR, "Conexión", e.getMessage()); // si truena la conexion aviso
        }
    }

    // trae todos los productos de la bd y llena la tabla
    private void cargarTabla() {
        try {
            List<Producto> productos = productoDAO.listarTodos();
            listaProductos.setAll(productos); // reemplazo lo que habia por lo nuevo
            lblTotalProductos.setText(String.valueOf(listaProductos.size())); // actualizo el contador
        } catch (SQLException e) {
            alerta(Alert.AlertType.ERROR, "Catálogo",
                    "No se pudo cargar el catálogo de productos.\n" + e.getMessage());
        } catch (RuntimeException e) {
            alerta(Alert.AlertType.ERROR, "Conexión", e.getMessage());
        }
    }

    // agarra el producto de la tabla y lo pone en los campos pa poder editarlo
    private void cargarEnFormulario(Producto producto) {
        productoSeleccionado = producto;
        txtNombre.setText(producto.getNombreproducto());
        txtMarca.setText(producto.getMarca());
        txtPrecio.setText(producto.getPrecio() == null ? "" : producto.getPrecio().toPlainString());
        txtCantidad.setText(String.valueOf(producto.getCantidad()));
        dtpCaducidad.setValue(producto.getCaducidad());
        seleccionarProveedorPorNombre(producto.getNombreproveedor());
        imagenUrlSeleccionada = producto.getImagenUrl();
        mostrarVistaPrevia(imagenUrlSeleccionada);
    }

    // busca en el combo el proveedor que se llame igual y lo selecciona
    private void seleccionarProveedorPorNombre(String nombre) {
        if (nombre != null) {
            for (Proveedor proveedor : cboProveedor.getItems()) {
                if (nombre.equals(proveedor.getNombreProveedor())) {
                    cboProveedor.getSelectionModel().select(proveedor);
                    return; // ya lo encontre, me salgo
                }
            }
        }
        cboProveedor.getSelectionModel().clearSelection(); // si no lo hallo lo dejo vacio
    }

    // muestra la imagen chiquita del producto en el preview
    private void mostrarVistaPrevia(String url) {
        if (url == null || url.trim().isEmpty()) {
            imgPreview.setImage(null); // sin ruta, sin imagen
            return;
        }
        try {
            imgPreview.setImage(new Image(url.trim(), 70, 70, true, true, true));
        } catch (Exception e) {
            imgPreview.setImage(null); // si la ruta esta mala no truena, solo la deja vacia
        }
    }

    // abre el explorador pa escoger una imagen png o jpg
    @FXML
    private void elegirImagen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Elegir imagen del producto");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes PNG o JPG", "*.png", "*.jpg", "*.jpeg"));
        File archivo = chooser.showOpenDialog(imgPreview.getScene().getWindow());
        if (archivo != null) {
            imagenUrlSeleccionada = archivo.toURI().toString(); // guardo la ruta
            mostrarVistaPrevia(imagenUrlSeleccionada);
        }
    }

    // el boton guardar: si no hay nada seleccionado es nuevo, si no es editar
    @FXML
    private void guardar() {
        if (productoSeleccionado == null) {
            insertar();
        } else {
            editar();
        }
    }

    // aqui creo un producto nuevo (valido todo antes de mandarlo a la bd)
    private void insertar() {
        String nombre = valorTrim(txtNombre); // agarro el texto y le quito espacios
        String marca = valorTrim(txtMarca);


        if (!confirmarGuardado(nombre)) {
            return; // si le pico cancelar no guardo
        }

        // valido nombre: no vacio y solo letras
        if (nombre.isEmpty() || !nombre.matches(REGEX_LETRAS)) {
            alerta(Alert.AlertType.ERROR, "Validación", "El nombre solo acepta letras.");
            return;
        }
        // lo mismo con la marca
        if (marca.isEmpty() || !marca.matches(REGEX_LETRAS)) {
            alerta(Alert.AlertType.ERROR, "Validación", "La marca solo acepta letras.");
            return;
        }
        BigDecimal precio = validarPrecioInsert(); // valido y convierto el precio
        if (precio == null) {
            return; // si vino mal me salgo (ya avise adentro)
        }
        Integer cantidad = validarCantidadInsert(); // valido la cantidad
        if (cantidad == null) {
            return;
        }
        LocalDate caducidad = validarCaducidad(); // valido la fecha
        if (caducidad == null) {
            return;
        }
        Proveedor proveedor = cboProveedor.getValue(); // agarro el proveedor elegido
        if (proveedor == null) {
            alerta(Alert.AlertType.WARNING, "Validación", "Selecciona un proveedor.");
            return;
        }

        // ya que todo esta bien, armo el producto nuevo
        Producto nuevo = new Producto();
        nuevo.setNombreproducto(nombre);
        nuevo.setMarca(marca);
        nuevo.setPrecio(precio);
        nuevo.setCantidad(cantidad);
        nuevo.setCaducidad(caducidad);
        nuevo.setImagenUrl(imagenUrlSeleccionada);
        nuevo.setIdproveedor(proveedor.getIdProveedor());

        try {
            productoDAO.insertar(nuevo); // lo mando a la bd
            cargarTabla(); // refresco la tabla pa que aparezca
            limpiar(); // limpio los campos
            alerta(Alert.AlertType.INFORMATION, "Productos", "Producto agregado correctamente.");
        } catch (SQLException e) {
            alerta(Alert.AlertType.ERROR, "Productos", "No se pudo agregar el producto.\n" + e.getMessage());
        }
    }

    // aqui edito el producto que ya existe
    private void editar() {
        String nombre = valorTrim(txtNombre);
        String marca = valorTrim(txtMarca);
        if (nombre.isEmpty() || marca.isEmpty()) {
            alerta(Alert.AlertType.WARNING, "Validación", "Nombre y marca son obligatorios.");
            return;
        }
        BigDecimal precio = parsearPrecio(); // valido precio
        if (precio == null) {
            return;
        }
        Integer cantidad = parsearCantidad(true); // valido cantidad (obligatoria)
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

        if (!confirmarModificacion(productoSeleccionado.getNombreproducto())) {
            return; // si cancela no edito
        }

        // le cambio los datos al producto seleccionado
        productoSeleccionado.setNombreproducto(nombre);
        productoSeleccionado.setMarca(marca);
        productoSeleccionado.setPrecio(precio);
        productoSeleccionado.setImagenUrl(imagenUrlSeleccionada);
        productoSeleccionado.setCantidad(cantidad);
        productoSeleccionado.setCaducidad(caducidad);
        productoSeleccionado.setIdproveedor(proveedor.getIdProveedor());

        try {

            int filasActualizadas = productoDAO.editarCompleto(productoSeleccionado); // lo mando a la bd
            // si no cambio ninguna fila es que alguien ya lo movio o borro
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

    // ventanita de "seguro que quieres modificar?", regresa true si le pico que si
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

    // igualita pero pa cuando guardas uno nuevo
    private boolean confirmarGuardado(String nombre) {
        ButtonType btnSi = new ButtonType("Sí", ButtonBar.ButtonData.YES);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar guardado");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Desea guardar el producto '" + nombre + "'?");
        confirmacion.getButtonTypes().setAll(btnSi, btnCancelar);

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        return resultado.isPresent() && resultado.get() == btnSi;
    }

    // el boton eliminar
    @FXML
    private void eliminar() {
        if (productoSeleccionado == null) {
            alerta(Alert.AlertType.WARNING, "Eliminar", "Selecciona un producto de la tabla para eliminarlo.");
            return; // si no agarraste nada, no hay que borrar
        }

        // pregunto antes de borrar porque ya no hay vuelta atras
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar producto");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Eliminar el producto \""
                + productoSeleccionado.getNombreproducto() + "\"? Esta acción no se puede deshacer.");
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                productoDAO.eliminar(productoSeleccionado.getIdproducto()); // lo borro de la bd
                cargarTabla();
                limpiar();
                alerta(Alert.AlertType.INFORMATION, "Productos", "Producto eliminado correctamente.");
            } catch (SQLException e) {
                // el 23503 es cuando el producto tiene ventas amarradas, por eso no deja borrar
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

    // deja todos los campos vacios y sin nada seleccionado
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


    // atajo pa sacar el texto de un campo sin espacios (y si esta null regresa vacio)
    private String valorTrim(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    // valida el precio al editar: no vacio, numero valido y no negativo
    private BigDecimal parsearPrecio() {
        String texto = valorTrim(txtPrecio);
        if (texto.isEmpty()) {
            alerta(Alert.AlertType.WARNING, "Validación", "El precio es obligatorio.");
            return null;
        }
        try {
            BigDecimal precio = new BigDecimal(texto.replace(",", ".")); // si puso coma la cambio por punto
            if (precio.signum() < 0) {
                alerta(Alert.AlertType.WARNING, "Validación", "El precio no puede ser negativo.");
                return null;
            }
            return precio;
        } catch (NumberFormatException e) {
            alerta(Alert.AlertType.WARNING, "Validación", "El precio debe ser un número, ej. 28.50");
            return null; // si no es numero regreso null
        }
    }

    // valida la cantidad al editar (obligatoria = si o si tiene que traer algo)
    private Integer parsearCantidad(boolean obligatoria) {
        String texto = valorTrim(txtCantidad);
        if (texto.isEmpty()) {
            if (obligatoria) {
                alerta(Alert.AlertType.WARNING, "Validación", "La cantidad es obligatoria.");
                return null;
            }
            return 0; // si no es obligatoria y esta vacia, le pongo 0
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


    // valida el precio al crear uno nuevo (aqui debe traer numero si o si)
    private BigDecimal validarPrecioInsert() {
        String texto = valorTrim(txtPrecio);
        try {
            if (texto.isEmpty()) {
                throw new NumberFormatException("vacío"); // vacio lo trato como error
            }
            return new BigDecimal(texto);
        } catch (NumberFormatException e) {
            alerta(Alert.AlertType.ERROR, "Validación", "El precio solo acepta números y un punto decimal.");
            return null;
        }
    }
    // lo mismo pero pa la cantidad al crear uno nuevo
    private Integer validarCantidadInsert() {
        String texto = valorTrim(txtCantidad);
        try {
            if (texto.isEmpty()) {
                throw new NumberFormatException("vacío");
            }
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            alerta(Alert.AlertType.ERROR, "Validación", "La cantidad solo acepta números enteros.");
            return null;
        }
    }

    // valida la fecha de caducidad: que este puesta y que no sea del pasado
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

    // metodito pa no repetir el codigo de las ventanas de aviso, solo le paso el tipo, titulo y mensaje
    private void alerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
