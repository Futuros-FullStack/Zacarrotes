package michelle.zacarrotes;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    public static final String VISTA_VENTA       = "/vistas/venta.fxml";
    public static final String VISTA_PRODUCTOS   = "/vistas/productos.fxml";
    public static final String VISTA_CLIENTES    = "/vistas/clientes.fxml";
    public static final String VISTA_PROVEEDORES = "/vistas/proveedores.fxml";

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = cargar(VISTA_PRODUCTOS);
        marcarSeccion(root, "tgbProductos");

        scene = new Scene(root, 1150, 720);
        stage.setTitle("Zacarrotes");
        stage.setScene(scene);
        stage.show();
    }

    public static void cambiarVista(String recursoFxml, String idBoton) {
        try {
            Parent root = cargar(recursoFxml);
            marcarSeccion(root, idBoton);
            scene.setRoot(root);
        } catch (IOException e) {
            System.err.println("No se pudo abrir " + recursoFxml + ": " + e.getMessage());
        }
    }

    private static Parent cargar(String recursoFxml) throws IOException {
        java.net.URL url = App.class.getResource(recursoFxml);
        if (url == null) {
            throw new IOException("no esta en el classpath");
        }
        return FXMLLoader.load(url);
    }

    private static void marcarSeccion(Parent root, String idBoton) {
        Node boton = root.lookup("#" + idBoton);
        if (boton instanceof ToggleButton) {
            ((ToggleButton) boton).setSelected(true);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
