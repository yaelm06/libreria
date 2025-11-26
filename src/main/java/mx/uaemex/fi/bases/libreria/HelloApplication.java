package mx.uaemex.fi.bases.libreria;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import mx.uaemex.fi.bases.libreria.controlador.LoginControlador;
import mx.uaemex.fi.bases.libreria.modelo.ConexionBD;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {

        // 1. Se crea la Conexión (equivalente a tu "servicio") UNA SOLA VEZ
        ConexionBD servicioConexion = new ConexionBD();

        // 2. Se prepara el cargador de FXML para la vista de Login
        // Esta es la nueva ruta para los RECURSOS
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginVista.fxml"));
        Parent root = loader.load();

        // 3. Se obtiene el controlador
        LoginControlador controller = loader.getController();

        // 4. Se "inyecta" la conexión en el controlador
        controller.setConexionBD(servicioConexion);

        // 5. Se muestra la ventana
        Scene scene = new Scene(root);
        primaryStage.setTitle("Sistema de Librería - Iniciar Sesión");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
