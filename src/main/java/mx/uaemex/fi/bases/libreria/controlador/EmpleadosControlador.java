package mx.uaemex.fi.bases.libreria.controlador;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

    // --- AGREGAR ---
    @FXML private TextField txtNombreAdd, txtPaternoAdd, txtMaternoAdd;
    @FXML private ComboBox<String> cmbCargoAdd;
    @FXML private TextField txtTelefonoAdd, txtEmailAdd, txtCalleAdd, txtNumeroAdd, txtUsuarioAdd;
    @FXML private PasswordField txtContraAdd;
    @FXML private ComboBox<Estado> cmbEstadoAdd;
    @FXML private ComboBox<Municipio> cmbMuniAdd;
    @FXML private ComboBox<Localidad> cmbLocalidadAdd;

    // --- TABLA Y BUSQUEDA ---
    @FXML private TableView<Empleado> tblEmpleados;
    @FXML private TableColumn<Empleado, Integer> colId;
    @FXML private TableColumn<Empleado, String> colNombre, colApellido, colCargo, colUsuario, colEmail, colTelefono, colLocalidadInfo, colActivo;
    @FXML private TextField txtBuscarNombre, txtBuscarUsuario;
    @FXML private ComboBox<Estado> cmbBuscarEstado;
    @FXML private Label lblTotal;

    // --- EDITAR ---
    @FXML private TextField txtNombreEdit, txtPaternoEdit, txtMaternoEdit;
    @FXML private ComboBox<String> cmbCargoEdit;
    @FXML private TextField txtTelefonoEdit, txtEmailEdit, txtCalleEdit, txtNumeroEdit, txtUsuarioEdit;
    @FXML private PasswordField txtContraEdit;
    @FXML private ComboBox<Estado> cmbEstadoEdit;
    @FXML private ComboBox<Municipio> cmbMuniEdit;
    @FXML private ComboBox<Localidad> cmbLocalidadEdit;

    @FXML private Button btnActualizar, btnEliminar;
    @FXML private Label lblMensaje;

    private ConexionBD con;
    private EmpleadosDAOPsqlImp empleadoDAO;
    private LocalidadDAOPsqlImp localidadDAO;
    private MunicipioDAOPsqlImp municipioDAO;
    private EstadoDAOPsqlImp estadoDAO;

    private Empleado empleadoSeleccionado;

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

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        colApellido.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getApellidoPaterno() + " " + cell.getValue().getApellidoMaterno()));

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
            empleadoSeleccionado = newSel;
            if (newSel != null) {
                cargarEmpleadoEnEdicion(newSel);
                btnActualizar.setDisable(false);
                btnEliminar.setDisable(false);

                if (Boolean.TRUE.equals(newSel.isActivo())) {
                    btnEliminar.setText("Dar de Baja");
                    btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                } else {
                    btnEliminar.setText("Reactivar");
                    btnEliminar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                }
            }
        });
    }

    private void configurarCascadas() {
        cmbEstadoAdd.setOnAction(e -> filtrarMunicipios(cmbEstadoAdd, cmbMuniAdd, cmbLocalidadAdd));
        cmbMuniAdd.setOnAction(e -> filtrarLocalidades(cmbMuniAdd, cmbLocalidadAdd));
        cmbEstadoEdit.setOnAction(e -> filtrarMunicipios(cmbEstadoEdit, cmbMuniEdit, cmbLocalidadEdit));
        cmbMuniEdit.setOnAction(e -> filtrarLocalidades(cmbMuniEdit, cmbLocalidadEdit));
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
        cargarEmpleados(null);
        recargarListasUbicacion(null);
        ObservableList<String> cargos = FXCollections.observableArrayList("Administrador", "Vendedor");
        cmbCargoAdd.setItems(cargos);
        cmbCargoEdit.setItems(cargos);
    }

    @FXML
    void recargarListasUbicacion(ActionEvent e) {
        try {
            ArrayList<Estado> estados = estadoDAO.consultar();
            todosMunicipios = municipioDAO.consultar();
            todasLocalidades = localidadDAO.consultar();

            ObservableList<Estado> obsEstados = FXCollections.observableArrayList(estados);
            cmbEstadoAdd.setItems(obsEstados);
            cmbEstadoEdit.setItems(obsEstados);
            cmbBuscarEstado.setItems(obsEstados);

            tblEmpleados.refresh();
        } catch(Exception ex) { ex.printStackTrace(); }
    }

    @FXML
    void cargarEmpleados(ActionEvent e) {
        txtBuscarNombre.clear();
        txtBuscarUsuario.clear();
        cmbBuscarEstado.getSelectionModel().clearSelection();
        try {
            Empleado filtroTodos = new Empleado();
            filtroTodos.setActivo(null);
            ArrayList<Empleado> lista = empleadoDAO.consultar(filtroTodos);
            tblEmpleados.setItems(FXCollections.observableArrayList(lista));
            lblTotal.setText("Encontrados: " + lista.size());
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML
    void buscarEmpleado(ActionEvent e) {
        Empleado filtro = new Empleado();
        if(!txtBuscarNombre.getText().isEmpty()) filtro.setNombre(txtBuscarNombre.getText());
        if(!txtBuscarUsuario.getText().isEmpty()) filtro.setUsuario(txtBuscarUsuario.getText());
        filtro.setActivo(null);

        ArrayList<Empleado> res = empleadoDAO.consultar(filtro);

        if (cmbBuscarEstado.getValue() != null) {
            res = res.stream().filter(emp -> {
                for(Localidad loc : todasLocalidades) {
                    if(loc.getId() == emp.getIdLocalidad()) {
                        return loc.getEstado().getNombre().equals(cmbBuscarEstado.getValue().getNombre());
                    }
                }
                return false;
            }).collect(Collectors.toCollection(ArrayList::new));
        }

        tblEmpleados.setItems(FXCollections.observableArrayList(res));
        lblTotal.setText("Encontrados: " + res.size());
    }

    @FXML
    void agregarEmpleado(ActionEvent e) {
        try {
            if(cmbLocalidadAdd.getValue() == null) throw new RuntimeException("Seleccione una Localidad (Complete los 3 pasos).");
            if(cmbCargoAdd.getValue() == null) throw new RuntimeException("Seleccione un Cargo");

            String usuario = txtUsuarioAdd.getText().trim();
            if(usuario.isEmpty()) throw new RuntimeException("Usuario obligatorio");

            Empleado busqueda = new Empleado();
            busqueda.setUsuario(usuario);
            busqueda.setActivo(null);

            ArrayList<Empleado> existentes = empleadoDAO.consultar(busqueda);

            if (!existentes.isEmpty()) {
                Empleado existente = existentes.get(0);
                if (Boolean.TRUE.equals(existente.isActivo())) {
                    throw new RuntimeException("El usuario '" + usuario + "' ya existe.");
                } else {
                    actualizarDatosObjeto(existente, true);
                    existente.setActivo(true);
                    empleadoDAO.actualizar(existente);
                    lblMensaje.setText("Empleado reactivado y actualizado.");
                    limpiarFormularioAgregar();
                    cargarEmpleados(null);
                    return;
                }
            }

            Empleado nuevo = new Empleado();
            actualizarDatosObjeto(nuevo, true);
            empleadoDAO.insertar(nuevo);

            lblMensaje.setText("Empleado registrado correctamente.");
            limpiarFormularioAgregar();
            cargarEmpleados(null);

        } catch (Exception ex) {
            lblMensaje.setText("Error: " + ex.getMessage());
        }
    }

    private void actualizarDatosObjeto(Empleado emp, boolean desdeAdd) {
        if(desdeAdd) {
            emp.setNombre(txtNombreAdd.getText());
            emp.setApellidoPaterno(txtPaternoAdd.getText());
            emp.setApellidoMaterno(txtMaternoAdd.getText());
            emp.setCargo(cmbCargoAdd.getValue());
            emp.setTelefono(txtTelefonoAdd.getText());
            emp.setEmail(txtEmailAdd.getText());
            emp.setCalle(txtCalleAdd.getText());
            emp.setNumeroCalle(txtNumeroAdd.getText());
            emp.setUsuario(txtUsuarioAdd.getText());
            emp.setContrasenia(txtContraAdd.getText());
            emp.setIdLocalidad(cmbLocalidadAdd.getValue().getId());
        } else {
            emp.setNombre(txtNombreEdit.getText());
            emp.setApellidoPaterno(txtPaternoEdit.getText());
            emp.setApellidoMaterno(txtMaternoEdit.getText());
            emp.setCargo(cmbCargoEdit.getValue());
            emp.setTelefono(txtTelefonoEdit.getText());
            emp.setEmail(txtEmailEdit.getText());
            emp.setCalle(txtCalleEdit.getText());
            emp.setNumeroCalle(txtNumeroEdit.getText());
            emp.setUsuario(txtUsuarioEdit.getText());
            if(!txtContraEdit.getText().isEmpty()) emp.setContrasenia(txtContraEdit.getText());
            if(cmbLocalidadEdit.getValue() != null) emp.setIdLocalidad(cmbLocalidadEdit.getValue().getId());
        }
    }

    private void cargarEmpleadoEnEdicion(Empleado emp) {
        txtNombreEdit.setText(emp.getNombre());
        txtPaternoEdit.setText(emp.getApellidoPaterno());
        txtMaternoEdit.setText(emp.getApellidoMaterno());
        cmbCargoEdit.setValue(emp.getCargo());
        txtTelefonoEdit.setText(emp.getTelefono());
        txtEmailEdit.setText(emp.getEmail());
        txtCalleEdit.setText(emp.getCalle());
        txtNumeroEdit.setText(emp.getNumeroCalle());
        txtUsuarioEdit.setText(emp.getUsuario());
        txtContraEdit.clear();

        Localidad locCompleta = null;
        for(Localidad l : todasLocalidades) {
            if(l.getId() == emp.getIdLocalidad()) {
                locCompleta = l;
                break;
            }
        }

        if (locCompleta != null) {
            for(Estado e : cmbEstadoEdit.getItems()) {
                if(e.getNombre().equals(locCompleta.getEstado().getNombre())) {
                    cmbEstadoEdit.setValue(e);
                    break;
                }
            }
            for(Municipio m : cmbMuniEdit.getItems()) {
                if(m.getNombre().equals(locCompleta.getMunicipio().getNombre())) {
                    cmbMuniEdit.setValue(m);
                    break;
                }
            }
            for(Localidad l : cmbLocalidadEdit.getItems()) {
                if(l.getId() == locCompleta.getId()) {
                    cmbLocalidadEdit.setValue(l);
                    break;
                }
            }
        }
    }

    @FXML
    void actualizarEmpleado(ActionEvent e) {
        try {
            if(empleadoSeleccionado == null) return;
            actualizarDatosObjeto(empleadoSeleccionado, false);
            empleadoDAO.actualizar(empleadoSeleccionado);
            lblMensaje.setText("Empleado actualizado.");
            limpiarSeleccion(null);
            cargarEmpleados(null);
        } catch (Exception ex) {
            lblMensaje.setText("Error: " + ex.getMessage());
        }
    }

    @FXML
    void eliminarOReactivarEmpleado(ActionEvent e) {
        try {
            if(empleadoSeleccionado == null) return;

            if (Boolean.TRUE.equals(empleadoSeleccionado.isActivo())) {
                empleadoDAO.borrar(empleadoSeleccionado);
                lblMensaje.setText("Empleado dado de baja.");
            } else {
                empleadoSeleccionado.setActivo(true);
                empleadoDAO.actualizar(empleadoSeleccionado);
                lblMensaje.setText("Empleado reactivado.");
            }

            limpiarSeleccion(null);
            cargarEmpleados(null);
        } catch (Exception ex) {
            lblMensaje.setText("Error: " + ex.getMessage());
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
            // CORRECCIÓN: Tamaño reducido para evitar que se desborde
            stage.setTitle("Gestión Rápida de Ubicaciones");
            stage.setScene(new Scene(root, 800, 550));
            stage.centerOnScreen();
            stage.showAndWait();

            recargarListasUbicacion(null);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    void limpiarSeleccion(ActionEvent e) {
        empleadoSeleccionado = null;
        txtNombreEdit.clear(); txtPaternoEdit.clear(); txtMaternoEdit.clear();
        cmbCargoEdit.getSelectionModel().clearSelection();
        txtTelefonoEdit.clear(); txtEmailEdit.clear();
        txtCalleEdit.clear(); txtNumeroEdit.clear(); txtUsuarioEdit.clear();
        txtContraEdit.clear();

        cmbEstadoEdit.getSelectionModel().clearSelection();
        cmbMuniEdit.setDisable(true);
        cmbLocalidadEdit.setDisable(true);

        tblEmpleados.getSelectionModel().clearSelection();
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
        btnEliminar.setText("Eliminar / Reactivar");
    }

    private void limpiarFormularioAgregar() {
        txtNombreAdd.clear(); txtPaternoAdd.clear(); txtMaternoAdd.clear();
        cmbCargoAdd.getSelectionModel().clearSelection();
        txtTelefonoAdd.clear(); txtEmailAdd.clear();
        txtCalleAdd.clear(); txtNumeroAdd.clear(); txtUsuarioAdd.clear();
        txtContraAdd.clear();

        cmbEstadoAdd.getSelectionModel().clearSelection();
        cmbMuniAdd.setDisable(true);
        cmbLocalidadAdd.setDisable(true);
    }
}