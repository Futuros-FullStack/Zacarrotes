package michelle.zacarrotes.controlador;

import javafx.fxml.FXML;
import michelle.zacarrotes.App;

public class NavbarController {

    @FXML
    private void irAVenta() {
        App.cambiarVista(App.VISTA_VENTA, "tgbVenta");
    }

    @FXML
    private void irAProductos() {
        App.cambiarVista(App.VISTA_PRODUCTOS, "tgbProductos");
    }

    @FXML
    private void irAClientes() {
        App.cambiarVista(App.VISTA_CLIENTES, "tgbClientes");
    }

    @FXML
    private void irAProveedores() {
        App.cambiarVista(App.VISTA_PROVEEDORES, "tgbProveedores");
    }
}
