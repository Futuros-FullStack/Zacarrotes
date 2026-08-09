package com.mycompany.zacarrotes;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ProveedoresController implements Initializable {

    @FXML private TextField txtNombre;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;

    @FXML private TableView<Proveedor> tblProveedores;
    @FXML private TableColumn<Proveedor, Integer> colIdProveedor;
    @FXML private TableColumn<Proveedor, String> colNombreProveedor;
    @FXML private TableColumn<Proveedor, String> colDireccion;
    @FXML private TableColumn<Proveedor, String> colTelefono;

    @FXML private Label lblTotalProveedores;

    private static final String TELEFONO_REGEX = "\\d{10}";

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final ObservableList<Proveedor> listaProveedores = FXCollections.observableArrayList();
    private Proveedor proveedorSeleccionado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Las columnas resuelven su valor con el PropertyValueFactory declarado en
        // el FXML (id, nombre, direccion, telefono).
        tblProveedores.setItems(listaProveedores);
        tblProveedores.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                cargarEnFormulario(seleccionado);
            }
        });

        cargarTabla();
    }

    private void cargarTabla() {
        try {
            List<Proveedor> proveedores = proveedorDAO.listarTodos();
            listaProveedores.setAll(proveedores);
            lblTotalProveedores.setText(String.valueOf(listaProveedores.size()));
        } catch (SQLException e) {
            alerta(Alert.AlertType.ERROR, "Proveedores",
                    "No se pudo cargar la lista de proveedores.\n" + e.getMessage());
        }
    }

    private void cargarEnFormulario(Proveedor proveedor) {
        proveedorSeleccionado = proveedor;
        txtNombre.setText(proveedor.getNombre());
        txtDireccion.setText(proveedor.getDireccion());
        txtTelefono.setText(proveedor.getTelefono());
    }

    @FXML
    private void guardar() {
        String nombre = valorTrim(txtNombre);
        String direccion = valorTrim(txtDireccion);
        String telefono = valorTrim(txtTelefono);

        if (nombre.isEmpty() || direccion.isEmpty() || telefono.isEmpty()) {
            alerta(Alert.AlertType.WARNING, "Validación", "Nombre, dirección y teléfono son obligatorios.");
            return;
        }
        if (!telefono.matches(TELEFONO_REGEX)) {
            alerta(Alert.AlertType.WARNING, "Validación", "El teléfono debe tener 10 dígitos, sin guiones ni espacios.");
            return;
        }

        try {
            Integer idActual = proveedorSeleccionado == null ? null : proveedorSeleccionado.getId();
            if (proveedorDAO.existeNombreODireccion(nombre, direccion, idActual)) {
                alerta(Alert.AlertType.WARNING, "Validación", "Ya existe un proveedor con ese nombre o dirección.");
                return;
            }

            if (proveedorSeleccionado == null) {
                Proveedor nuevoProveedor = new Proveedor();
                nuevoProveedor.setNombre(nombre);
                nuevoProveedor.setDireccion(direccion);
                nuevoProveedor.setTelefono(telefono);
                proveedorDAO.insertar(nuevoProveedor);
            } else {
                proveedorSeleccionado.setNombre(nombre);
                proveedorSeleccionado.setDireccion(direccion);
                proveedorSeleccionado.setTelefono(telefono);
                proveedorDAO.actualizar(proveedorSeleccionado);
            }

            cargarTabla();
            limpiar();
        } catch (SQLException e) {
            alerta(Alert.AlertType.ERROR, "Proveedores", "No se pudo guardar el proveedor.\n" + e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        if (proveedorSeleccionado == null) {
            alerta(Alert.AlertType.WARNING, "Eliminar", "Selecciona un proveedor en la tabla para eliminarlo.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar proveedor");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText(
                "¿Eliminar al proveedor \"" + proveedorSeleccionado.getNombre() + "\"? Esta acción no se puede deshacer.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                proveedorDAO.eliminar(proveedorSeleccionado.getId());
                cargarTabla();
                limpiar();
            } catch (SQLException e) {
                // 23503 = foreign_key_violation: hay productos que apuntan a este proveedor.
                if ("23503".equals(e.getSQLState())) {
                    alerta(Alert.AlertType.ERROR, "No se puede eliminar",
                            "Este proveedor tiene productos relacionados, por eso no se puede eliminar.");
                } else {
                    alerta(Alert.AlertType.ERROR, "Proveedores", "No se pudo eliminar el proveedor.\n" + e.getMessage());
                }
            }
        }
    }

    private void limpiar() {
        proveedorSeleccionado = null;
        tblProveedores.getSelectionModel().clearSelection();
        txtNombre.clear();
        txtDireccion.clear();
        txtTelefono.clear();
    }

    private String valorTrim(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    private void alerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
