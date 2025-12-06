package mx.uaemex.fi.bases.libreria.controlador;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mx.uaemex.fi.bases.libreria.modelo.*;
import mx.uaemex.fi.bases.libreria.modelo.data.Empleado;
import mx.uaemex.fi.bases.libreria.modelo.data.Estado;
import mx.uaemex.fi.bases.libreria.modelo.data.Localidad;
import mx.uaemex.fi.bases.libreria.modelo.data.Municipio;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class EmpleadosControlador {

    // --- FORMULARIO ÚNICO ---
    @FXML private TextField txtNombre, txtPaterno, txtMaterno;
    @FXML private ComboBox<String> cmbCargo;
    @FXML private TextField txtTelefono, txtEmail, txtCalle, txtNumero, txtUsuario;
    @FXML private PasswordField txtContra;
    @FXML private CheckBox chkActivo;

    @FXML private ComboBox<Estado> cmbEstado;
    @FXML private ComboBox<Municipio> cmbMuni;
    @FXML private ComboBox<Localidad> cmbLocalidad;
    @FXML private Label lblModo;
    @FXML private Button btnEliminar;

    // --- TABLA Y BUSQUEDA ---
    @FXML private TableView<Empleado> tblEmpleados;
    @FXML private TableColumn<Empleado, Integer> colId;
    @FXML private TableColumn<Empleado, String> colNombre, colPaterno, colMaterno, colCargo, colUsuario, colEmail, colTelefono, colCalle, colNumero, colLocalidadInfo, colActivo;

    // Filtros COMPLETOS
    @FXML private TextField txtBuscarNombre, txtBuscarPaterno, txtBuscarMaterno;
    @FXML private ComboBox<String> cmbBuscarCargo;
    @FXML private TextField txtBuscarUsuario, txtBuscarEmail;
    @FXML private TextField txtBuscarTelefono, txtBuscarCalle, txtBuscarNumero;
    @FXML private ComboBox<Estado> cmbBuscarEstado;
    @FXML private ComboBox<Municipio> cmbBuscarMuni;
    @FXML private ComboBox<Localidad> cmbBuscarLocalidad;
    @FXML private ComboBox<String> cmbBuscarEstatus;

    @FXML private Label lblTotal;
    @FXML private Label lblMensaje; // Ahora está arriba

    private ConexionBD con;
    private EmpleadosDAOPsqlImp empleadoDAO;
    private LocalidadDAOPsqlImp localidadDAO;
    private MunicipioDAOPsqlImp municipioDAO;
    private EstadoDAOPsqlImp estadoDAO;

    private Empleado empleadoSeleccionado = null;
    private ArrayList<Municipio> todosMunicipios;
    private ArrayList<Localidad> todasLocalidades;

    public EmpleadosControlador() {
        this.empleadoDAO = new EmpleadosDAOPsqlImp();
        this.localidadDAO = new LocalidadDAOPsqlImp();
        this.municipioDAO = new MunicipioDAOPsqlImp();
        this.estadoDAO = new EstadoDAOPsqlImp();
    }

    public void setConexionBD(ConexionBD conexion, Connection connSql) {
        this.con = conexion;
        this.empleadoDAO.setConexion(connSql);
        this.localidadDAO.setConexion(connSql);
        this.municipioDAO.setConexion(connSql);
        this.estadoDAO.setConexion(connSql);

        configurarTabla();
        configurarCascadas();
        cargarDatosIniciales();
    }

    @FXML
    void regresarMenu(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPaterno.setCellValueFactory(new PropertyValueFactory<>("apellidoPaterno"));
        colMaterno.setCellValueFactory(new PropertyValueFactory<>("apellidoMaterno"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCalle.setCellValueFactory(new PropertyValueFactory<>("calle"));
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroCalle"));

        colActivo.setCellValueFactory(cell ->
                new SimpleStringProperty(Boolean.TRUE.equals(cell.getValue().isActivo()) ? "Sí" : "No"));

        colLocalidadInfo.setCellValueFactory(cell -> {
            int idLoc = cell.getValue().getIdLocalidad();
            if(todasLocalidades != null) {
                for(Localidad l : todasLocalidades) {
                    if(l.getId() == idLoc) return new SimpleStringProperty(l.toString());
                }
            }
            return new SimpleStringProperty("ID: " + idLoc);
        });

        tblEmpleados.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                cargarEmpleadoEnFormulario(newSel);
            }
        });
    }

    private void configurarCascadas() {
        cmbEstado.setOnAction(e -> filtrarMunicipios(cmbEstado, cmbMuni, cmbLocalidad));
        cmbMuni.setOnAction(e -> filtrarLocalidades(cmbMuni, cmbLocalidad));

        cmbBuscarEstado.setOnAction(e -> filtrarMunicipios(cmbBuscarEstado, cmbBuscarMuni, cmbBuscarLocalidad));
        cmbBuscarMuni.setOnAction(e -> filtrarLocalidades(cmbBuscarMuni, cmbBuscarLocalidad));
    }

    private void filtrarMunicipios(ComboBox<Estado> cbEstado, ComboBox<Municipio> cbMuni, ComboBox<Localidad> cbLoc) {
        Estado est = cbEstado.getValue();
        cbMuni.getSelectionModel().clearSelection();
        cbLoc.getSelectionModel().clearSelection();
        cbLoc.setDisable(true);

        if (est != null) {
            ArrayList<Municipio> filtrados = todosMunicipios.stream()
                    .filter(m -> m.getEstado().getId() == est.getId())
                    .collect(Collectors.toCollection(ArrayList::new));
            cbMuni.setItems(FXCollections.observableArrayList(filtrados));
            cbMuni.setDisable(false);
        } else {
            cbMuni.setDisable(true);
        }
    }

    private void filtrarLocalidades(ComboBox<Municipio> cbMuni, ComboBox<Localidad> cbLoc) {
        Municipio mun = cbMuni.getValue();
        cbLoc.getSelectionModel().clearSelection();
        if (mun != null) {
            ArrayList<Localidad> filtrados = todasLocalidades.stream()
                    .filter(l -> l.getMunicipio().getNombre().equals(mun.getNombre()))
                    .collect(Collectors.toCollection(ArrayList::new));
            cbLoc.setItems(FXCollections.observableArrayList(filtrados));
            cbLoc.setDisable(false);
        } else {
            cbLoc.setDisable(true);
        }
    }

    private void cargarDatosIniciales() {
        recargarListasUbicacion(null);

        ObservableList<String> cargos = FXCollections.observableArrayList("Administrador", "Vendedor", "Gerente");
        cmbCargo.setItems(cargos);
        cmbBuscarCargo.setItems(cargos);

        cmbBuscarEstatus.setItems(FXCollections.observableArrayList("Activos", "Inactivos", "Todos"));

        cargarEmpleados(null);
    }

    @FXML
    void recargarListasUbicacion(ActionEvent e) {
        try {
            ArrayList<Estado> estados = estadoDAO.consultar();
            todosMunicipios = municipioDAO.consultar();
            todasLocalidades = localidadDAO.consultar();

            ObservableList<Estado> obsEstados = FXCollections.observableArrayList(estados);
            cmbEstado.setItems(obsEstados);
            cmbBuscarEstado.setItems(obsEstados);

            cmbBuscarMuni.setItems(FXCollections.observableArrayList());
            cmbBuscarLocalidad.setItems(FXCollections.observableArrayList());

            tblEmpleados.refresh();
        } catch(Exception ex) { ex.printStackTrace(); }
    }

    @FXML
    void cargarEmpleados(ActionEvent e) {
        limpiarFiltros();
        cmbBuscarEstatus.getSelectionModel().select("Todos");
        buscarEmpleado(null);
    }

    private void limpiarFiltros() {
        txtBuscarNombre.clear(); txtBuscarPaterno.clear(); txtBuscarMaterno.clear();
        txtBuscarUsuario.clear(); txtBuscarEmail.clear(); txtBuscarTelefono.clear();
        txtBuscarCalle.clear(); txtBuscarNumero.clear();
        cmbBuscarCargo.getSelectionModel().clearSelection();
        cmbBuscarEstado.getSelectionModel().clearSelection();
        cmbBuscarMuni.getSelectionModel().clearSelection();
        cmbBuscarLocalidad.getSelectionModel().clearSelection();
    }

    @FXML
    void buscarEmpleado(ActionEvent e) {
        Empleado filtro = new Empleado();

        if(!txtBuscarNombre.getText().isEmpty()) filtro.setNombre(txtBuscarNombre.getText());
        if(!txtBuscarPaterno.getText().isEmpty()) filtro.setApellidoPaterno(txtBuscarPaterno.getText());
        if(!txtBuscarMaterno.getText().isEmpty()) filtro.setApellidoMaterno(txtBuscarMaterno.getText());
        if(cmbBuscarCargo.getValue() != null) filtro.setCargo(cmbBuscarCargo.getValue());

        if(!txtBuscarUsuario.getText().isEmpty()) filtro.setUsuario(txtBuscarUsuario.getText());
        if(!txtBuscarEmail.getText().isEmpty()) filtro.setEmail(txtBuscarEmail.getText());
        if(!txtBuscarTelefono.getText().isEmpty()) filtro.setTelefono(txtBuscarTelefono.getText());

        if(!txtBuscarCalle.getText().isEmpty()) filtro.setCalle(txtBuscarCalle.getText());
        if(!txtBuscarNumero.getText().isEmpty()) filtro.setNumeroCalle(txtBuscarNumero.getText());

        String estatus = cmbBuscarEstatus.getValue();
        if ("Activos".equals(estatus)) filtro.setActivo(true);
        else if ("Inactivos".equals(estatus)) filtro.setActivo(false);
        else filtro.setActivo(null);

        ArrayList<Empleado> res = empleadoDAO.consultar(filtro);

        if (cmbBuscarLocalidad.getValue() != null) {
            int idLoc = cmbBuscarLocalidad.getValue().getId();
            res = res.stream().filter(emp -> emp.getIdLocalidad() == idLoc).collect(Collectors.toCollection(ArrayList::new));
        } else if (cmbBuscarMuni.getValue() != null) {
            String nombreMuni = cmbBuscarMuni.getValue().getNombre();
            res = res.stream().filter(emp -> {
                for(Localidad loc : todasLocalidades) {
                    if(loc.getId() == emp.getIdLocalidad()) return loc.getMunicipio().getNombre().equals(nombreMuni);
                }
                return false;
            }).collect(Collectors.toCollection(ArrayList::new));
        } else if (cmbBuscarEstado.getValue() != null) {
            String nombreEstado = cmbBuscarEstado.getValue().getNombre();
            res = res.stream().filter(emp -> {
                for(Localidad loc : todasLocalidades) {
                    if(loc.getId() == emp.getIdLocalidad()) return loc.getEstado().getNombre().equals(nombreEstado);
                }
                return false;
            }).collect(Collectors.toCollection(ArrayList::new));
        }

        tblEmpleados.setItems(FXCollections.observableArrayList(res));
        lblTotal.setText("Registros: " + res.size());
    }

    @FXML
    void nuevoEmpleado(ActionEvent e) {
        limpiarFormulario();
        lblModo.setText("(Nuevo)");
        lblModo.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        btnEliminar.setVisible(false);
        tblEmpleados.getSelectionModel().clearSelection();
        empleadoSeleccionado = null;
        chkActivo.setSelected(true);
        lblMensaje.setText("");
    }

    @FXML
    void guardarEmpleado(ActionEvent e) {
        try {
            if(cmbLocalidad.getValue() == null) throw new RuntimeException("Seleccione una Localidad.");
            if(cmbCargo.getValue() == null) throw new RuntimeException("Seleccione un Cargo.");
            String usuario = txtUsuario.getText().trim();
            if(usuario.isEmpty()) throw new RuntimeException("Usuario obligatorio");

            if (empleadoSeleccionado == null) {
                // INSERTAR
                Empleado busqueda = new Empleado();
                busqueda.setUsuario(usuario);
                busqueda.setActivo(null);
                ArrayList<Empleado> existentes = empleadoDAO.consultar(busqueda);

                if (!existentes.isEmpty()) {
                    Empleado existente = existentes.get(0);
                    if (Boolean.TRUE.equals(existente.isActivo())) {
                        throw new RuntimeException("El usuario '" + usuario + "' ya existe.");
                    } else {
                        leerDatosDelFormulario(existente);
                        existente.setActivo(true);
                        empleadoDAO.actualizar(existente);
                        mostrarMensaje("Empleado reactivado exitosamente.", true);
                        nuevoEmpleado(null);
                        buscarEmpleado(null);
                        return;
                    }
                }

                Empleado nuevo = new Empleado();
                leerDatosDelFormulario(nuevo);
                empleadoDAO.insertar(nuevo);
                mostrarMensaje("Empleado registrado exitosamente.", true);
            } else {
                // ACTUALIZAR
                leerDatosDelFormulario(empleadoSeleccionado);
                empleadoDAO.actualizar(empleadoSeleccionado);
                mostrarMensaje("Empleado actualizado correctamente.", true);
            }

            // NOTA: No llamamos a nuevoEmpleado(null) inmediatamente aquí para que no borre el mensaje
            // Limpiamos campos pero mantenemos el mensaje
            txtNombre.clear(); txtPaterno.clear(); txtMaterno.clear(); txtTelefono.clear();
            txtEmail.clear(); txtCalle.clear(); txtNumero.clear(); txtUsuario.clear(); txtContra.clear();
            cmbCargo.getSelectionModel().clearSelection();
            cmbEstado.getSelectionModel().clearSelection();
            cmbMuni.setDisable(true); cmbLocalidad.setDisable(true);
            empleadoSeleccionado = null;

            buscarEmpleado(null);

        } catch (Exception ex) {
            mostrarMensaje("Error: " + ex.getMessage(), false);
        }
    }

    private void leerDatosDelFormulario(Empleado emp) {
        emp.setNombre(txtNombre.getText());
        emp.setApellidoPaterno(txtPaterno.getText());
        emp.setApellidoMaterno(txtMaterno.getText());
        emp.setCargo(cmbCargo.getValue());
        emp.setTelefono(txtTelefono.getText());
        emp.setEmail(txtEmail.getText());
        emp.setCalle(txtCalle.getText());
        emp.setNumeroCalle(txtNumero.getText());
        emp.setUsuario(txtUsuario.getText());
        if(!txtContra.getText().isEmpty()) emp.setContrasenia(txtContra.getText());
        if(cmbLocalidad.getValue() != null) emp.setIdLocalidad(cmbLocalidad.getValue().getId());
        emp.setActivo(chkActivo.isSelected());
    }

    private void cargarEmpleadoEnFormulario(Empleado emp) {
        // Al cargar un empleado, limpiamos cualquier mensaje previo
        lblMensaje.setText("");

        empleadoSeleccionado = emp;
        lblModo.setText("(Editando ID: " + emp.getId() + ")");
        lblModo.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");

        txtNombre.setText(emp.getNombre());
        txtPaterno.setText(emp.getApellidoPaterno());
        txtMaterno.setText(emp.getApellidoMaterno());
        cmbCargo.setValue(emp.getCargo());
        txtTelefono.setText(emp.getTelefono());
        txtEmail.setText(emp.getEmail());
        txtCalle.setText(emp.getCalle());
        txtNumero.setText(emp.getNumeroCalle());
        txtUsuario.setText(emp.getUsuario());
        txtContra.clear();

        chkActivo.setSelected(Boolean.TRUE.equals(emp.isActivo()));

        Localidad locCompleta = null;
        if(todasLocalidades != null) {
            for(Localidad l : todasLocalidades) {
                if(l.getId() == emp.getIdLocalidad()) {
                    locCompleta = l;
                    break;
                }
            }
        }

        if (locCompleta != null) {
            for(Estado e : cmbEstado.getItems()) {
                if(e.getNombre().equals(locCompleta.getEstado().getNombre())) {
                    cmbEstado.setValue(e);
                    break;
                }
            }
            for(Municipio m : cmbMuni.getItems()) {
                if(m.getNombre().equals(locCompleta.getMunicipio().getNombre())) {
                    cmbMuni.setValue(m);
                    break;
                }
            }
            for(Localidad l : cmbLocalidad.getItems()) {
                if(l.getId() == locCompleta.getId()) {
                    cmbLocalidad.setValue(l);
                    break;
                }
            }
        }

        btnEliminar.setVisible(true);
        btnEliminar.setText("Eliminar / Dar de Baja");
        if (Boolean.TRUE.equals(emp.isActivo())) {
            btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        } else {
            btnEliminar.setText("Reactivar");
            btnEliminar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        }
    }

    @FXML
    void eliminarOReactivarEmpleado(ActionEvent e) {
        try {
            if(empleadoSeleccionado == null) return;

            String msg = "";
            if (Boolean.TRUE.equals(empleadoSeleccionado.isActivo())) {
                empleadoDAO.borrar(empleadoSeleccionado);
                msg = "Se ha inactivado al empleado correctamente.";
            } else {
                empleadoSeleccionado.setActivo(true);
                empleadoDAO.actualizar(empleadoSeleccionado);
                msg = "Empleado reactivado correctamente.";
            }

            // Limpiar formulario y mostrar mensaje
            limpiarFormulario();
            mostrarMensaje(msg, true);

            buscarEmpleado(null);
        } catch (Exception ex) {
            mostrarMensaje("No se pudo eliminar/reactivar: " + ex.getMessage(), false);
        }
    }

    @FXML
    void abrirGestionUbicaciones(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mx/uaemex/fi/bases/libreria/UbicacionesVista.fxml"));
            Parent root = loader.load();

            UbicacionesControlador ctrl = loader.getController();
            Connection connSql = this.con.obtenerConexion();
            ctrl.setConexionBD(this.con, connSql);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Gestión Rápida de Ubicaciones");
            stage.setScene(new Scene(root, 800, 550));
            stage.centerOnScreen();
            stage.showAndWait();

            recargarListasUbicacion(null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void limpiarFormulario() {
        txtNombre.clear(); txtPaterno.clear(); txtMaterno.clear();
        cmbCargo.getSelectionModel().clearSelection();
        txtTelefono.clear(); txtEmail.clear();
        txtCalle.clear(); txtNumero.clear(); txtUsuario.clear();
        txtContra.clear();

        cmbEstado.getSelectionModel().clearSelection();
        cmbMuni.setDisable(true);
        cmbLocalidad.setDisable(true);
        chkActivo.setSelected(true);

        lblModo.setText("(Nuevo)");
        lblModo.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        btnEliminar.setVisible(false);
        empleadoSeleccionado = null;
        // NOTA: No limpiamos el mensaje aquí para que persista tras la acción
    }

    private void mostrarMensaje(String mensaje, boolean exito) {
        lblMensaje.setText(mensaje);
        if (exito) {
            // Verde limpio sin fondo para que no se vea una caja fea
            lblMensaje.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 13px;");
        } else {
            // Rojo limpio
            lblMensaje.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold; -fx-font-size: 13px;");
        }
    }
}