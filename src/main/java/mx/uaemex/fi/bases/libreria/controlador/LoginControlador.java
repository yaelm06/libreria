package mx.uaemex.fi.bases.libreria.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

// Rutas de 'import' actualizadas a 'modelo'
import mx.uaemex.fi.bases.libreria.modelo.ConexionBD;
import mx.uaemex.fi.bases.libreria.modelo.EmpleadosDAO;
import mx.uaemex.fi.bases.libreria.modelo.EmpleadosDAOPsqlImp;
import mx.uaemex.fi.bases.libreria.modelo.data.Empleado;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;

public class LoginControlador {

    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtContraseña;
    @FXML
    private Label lblMensaje;

    private ConexionBD con;
    private EmpleadosDAOPsqlImp empleadosDAO;

    public void setConexionBD(ConexionBD conexion) {
        this.con = conexion;
    }

    public LoginControlador() {
        // La implementación del DAO ahora está en 'modelo'
        this.empleadosDAO = new EmpleadosDAOPsqlImp();
    }

    @FXML
    void login(ActionEvent event) {
        String usuario = txtUsuario.getText();
        String contraseña = txtContraseña.getText();
        Connection conn = null;

        try {
            conn = con.obtenerConexion();
            if (conn == null) {
                lblMensaje.setText("Error: No se pudo conectar a la BD.");
                lblMensaje.setTextFill(Color.RED);
                return;
            }

            empleadosDAO.setConexion(conn);

            Empleado empleadoConsulta = new Empleado();
            empleadoConsulta.setUsuario(usuario);
            empleadoConsulta.setContrasenia(contraseña);

            ArrayList<Empleado> resultado = empleadosDAO.consultar(empleadoConsulta);

            if (resultado.size() == 1) {
                lblMensaje.setText("¡Éxito! Bienvenido " + resultado.get(0).getNombre());
                lblMensaje.setTextFill(Color.GREEN);
            } else {
                lblMensaje.setText("Usuario o contraseña incorrectos.");
                lblMensaje.setTextFill(Color.RED);
            }

        } catch (Exception e) {
            lblMensaje.setText("Error en la aplicación: " + e.getMessage());
            lblMensaje.setTextFill(Color.RED);
            e.printStackTrace();
        } finally {
            if(conn != null) {
                con.cerrarRecursos(conn);
            }
        }
    }

    @FXML
    void irARegistro(ActionEvent event) {
        try {
            // Ruta de FXML actualizada a '/.../vista/...'

            String fxmlPath = "/mx/uaemex/fi/bases/libreria/RegistroVista.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            RegistroControlador controller = loader.getController();
            controller.setConexionBD(this.con);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("Registrar Nuevo Empleado");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            lblMensaje.setText("Error al cargar la vista de registro.");
        }
    }
}
