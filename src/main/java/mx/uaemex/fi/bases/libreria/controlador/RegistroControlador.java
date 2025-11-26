package mx.uaemex.fi.bases.libreria.controlador;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import mx.uaemex.fi.bases.libreria.modelo.*;
import mx.uaemex.fi.bases.libreria.modelo.data.ElementoConID;
import mx.uaemex.fi.bases.libreria.modelo.data.Empleado;
import mx.uaemex.fi.bases.libreria.modelo.data.Estado;
import mx.uaemex.fi.bases.libreria.modelo.data.Localidad;
import mx.uaemex.fi.bases.libreria.modelo.data.Municipio;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Optional;

public class RegistroControlador {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidoPaterno;
    @FXML private TextField txtApellidoMaterno;
    @FXML private TextField txtCargo;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCalle;
    @FXML private TextField txtNumeroCalle;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContraseña;
    @FXML private Label lblMensaje;

    @FXML private ComboBox<Estado> cmbEstado;
    @FXML private ComboBox<Municipio> cmbMunicipio;
    @FXML private ComboBox<Localidad> cmbLocalidad;

    private ConexionBD con;
    private EmpleadosDAOPsqlImp empleadoDAO;
    private EstadoDAOPsqlImp estadoDAO;
    private MunicipioDAOPsqlImp municipioDAO;
    private LocalidadDAOPsqlImp localidadDAO;

    public RegistroControlador() {
        this.empleadoDAO = new EmpleadosDAOPsqlImp();
        this.estadoDAO = new EstadoDAOPsqlImp();
        this.municipioDAO = new MunicipioDAOPsqlImp();
        this.localidadDAO = new LocalidadDAOPsqlImp();
    }

    public void setConexionBD(ConexionBD conexion) {
        this.con = conexion;
        cargarEstados();
    }

    // --- MÉTODOS DE CARGA DE DATOS (CASCADA) ---

