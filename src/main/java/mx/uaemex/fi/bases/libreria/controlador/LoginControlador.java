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

import mx.uaemex.fi.bases.libreria.modelo.ConexionBD;
import mx.uaemex.fi.bases.libreria.modelo.EmpleadosDAOPsqlImp;
import mx.uaemex.fi.bases.libreria.modelo.data.Empleado;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;

public class LoginControlador {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContraseña;
    @FXML private Label lblMensaje;

    private ConexionBD con;
    private EmpleadosDAOPsqlImp empleadosDAO;

    public void setConexionBD(ConexionBD conexion) {
        this.con = conexion;
    }

    public LoginControlador() {
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
                lblMensaje.setText("Error de conexión BD.");
                lblMensaje.setTextFill(Color.RED);
                return;
            }

            empleadosDAO.setConexion(conn);

            Empleado empleadoConsulta = new Empleado();
            empleadoConsulta.setUsuario(usuario);
            empleadoConsulta.setContrasenia(contraseña);

            ArrayList<Empleado> resultado = empleadosDAO.consultar(empleadoConsulta);

            if (!resultado.isEmpty()) {
                Empleado empLogueado = resultado.get(0);
                if (Boolean.TRUE.equals(empLogueado.isActivo())) {
                    lblMensaje.setText("¡Bienvenido " + empLogueado.getNombre() + "!");
                    lblMensaje.setTextFill(Color.GREEN);
                    abrirMenuPrincipal(event, empLogueado);
                } else {
                    lblMensaje.setText("Usuario inactivo.");
                    lblMensaje.setTextFill(Color.RED);
                }
            } else {
                lblMensaje.setText("Usuario o contraseña incorrectos.");
                lblMensaje.setTextFill(Color.RED);
            }

        } catch (Exception e) {
            lblMensaje.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void abrirMenuPrincipal(ActionEvent event, Empleado empleado) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mx/uaemex/fi/bases/libreria/MenuPrincipalVista.fxml"));
            Parent root = loader.load();

            MenuPrincipalControlador menuCtrl = loader.getController();
            menuCtrl.initData(this.con, empleado);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setTitle("Sistema Librería - Menú Principal");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            lblMensaje.setText("Error al cargar menú principal: " + e.getMessage());
        }
    }
}