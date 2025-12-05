package mx.uaemex.fi.bases.libreria.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import mx.uaemex.fi.bases.libreria.modelo.ConexionBD;
import mx.uaemex.fi.bases.libreria.modelo.data.Empleado;

import java.io.IOException;
import java.sql.Connection;

public class MenuPrincipalControlador {

    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;

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
        lblBienvenida.setText("Bienvenido, " + empleado.getNombre());
        lblRol.setText("Rol: " + empleado.getCargo());
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

    // --- MÉTODO CORREGIDO: Abre ventana independiente ---
    @FXML
    void mostrarVistaEmpleados(ActionEvent event) {
        // Ruta absoluta correcta
        abrirVentana("/mx/uaemex/fi/bases/libreria/EmpleadosVista.fxml", "Gestión de Empleados");
    }

    // --- MÉTODO CORREGIDO: Abre ventana independiente ---
    @FXML
    void mostrarVistaUbicaciones(ActionEvent event) {
        // Ruta absoluta correcta
        abrirVentana("/mx/uaemex/fi/bases/libreria/UbicacionesVista.fxml", "Gestión de Ubicaciones");
    }

    // Método genérico para abrir ventanas y manejar la conexión
    private void abrirVentana(String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Inyección de dependencias manual (polimorfismo simple)
            Object controller = loader.getController();
            Connection connSql = this.con.obtenerConexion();

            if (controller instanceof EmpleadosControlador) {
                ((EmpleadosControlador) controller).setConexionBD(this.con, connSql);
            } else if (controller instanceof UbicacionesControlador) {
                ((UbicacionesControlador) controller).setConexionBD(this.con, connSql);
            }

            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(new Scene(root));
            stage.setMaximized(true); // Pantalla completa para aprovechar espacio
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar ventana: " + fxmlPath + " -> " + e.getMessage());
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