    private void cargarEstados() {
        Connection conn = null;
        try {
            conn = con.obtenerConexion();
            estadoDAO.setConexion(conn);
            ArrayList<Estado> estados = estadoDAO.consultar();
            cmbEstado.setItems(FXCollections.observableArrayList(estados));

            // Listener para cambio de Estado
            cmbEstado.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    cargarMunicipios(newVal.getId());
                } else {
                    cmbMunicipio.getItems().clear();
                    cmbLocalidad.getItems().clear();
                }
            });
        } catch (Exception e) {
            lblMensaje.setText("Error al cargar estados: " + e.getMessage());
        } finally {
            con.cerrarRecursos(conn);
        }
    }

    private void cargarMunicipios(int idEstado) {
        Connection conn = null;
        try {
            conn = con.obtenerConexion();
            municipioDAO.setConexion(conn);

            Municipio filtro = new Municipio();
            Estado e = new Estado();
            e.setId(idEstado);
            filtro.setEstado(e);

            ArrayList<Municipio> municipios = municipioDAO.consultar(filtro);
            cmbMunicipio.setItems(FXCollections.observableArrayList(municipios));
            cmbLocalidad.getItems().clear();

            // Listener para cambio de Municipio
            cmbMunicipio.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    cargarLocalidades(newVal.getId());
                } else {
                    cmbLocalidad.getItems().clear();
                }
            });
        } catch (Exception e) {
            lblMensaje.setText("Error al cargar municipios: " + e.getMessage());
        } finally {
            con.cerrarRecursos(conn);
        }
    }

    private void cargarLocalidades(int idMunicipio) {
        Connection conn = null;
        try {
            conn = con.obtenerConexion();
            localidadDAO.setConexion(conn);

            Localidad filtro = new Localidad();
            Municipio m = new Municipio();
            m.setId(idMunicipio);
            filtro.setMunicipio(m);

            ArrayList<Localidad> localidades = localidadDAO.consultar(filtro);
            cmbLocalidad.setItems(FXCollections.observableArrayList(localidades));
        } catch (Exception e) {
            lblMensaje.setText("Error al cargar localidades: " + e.getMessage());
        } finally {
            con.cerrarRecursos(conn);
        }
    }

    // --- MÉTODOS DE LOS BOTONES "AGREGAR (+)" ---

    @FXML
    void agregarEstado(ActionEvent event) {
        String nombreNuevo = mostrarInput("Nuevo Estado", "Ingrese el nombre del estado:");
        if (nombreNuevo == null || nombreNuevo.trim().isEmpty()) return;

        try {
            Estado nuevo = new Estado();
            nuevo.setNombre(nombreNuevo.trim());

            Connection conn = con.obtenerConexion();
            estadoDAO.setConexion(conn);
            Estado insertado = estadoDAO.insertar(nuevo);
            con.cerrarRecursos(conn);

            if (insertado != null) {
                cargarEstados(); // Recargamos la lista
                seleccionarEnCombo(cmbEstado, insertado.getId()); // Seleccionamos el nuevo
                lblMensaje.setText("Estado agregado.");
                lblMensaje.setTextFill(Color.GREEN);
            }
        } catch (Exception e) {
            lblMensaje.setText("Error: " + e.getMessage());
            lblMensaje.setTextFill(Color.RED);
        }
    }

    @FXML
    void agregarMunicipio(ActionEvent event) {
        Estado estadoPadre = cmbEstado.getValue();
        if (estadoPadre == null) {
            mostrarAlerta("Error", "Debe seleccionar un Estado primero.");
            return;
        }

        String nombreNuevo = mostrarInput("Nuevo Municipio", "Agregando municipio a: " + estadoPadre.getNombre() + "\nIngrese nombre:");
        if (nombreNuevo == null || nombreNuevo.trim().isEmpty()) return;

        try {
            Municipio nuevo = new Municipio();
            nuevo.setNombre(nombreNuevo.trim());
            nuevo.setEstado(estadoPadre); // IMPORTANTE: Relacionar padre

            Connection conn = con.obtenerConexion();
            municipioDAO.setConexion(conn);
            Municipio insertado = municipioDAO.insertar(nuevo);
            con.cerrarRecursos(conn);

            if (insertado != null) {
                cargarMunicipios(estadoPadre.getId());
                seleccionarEnCombo(cmbMunicipio, insertado.getId());
                lblMensaje.setText("Municipio agregado.");
                lblMensaje.setTextFill(Color.GREEN);
            }
        } catch (Exception e) {
            lblMensaje.setText("Error: " + e.getMessage());
            lblMensaje.setTextFill(Color.RED);
        }
    }

    @FXML
    void agregarLocalidad(ActionEvent event) {
        Municipio muniPadre = cmbMunicipio.getValue();
        if (muniPadre == null) {
            mostrarAlerta("Error", "Debe seleccionar un Municipio primero.");
            return;
        }

        String nombreLoc = mostrarInput("Nueva Localidad", "Municipio: " + muniPadre.getNombre() + "\nIngrese nombre localidad:");
        if (nombreLoc == null || nombreLoc.trim().isEmpty()) return;

        String cpLoc = mostrarInput("Código Postal", "Ingrese C.P. para " + nombreLoc + ":");
        if (cpLoc == null) cpLoc = "00000"; // Default si cancela

        try {
            Localidad nueva = new Localidad();
            nueva.setLocalidad(nombreLoc.trim());
            nueva.setCodigoPostal(cpLoc.trim());
            nueva.setMunicipio(muniPadre); // IMPORTANTE: Relacionar padre

            Connection conn = con.obtenerConexion();
            localidadDAO.setConexion(conn);
            Localidad insertada = localidadDAO.insertar(nueva);
            con.cerrarRecursos(conn);

            if (insertada != null) {
                cargarLocalidades(muniPadre.getId());
                seleccionarEnCombo(cmbLocalidad, insertada.getId());
                lblMensaje.setText("Localidad agregada.");
                lblMensaje.setTextFill(Color.GREEN);
            }
        } catch (Exception e) {
            lblMensaje.setText("Error: " + e.getMessage());
            lblMensaje.setTextFill(Color.RED);
        }
    }

    // --- LÓGICA PRINCIPAL DE REGISTRO ---

    @FXML
    void registrar(ActionEvent event) {
        try {
            Localidad locSeleccionada = cmbLocalidad.getValue();
            if (locSeleccionada == null) {
                lblMensaje.setText("Error: Ubicación incompleta.");
                lblMensaje.setTextFill(Color.RED);
                return;
            }

            Empleado nuevo = new Empleado();
            nuevo.setNombre(txtNombre.getText());
            nuevo.setApellidoPaterno(txtApellidoPaterno.getText());
            nuevo.setApellidoMaterno(txtApellidoMaterno.getText());
            nuevo.setCargo(txtCargo.getText());
            nuevo.setEmail(txtEmail.getText());
            nuevo.setTelefono(txtTelefono.getText());
            nuevo.setCalle(txtCalle.getText());
            nuevo.setNumeroCalle(txtNumeroCalle.getText());
            nuevo.setUsuario(txtUsuario.getText());
            nuevo.setContrasenia(txtContraseña.getText());

            // Asignar ID de localidad
            nuevo.setIdLocalidad(locSeleccionada.getId());

            Connection conn = con.obtenerConexion();
            empleadoDAO.setConexion(conn);
            Empleado resultado = empleadoDAO.insertar(nuevo);
            con.cerrarRecursos(conn);

            if (resultado != null) {
                lblMensaje.setText("¡Empleado registrado! ID: " + resultado.getId());
                lblMensaje.setTextFill(Color.GREEN);
                limpiarFormulario();
            }

        } catch (Exception e) {
            lblMensaje.setText("Error: " + e.getMessage());
            lblMensaje.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    @FXML
    void volverLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mx/uaemex/fi/bases/libreria/LoginVista.fxml"));
            Parent root = loader.load();
            LoginControlador controller = loader.getController();
            controller.setConexionBD(this.con);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("Sistema de Librería - Iniciar Sesión");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- HELPERS ---

    private void limpiarFormulario() {
        txtNombre.clear(); txtApellidoPaterno.clear(); txtApellidoMaterno.clear();
        txtCargo.clear(); txtEmail.clear(); txtTelefono.clear();
        txtCalle.clear(); txtNumeroCalle.clear(); txtUsuario.clear(); txtContraseña.clear();
        cmbEstado.getSelectionModel().clearSelection();
        cmbMunicipio.getItems().clear();
        cmbLocalidad.getItems().clear();
    }

    private String mostrarInput(String titulo, String contenido) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(titulo);
        dialog.setHeaderText(null);
        dialog.setContentText(contenido);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    /**
     * Genérico para seleccionar automáticamente el item recién agregado
     */
    private <T extends ElementoConID> void seleccionarEnCombo(ComboBox<T> combo, int id) {
        for (T item : combo.getItems()) {
            if (item.getId() == id) {
                combo.getSelectionModel().select(item);
                return;
            }
        }
    }
}