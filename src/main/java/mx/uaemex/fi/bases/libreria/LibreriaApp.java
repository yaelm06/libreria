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

    private ConexionBD conexionBD;

    @Override
    public void start(Stage stage) throws IOException {

        this.conexionBD = new ConexionBD();

        FXMLLoader fxmlLoader = new FXMLLoader(LibreriaApp.class.getResource("LoginVista.fxml"));
        Parent root = fxmlLoader.load();

        LoginControlador loginCtrl = fxmlLoader.getController();
        loginCtrl.setConexionBD(this.conexionBD);

        Scene scene = new Scene(root);
        stage.setTitle("Sistema de Gestión de Librería - Acceso");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

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