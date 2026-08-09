/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package michelle.zacarrotes.controlador;

import michelle.zacarrotes.modelo.Cliente;
import michelle.zacarrotes.dao.ClienteDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author CORDOVA
 */
public class ClienteController {
    
    public TextField txtNombre;
    public TableView<Cliente> tblClientes;
    public TableColumn<Cliente, String> colIdCliente;
    public TableColumn<Cliente, String> colNombre;
    public Label lblTotalClientes;
    public Button btnGuardar;
    public Button btnEliminar;
    
    public ClienteDao dao = new ClienteDao();
    public ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();
    
    public void initialize(){
        colIdCliente.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        
        cargarTabla();
    }
    
    private void cargarTabla(){
        listaClientes.clear();
        listaClientes.addAll(dao.listarClientes());
        tblClientes.setItems(listaClientes);
        
        if (lblTotalClientes != null) {
            lblTotalClientes.setText(String.valueOf(listaClientes.size()));
        }
    }
    
    public void guardarCliente() {
        String nombre = txtNombre.getText();
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            Cliente nuevo = new Cliente();
            nuevo.setNombre(nombre);
            
            dao.insertar(nuevo);
            
            txtNombre.clear();
            cargarTabla();
        } else {
            mostrarAlerta("Atención", "El campo de nombre no puede estar vacío.");
        }
    }
    
    public void eliminarCliente() {
        Cliente seleccionado = tblClientes.getSelectionModel().getSelectedItem();
        
        if (seleccionado != null) {
            dao.eliminar(seleccionado.getIdCliente());
            cargarTabla();
        } else {
            mostrarAlerta("Atención", "Primero selecciona un cliente de la tabla.");
        }
    }
    
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
