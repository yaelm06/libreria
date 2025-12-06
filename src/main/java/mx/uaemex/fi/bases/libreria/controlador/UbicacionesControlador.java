package mx.uaemex.fi.bases.libreria.controlador;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import mx.uaemex.fi.bases.libreria.modelo.*;
import mx.uaemex.fi.bases.libreria.modelo.data.Estado;
import mx.uaemex.fi.bases.libreria.modelo.data.Localidad;
import mx.uaemex.fi.bases.libreria.modelo.data.Municipio;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class UbicacionesControlador {

    @FXML private TextField txtNombreEstadoAgregar;
    @FXML private TableView<Estado> tblEstados;
    @FXML private TableColumn<Estado, Integer> colIdEstado;
    @FXML private TableColumn<Estado, String> colNombreEstado;
    @FXML private TextField txtBuscarEstado;
    @FXML private Label lblTotalEstados;
    @FXML private TextField txtNombreEstadoEditar;
    @FXML private Button btnActualizarEstado, btnEliminarEstado;

    @FXML private ComboBox<Estado> cmbEstadoMuniAgregar;
    @FXML private TextField txtNombreMuniAgregar;
    @FXML private TableView<Municipio> tblMunicipios;
    @FXML private TableColumn<Municipio, Integer> colIdMunicipio;
    @FXML private TableColumn<Municipio, String> colNombreMunicipio;
    @FXML private TableColumn<Municipio, String> colEstadoDeMunicipio;
    @FXML private TextField txtBuscarMunicipio;
    @FXML private ComboBox<Estado> cmbBuscarEstadoMuni;
    @FXML private Label lblTotalMunicipios;
    @FXML private ComboBox<Estado> cmbEstadoMuniEditar;
    @FXML private TextField txtNombreMuniEditar;
    @FXML private Button btnActualizarMunicipio, btnEliminarMunicipio;

    // ==========================================
    // LOCALIDADES
    // ==========================================
    @FXML private ComboBox<Estado> cmbEstadoLocAgregar;
    @FXML private ComboBox<Municipio> cmbMuniLocAgregar;
    @FXML private TextField txtNombreLocAgregar;
    @FXML private TextField txtCPLocAgregar;

    @FXML private TableView<Localidad> tblLocalidades;
    @FXML private TableColumn<Localidad, Integer> colIdLocalidad;
    @FXML private TableColumn<Localidad, String> colNombreLocalidad;
    @FXML private TableColumn<Localidad, String> colCPLocalidad;
    @FXML private TableColumn<Localidad, String> colMunicipioDeLocalidad;
    @FXML private TableColumn<Localidad, String> colEstadoDeLocalidad;
    @FXML private ComboBox<Estado> cmbBuscarEstadoLoc;
    @FXML private ComboBox<Municipio> cmbBuscarMunicipioLoc;
    @FXML private TextField txtBuscarLocalidad;
    @FXML private Label lblTotalLocalidades;

    @FXML private ComboBox<Estado> cmbEstadoLocEditar;
    @FXML private ComboBox<Municipio> cmbMuniLocEditar;
    @FXML private TextField txtNombreLocEditar;
    @FXML private TextField txtCPLocEditar;
    @FXML private Button btnActualizarLocalidad, btnEliminarLocalidad;

    @FXML private Label lblMensaje;

    private ConexionBD con;
    private EstadoDAOPsqlImp estadoDAO;
    private MunicipioDAOPsqlImp municipioDAO;
    private LocalidadDAOPsqlImp localidadDAO;

    private Estado estadoSeleccionado;
    private Municipio municipioSeleccionado;
    private Localidad localidadSeleccionada;

    private ArrayList<Municipio> todosMunicipios = new ArrayList<>();

    public UbicacionesControlador() {
        this.estadoDAO = new EstadoDAOPsqlImp();
        this.municipioDAO = new MunicipioDAOPsqlImp();
        this.localidadDAO = new LocalidadDAOPsqlImp();
    }

    public void setConexionBD(ConexionBD conexion, Connection connSql) {
        this.con = conexion;
        this.estadoDAO.setConexion(connSql);
        this.municipioDAO.setConexion(connSql);
        this.localidadDAO.setConexion(connSql);

        configurarTablas();
        configurarListenersLogica();
        cargarDatos();
    }

    @FXML
    void regresarMenu(ActionEvent event) {
        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    }

    private void configurarTablas() {
        colIdEstado.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreEstado.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        tblEstados.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            estadoSeleccionado = newSel;
            if (newSel != null) {
                txtNombreEstadoEditar.setText(newSel.getNombre());
                btnActualizarEstado.setDisable(false);
                btnEliminarEstado.setDisable(false);
            }
        });

        colIdMunicipio.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreMunicipio.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEstadoDeMunicipio.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstado().getNombre()));

        tblMunicipios.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            municipioSeleccionado = newSel;
            if (newSel != null) {
                txtNombreMuniEditar.setText(newSel.getNombre());
                for(Estado e : cmbEstadoMuniEditar.getItems()) {
                    if(e.getId() == newSel.getEstado().getId()) {
                        cmbEstadoMuniEditar.setValue(e);
                        break;
                    }
                }
                btnActualizarMunicipio.setDisable(false);
                btnEliminarMunicipio.setDisable(false);
            }
        });

        colIdLocalidad.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreLocalidad.setCellValueFactory(new PropertyValueFactory<>("localidad"));
        colCPLocalidad.setCellValueFactory(new PropertyValueFactory<>("codigoPostal"));
        colMunicipioDeLocalidad.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMunicipio().getNombre()));
        colEstadoDeLocalidad.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstado().getNombre()));

        tblLocalidades.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            localidadSeleccionada = newSel;
            if (newSel != null) {
                txtNombreLocEditar.setText(newSel.getLocalidad());
                txtCPLocEditar.setText(newSel.getCodigoPostal());

                if (newSel.getEstado() != null) {
                    for(Estado e : cmbEstadoLocEditar.getItems()) {
                        if(e.getId() == newSel.getEstado().getId()) {
                            cmbEstadoLocEditar.setValue(e);
                            break;
                        }
                    }
                }

                if (newSel.getMunicipio() != null) {
                    for(Municipio m : cmbMuniLocEditar.getItems()) {
                        if(m.getId() == newSel.getMunicipio().getId()) {
                            cmbMuniLocEditar.setValue(m);
                            break;
                        }
                    }
                }

                btnActualizarLocalidad.setDisable(false);
                btnEliminarLocalidad.setDisable(false);
            }
        });
    }

    private void configurarListenersLogica() {
        cmbEstadoLocAgregar.setOnAction(e -> filtrarMunicipios(cmbEstadoLocAgregar, cmbMuniLocAgregar));
        cmbEstadoLocEditar.setOnAction(e -> filtrarMunicipios(cmbEstadoLocEditar, cmbMuniLocEditar));

        cmbBuscarEstadoLoc.setOnAction(e -> {
            Estado est = cmbBuscarEstadoLoc.getValue();
            if(est != null) {
                ArrayList<Municipio> filtrados = todosMunicipios.stream()
                        .filter(m -> m.getEstado().getId() == est.getId())
                        .collect(Collectors.toCollection(ArrayList::new));
                cmbBuscarMunicipioLoc.setItems(FXCollections.observableArrayList(filtrados));
            } else {
                cmbBuscarMunicipioLoc.setItems(FXCollections.observableArrayList(todosMunicipios));
            }
            cmbBuscarMunicipioLoc.getSelectionModel().clearSelection();
        });
    }

    private void filtrarMunicipios(ComboBox<Estado> comboEstado, ComboBox<Municipio> comboMuni) {
        Estado est = comboEstado.getValue();
        if(est != null) {
            ArrayList<Municipio> filtrados = todosMunicipios.stream()
                    .filter(m -> m.getEstado().getId() == est.getId())
                    .collect(Collectors.toCollection(ArrayList::new));
            comboMuni.setItems(FXCollections.observableArrayList(filtrados));
            comboMuni.setDisable(false);
        } else {
            comboMuni.setItems(FXCollections.observableArrayList());
            comboMuni.setDisable(true);
        }
    }

    private void cargarDatos() {
        try {
            ArrayList<Estado> estados = estadoDAO.consultar();
            todosMunicipios = municipioDAO.consultar();
            ArrayList<Localidad> localidades = localidadDAO.consultar();

            tblEstados.setItems(FXCollections.observableArrayList(estados));
            tblMunicipios.setItems(FXCollections.observableArrayList(todosMunicipios));
            tblLocalidades.setItems(FXCollections.observableArrayList(localidades));

            actualizarContador(lblTotalEstados, estados.size());
            actualizarContador(lblTotalMunicipios, todosMunicipios.size());
            actualizarContador(lblTotalLocalidades, localidades.size());

            ObservableList<Estado> obsEstados = FXCollections.observableArrayList(estados);

            cmbEstadoMuniAgregar.setItems(obsEstados);
            cmbEstadoLocAgregar.setItems(obsEstados);
            cmbMuniLocAgregar.setDisable(true);

            cmbEstadoMuniEditar.setItems(obsEstados);
            cmbEstadoLocEditar.setItems(obsEstados);
            cmbMuniLocEditar.setDisable(true);

            cmbBuscarEstadoMuni.setItems(obsEstados);
            cmbBuscarEstadoLoc.setItems(obsEstados);
            cmbBuscarMunicipioLoc.setItems(FXCollections.observableArrayList(todosMunicipios));

        } catch(Exception e) { e.printStackTrace(); }
    }

    private void actualizarContador(Label lbl, int cantidad) {
        lbl.setText("Encontrados: " + cantidad);
    }

    private boolean existeEstado(String nombre) {
        return tblEstados.getItems().stream().anyMatch(e -> e.getNombre().equalsIgnoreCase(nombre));
    }
    private boolean existeMunicipio(String nombre, int idEstado) {
        return tblMunicipios.getItems().stream().anyMatch(m -> m.getNombre().equalsIgnoreCase(nombre) && m.getEstado().getId() == idEstado);
    }
    private boolean existeLocalidad(String nombre, int idMunicipio) {
        return tblLocalidades.getItems().stream().anyMatch(l -> l.getLocalidad().equalsIgnoreCase(nombre) && l.getMunicipio().getId() == idMunicipio);
    }

    @FXML void agregarEstado(ActionEvent e) {
        try {
            String nombre = txtNombreEstadoAgregar.getText().trim();
            if(nombre.isEmpty()) throw new RuntimeException("Escribe un nombre.");
            if(existeEstado(nombre)) throw new RuntimeException("¡El estado ya existe!");

            Estado nuevo = new Estado(0, nombre);
            estadoDAO.insertar(nuevo);
            lblMensaje.setText("Estado agregado.");
            txtNombreEstadoAgregar.clear();
            cargarDatos();
        } catch (Exception ex) { lblMensaje.setText("Error: " + ex.getMessage()); }
    }

    @FXML void actualizarEstado(ActionEvent e) {
        try {
            if(estadoSeleccionado==null) return;
            estadoSeleccionado.setNombre(txtNombreEstadoEditar.getText().trim());
            estadoDAO.actualizar(estadoSeleccionado);
            lblMensaje.setText("Estado actualizado.");
            limpiarSeleccionEstado(null);
            cargarDatos();
        } catch (Exception ex) { lblMensaje.setText("Error: " + ex.getMessage()); }
    }

    @FXML void eliminarEstado(ActionEvent e) {
        try {
            estadoDAO.borrar(estadoSeleccionado);
            lblMensaje.setText("Estado eliminado.");
            limpiarSeleccionEstado(null);
            cargarDatos();
        } catch (Exception ex) { lblMensaje.setText("Error: " + ex.getMessage()); }
    }

    @FXML void limpiarSeleccionEstado(ActionEvent e) {
        estadoSeleccionado = null;
        txtNombreEstadoEditar.clear();
        tblEstados.getSelectionModel().clearSelection();
        btnActualizarEstado.setDisable(true);
        btnEliminarEstado.setDisable(true);
    }

    @FXML void buscarEstado(ActionEvent e) {
        Estado filtro = new Estado();
        if(txtBuscarEstado.getText() != null && !txtBuscarEstado.getText().isEmpty()) {
            filtro.setNombre(txtBuscarEstado.getText());
        }
        ArrayList<Estado> res = estadoDAO.consultar(filtro);
        tblEstados.setItems(FXCollections.observableArrayList(res));
        actualizarContador(lblTotalEstados, res.size());
    }

    @FXML void cargarEstados(ActionEvent e) {
        txtBuscarEstado.clear();
        cargarDatos();
    }

    @FXML void agregarMunicipio(ActionEvent e) {
        try {
            String nombre = txtNombreMuniAgregar.getText().trim();
            Estado est = cmbEstadoMuniAgregar.getValue();
            if(est == null) throw new RuntimeException("Seleccione Estado");
            if(nombre.isEmpty()) throw new RuntimeException("Escribe un nombre.");

            if(existeMunicipio(nombre, est.getId())) throw new RuntimeException("¡El municipio ya existe!");

            Municipio m = new Municipio();
            m.setNombre(nombre);
            m.setEstado(est);
            municipioDAO.insertar(m);
            lblMensaje.setText("Municipio agregado.");
            txtNombreMuniAgregar.clear();
            cargarDatos();
        } catch (Exception ex) { lblMensaje.setText("Error: " + ex.getMessage()); }
    }

    @FXML void actualizarMunicipio(ActionEvent e) {
        try {
            municipioSeleccionado.setNombre(txtNombreMuniEditar.getText());
            municipioSeleccionado.setEstado(cmbEstadoMuniEditar.getValue());
            municipioDAO.actualizar(municipioSeleccionado);
            lblMensaje.setText("Municipio actualizado.");
            limpiarSeleccionMunicipio(null);
            cargarDatos();
        } catch (Exception ex) { lblMensaje.setText("Error: " + ex.getMessage()); }
    }

    @FXML void eliminarMunicipio(ActionEvent e) {
        try {
            municipioDAO.borrar(municipioSeleccionado);
            lblMensaje.setText("Municipio eliminado.");
            limpiarSeleccionMunicipio(null);
            cargarDatos();
        } catch (Exception ex) { lblMensaje.setText("Error: " + ex.getMessage()); }
    }

    @FXML void limpiarSeleccionMunicipio(ActionEvent e) {
        municipioSeleccionado = null;
        txtNombreMuniEditar.clear();
        cmbEstadoMuniEditar.getSelectionModel().clearSelection();
        tblMunicipios.getSelectionModel().clearSelection();
        btnActualizarMunicipio.setDisable(true);
        btnEliminarMunicipio.setDisable(true);
    }

    @FXML void buscarMunicipio(ActionEvent e) {
        Municipio filtro = new Municipio();
        if(!txtBuscarMunicipio.getText().isEmpty()) filtro.setNombre(txtBuscarMunicipio.getText());
        Estado estFiltro = cmbBuscarEstadoMuni.getValue();
        if(estFiltro != null) filtro.setEstado(estFiltro);

        ArrayList<Municipio> res = municipioDAO.consultar(filtro);
        tblMunicipios.setItems(FXCollections.observableArrayList(res));
        actualizarContador(lblTotalMunicipios, res.size());
    }

    @FXML void cargarMunicipios(ActionEvent e) {
        txtBuscarMunicipio.clear();
        cmbBuscarEstadoMuni.getSelectionModel().clearSelection();
        cargarDatos();
    }

    @FXML void agregarLocalidad(ActionEvent e) {
        try {
            String nombre = txtNombreLocAgregar.getText().trim();
            Municipio mun = cmbMuniLocAgregar.getValue();
            if(mun == null) throw new RuntimeException("Seleccione Municipio");
            if(nombre.isEmpty()) throw new RuntimeException("Escribe nombre.");

            if(existeLocalidad(nombre, mun.getId())) throw new RuntimeException("¡La localidad ya existe!");

            Localidad l = new Localidad();
            l.setLocalidad(nombre);
            l.setCodigoPostal(txtCPLocAgregar.getText());
            l.setMunicipio(mun);

            localidadDAO.insertar(l);
            lblMensaje.setText("Localidad agregada.");
            txtNombreLocAgregar.clear();
            txtCPLocAgregar.clear();
            cargarDatos();
        } catch (Exception ex) { lblMensaje.setText("Error: " + ex.getMessage()); }
    }

    @FXML void actualizarLocalidad(ActionEvent e) {
        try {
            localidadSeleccionada.setLocalidad(txtNombreLocEditar.getText());
            localidadSeleccionada.setCodigoPostal(txtCPLocEditar.getText());
            localidadSeleccionada.setMunicipio(cmbMuniLocEditar.getValue());
            localidadDAO.actualizar(localidadSeleccionada);
            lblMensaje.setText("Localidad actualizada.");
            limpiarSeleccionLocalidad(null);
            cargarDatos();
        } catch (Exception ex) { lblMensaje.setText("Error: " + ex.getMessage()); }
    }

    @FXML void eliminarLocalidad(ActionEvent e) {
        try {
            localidadDAO.borrar(localidadSeleccionada);
            lblMensaje.setText("Localidad eliminada.");
            limpiarSeleccionLocalidad(null);
            cargarDatos();
        } catch (Exception ex) { lblMensaje.setText("Error: " + ex.getMessage()); }
    }

    @FXML void limpiarSeleccionLocalidad(ActionEvent e) {
        localidadSeleccionada = null;
        txtNombreLocEditar.clear();
        txtCPLocEditar.clear();
        cmbEstadoLocEditar.getSelectionModel().clearSelection();
        cmbMuniLocEditar.getSelectionModel().clearSelection();
        tblLocalidades.getSelectionModel().clearSelection();
        btnActualizarLocalidad.setDisable(true);
        btnEliminarLocalidad.setDisable(true);
    }

    @FXML void buscarLocalidad(ActionEvent e) {
        Localidad filtro = new Localidad();
        if(!txtBuscarLocalidad.getText().isEmpty()) filtro.setLocalidad(txtBuscarLocalidad.getText());

        Municipio munFiltro = cmbBuscarMunicipioLoc.getValue();
        if(munFiltro != null) filtro.setMunicipio(munFiltro);

        Estado estFiltro = cmbBuscarEstadoLoc.getValue();
        if(estFiltro != null) filtro.setEstado(estFiltro);

        ArrayList<Localidad> res = localidadDAO.consultar(filtro);
        tblLocalidades.setItems(FXCollections.observableArrayList(res));
        actualizarContador(lblTotalLocalidades, res.size());
    }

    @FXML void cargarLocalidades(ActionEvent e) {
        txtBuscarLocalidad.clear();
        cmbBuscarEstadoLoc.getSelectionModel().clearSelection();
        cmbBuscarMunicipioLoc.getSelectionModel().clearSelection();
        cmbBuscarMunicipioLoc.setItems(FXCollections.observableArrayList(todosMunicipios));
        cargarDatos();
    }
}