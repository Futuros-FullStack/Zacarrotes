/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package michelle.zacarrotes.controlador;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import michelle.zacarrotes.dao.ProveedorDao;
import michelle.zacarrotes.modelo.Proveedor;

/**
 *
 * @author angel
 */
public class ProveedorController implements Initializable{
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtTelefono;
    @FXML
    private VBox boxFilas;
    @FXML
    private Label lblTotalProveedores;
    
    private ProveedorDao proveedorDao = new ProveedorDao();
    private int idProveedorActual = 0;
    private HBox filaSeleccionadaActual = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarTablaCustom();
    }
    
    @FXML
    private void guardarProveedor(ActionEvent event)
    {
        if(txtNombre.getText().isEmpty() || txtDireccion.getText().isEmpty() || txtTelefono.getText().isEmpty())
        {
            mostrarAlerta("Error", "Por favor llena todos los campos.", Alert.AlertType.WARNING);
            return;
        }
        Proveedor p = new Proveedor();
        p.setNombreProveedor(txtNombre.getText());
        p.setDireccion(txtDireccion.getText());
        p.setTelefono(txtTelefono.getText());
        
        boolean exito;
        if(idProveedorActual == 0)
        {
            exito = proveedorDao.insertarProveedor(p);
            if (exito) mostrarAlerta("Éxito", "Proveedor registrado correctamente.", Alert.AlertType.INFORMATION);
        }
        else
        {
            p.setIdProveedor(idProveedorActual);
            exito = proveedorDao.editar(p);
            if (exito) mostrarAlerta("Éxito", "Proveedor actualizado correctamente.", Alert.AlertType.INFORMATION);
        }
        
        if(exito)
        {
            limpiarCampos();
            cargarTablaCustom();
        }
    }
    
    @FXML
    private void eliminarProveedor()
    {
        if (idProveedorActual == 0) {
            mostrarAlerta("Atención", "Selecciona un proveedor de la lista haciendo clic en él.", Alert.AlertType.WARNING);
            return;
        }
        
        boolean exito = proveedorDao.eliminar(idProveedorActual);
        
        if (exito) {
            mostrarAlerta("Éxito", "Proveedor eliminado correctamente.", Alert.AlertType.INFORMATION);
            limpiarCampos();
            cargarTablaCustom();
        } else {
            mostrarAlerta("Error de Integridad", "No se puede eliminar el proveedor porque ya está ligado a otros registros en el sistema.", Alert.AlertType.ERROR);
        }
    }
    
    private void cargarTablaCustom() {
        boxFilas.getChildren().clear(); 
        
        List<Proveedor> lista = proveedorDao.ListarTodos(); // Cuidado: En tu DAO pusiste ListarTodos() con mayúscula inicial
        
        lblTotalProveedores.setText(String.valueOf(lista.size()));
        
        for (Proveedor p : lista) {
            HBox fila = new HBox();
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setSpacing(12.0);
            fila.getStyleClass().add("row"); 

            Label lblId = new Label(String.valueOf(p.getIdProveedor()));
            lblId.setMinWidth(70.0); lblId.setPrefWidth(70.0);
            lblId.getStyleClass().add("cell-id");

            Label lblNombre = new Label(p.getNombreProveedor());
            lblNombre.setMinWidth(200.0); lblNombre.setPrefWidth(200.0);
            lblNombre.getStyleClass().add("cell-strong");

            Label lblDireccion = new Label(p.getDireccion());
            lblDireccion.setPrefWidth(280.0);
            lblDireccion.getStyleClass().add("cell");
            HBox.setHgrow(lblDireccion, Priority.ALWAYS); 

            Label lblTelefono = new Label(p.getTelefono());
            lblTelefono.setAlignment(Pos.CENTER_RIGHT);
            lblTelefono.setMinWidth(120.0); lblTelefono.setPrefWidth(120.0);
            lblTelefono.getStyleClass().add("cell-num");

            fila.getChildren().addAll(lblId, lblNombre, lblDireccion, lblTelefono);

            fila.setOnMouseClicked(e -> seleccionarFilaCustom(p, fila));
            
            boxFilas.getChildren().add(fila);
        }
    }
    
    private void seleccionarFilaCustom(Proveedor p, HBox fila) {
        idProveedorActual = p.getIdProveedor();
        txtNombre.setText(p.getNombreProveedor());
        txtDireccion.setText(p.getDireccion());
        txtTelefono.setText(p.getTelefono());

        if (filaSeleccionadaActual != null) {
            filaSeleccionadaActual.setStyle(""); 
        }
        
        filaSeleccionadaActual = fila;
        filaSeleccionadaActual.setStyle("-fx-background-color: #dcdde1; -fx-background-radius: 5px;");
    }
    
    private void limpiarCampos() {
        txtNombre.clear();
        txtDireccion.clear();
        txtTelefono.clear();
        idProveedorActual = 0;
        
        if (filaSeleccionadaActual != null) {
            filaSeleccionadaActual.setStyle("");
            filaSeleccionadaActual = null;
        }
    }
    
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
