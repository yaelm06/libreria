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

    // --- ACCIÓN BOTÓN VENTAS ---
    @FXML
    void mostrarVistaVentas(ActionEvent event) {
        // Asegúrate de que el FXML VentasVista.fxml esté en esta ruta
        abrirVentana("/mx/uaemex/fi/bases/libreria/VentasVista.fxml", "Punto de Venta");
    }

    @FXML
    void mostrarVistaEmpleados(ActionEvent event) {
        abrirVentana("/mx/uaemex/fi/bases/libreria/EmpleadosVista.fxml", "Gestión de Empleados");
    }

    @FXML
    void mostrarVistaUbicaciones(ActionEvent event) {
        abrirVentana("/mx/uaemex/fi/bases/libreria/UbicacionesVista.fxml", "Gestión de Ubicaciones");
    }

    @FXML
    void mostrarVistaClientes(ActionEvent event) {
        abrirVentana("/mx/uaemex/fi/bases/libreria/ClientesVista.fxml", "Gestión de Clientes");
    }

    @FXML
    void mostrarVistaInventario(ActionEvent event) {
        abrirVentana("/mx/uaemex/fi/bases/libreria/InventarioVista.fxml", "Gestión de Inventario");
    }

    private void abrirVentana(String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            Connection connSql = this.con.obtenerConexion();

            // Inyección de dependencias según el controlador
            if (controller instanceof EmpleadosControlador) {
                ((EmpleadosControlador) controller).setConexionBD(this.con, connSql);
            } else if (controller instanceof UbicacionesControlador) {
                ((UbicacionesControlador) controller).setConexionBD(this.con, connSql);
            } else if (controller instanceof ClientesControlador) {
                ((ClientesControlador) controller).setConexionBD(this.con, connSql);
            } else if (controller instanceof InventarioControlador) {
                ((InventarioControlador) controller).setConexionBD(this.con, connSql);
            } else if (controller instanceof VentasControlador) {
                // CASO ESPECIAL: Ventas necesita el empleado logueado para registrar la venta
                ((VentasControlador) controller).initData(this.con, connSql, this.empleadoLogueado);
            }

            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
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