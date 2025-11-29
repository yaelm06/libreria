package mx.uaemex.fi.bases.libreria.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import mx.uaemex.fi.bases.libreria.modelo.ConexionBD;
import mx.uaemex.fi.bases.libreria.modelo.data.Empleado;

import java.io.IOException;
import java.sql.Connection;

public class MenuPrincipalControlador {

    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;
    @FXML private StackPane contentArea; // Se mantiene por si usas el dashboard para otras cosas

    @FXML private Button btnVentas;
    @FXML private Button btnLibros;
    @FXML private Button btnClientes;
    @FXML private Button btnEmpleados;
    @FXML private Button btnUbicaciones;

    private ConexionBD con;
    private Empleado empleadoLogueado;

    public void initData(ConexionBD conexion, Empleado empleado) {
        this.con = conexion;
        this.empleadoLogueado = empleado;
        lblBienvenida.setText(empleado.getNombre());
        lblRol.setText(empleado.getCargo());
        configurarPermisos();
    }

    private void configurarPermisos() {
        String cargo = empleadoLogueado.getCargo();
        boolean esAdmin = cargo != null && (cargo.equalsIgnoreCase("Gerente") || cargo.equalsIgnoreCase("Administrador"));
        if (!esAdmin) {
            btnEmpleados.setDisable(true);
            btnUbicaciones.setDisable(true);
        }
    }

    // --- CAMBIO: ABRIR EMPLEADOS EN NUEVA VENTANA ---
    @FXML
    void mostrarVistaEmpleados(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mx/uaemex/fi/bases/libreria/EmpleadosVista.fxml"));
            Parent root = loader.load();

            EmpleadosControlador ctrl = loader.getController();
            Connection connSql = this.con.obtenerConexion();
            ctrl.setConexionBD(this.con, connSql);

            // Creamos un nuevo Stage (Ventana)
            Stage stage = new Stage();
            stage.setTitle("Gestión de Empleados");
            stage.setScene(new Scene(root));
            stage.setMaximized(true); // ¡Que use toda la pantalla!
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar ventana empleados: " + e.getMessage());
        }
    }

    // --- CAMBIO: ABRIR UBICACIONES EN NUEVA VENTANA ---
    @FXML
    void mostrarVistaUbicaciones(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mx/uaemex/fi/bases/libreria/UbicacionesVista.fxml"));
            Parent root = loader.load();

            UbicacionesControlador ctrl = loader.getController();
            Connection connSql = this.con.obtenerConexion();
            ctrl.setConexionBD(this.con, connSql);

            Stage stage = new Stage();
            stage.setTitle("Gestión de Ubicaciones");
            stage.setScene(new Scene(root, 900, 600)); // Tamaño fijo cómodo
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void cerrarSesion(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mx/uaemex/fi/bases/libreria/LoginVista.fxml"));
            Parent root = loader.load();
            LoginControlador ctrl = loader.getController();
            ctrl.setConexionBD(this.con);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("Sistema de Librería - Login");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}