package mx.uaemex.fi.bases.libreria;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mx.uaemex.fi.bases.libreria.controlador.LoginControlador;
import mx.uaemex.fi.bases.libreria.modelo.ConexionBD;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class LibreriaApp extends Application {

    // Guardamos la referencia a la conexión para cerrarla al final
    private ConexionBD conexionBD;

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Instanciar la conexión a la BD
        // Asegúrate de que tu clase ConexionBD tenga los datos correctos
        this.conexionBD = new ConexionBD();

        // 2. Cargar la Vista de Login
        FXMLLoader fxmlLoader = new FXMLLoader(LibreriaApp.class.getResource("LoginVista.fxml"));
        Parent root = fxmlLoader.load();

        // 3. Obtener el controlador y pasarle la conexión
        LoginControlador loginCtrl = fxmlLoader.getController();
        loginCtrl.setConexionBD(this.conexionBD);

        // 4. Configurar y mostrar la ventana
        Scene scene = new Scene(root);
        stage.setTitle("Sistema de Gestión de Librería - Acceso");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    /**
     * Este método se ejecuta automáticamente cuando se cierra la aplicación.
     * Es el lugar IDEAL para cerrar la conexión a la base de datos.
     */
    @Override
    public void stop() throws Exception {
        System.out.println("Cerrando aplicación...");
        if (this.conexionBD != null) {
            Connection conn = this.conexionBD.obtenerConexion();
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("-> Conexión a Base de Datos cerrada correctamente.");
            }
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}