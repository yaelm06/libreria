package mx.uaemex.fi.bases.libreria.controlador;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import mx.uaemex.fi.bases.libreria.modelo.*;
import mx.uaemex.fi.bases.libreria.modelo.data.Cliente;

import java.sql.Connection;
import java.util.ArrayList;

public class ClientesControlador {

    @FXML private TextField txtNombre, txtPaterno, txtMaterno, txtTelefono;
    @FXML private CheckBox chkActivo;
    @FXML private Label lblModo;
    @FXML private Button btnEliminar;

    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, Integer> colId;
    @FXML private TableColumn<Cliente, String> colNombre, colPaterno, colMaterno, colTelefono, colActivo;

    @FXML private TextField txtBuscarNombre, txtBuscarPaterno, txtBuscarMaterno, txtBuscarTelefono;
    @FXML private ComboBox<String> cmbBuscarEstatus;
    @FXML private Label lblTotal, lblMensaje;

    private ConexionBD con;
    private ClientesDAOPsqlImp clientesDAO;
    private Cliente clienteSeleccionado = null;

    public ClientesControlador() {
        this.clientesDAO = new ClientesDAOPsqlImp();
    }

    public void setConexionBD(ConexionBD conexion, Connection connSql) {
        this.con = conexion;
        this.clientesDAO.setConexion(connSql);
        configurarTabla();
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
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        colActivo.setCellValueFactory(cell ->
                new SimpleStringProperty(Boolean.TRUE.equals(cell.getValue().isActivo()) ? "Sí" : "No"));

        tblClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) cargarClienteEnFormulario(newSel);
        });
    }

    private void cargarDatosIniciales() {
        cmbBuscarEstatus.setItems(FXCollections.observableArrayList("Activos", "Inactivos", "Todos"));
        cargarClientes(null);
    }

    @FXML
    void cargarClientes(ActionEvent e) {
        txtBuscarNombre.clear(); txtBuscarPaterno.clear(); txtBuscarMaterno.clear(); txtBuscarTelefono.clear();
        cmbBuscarEstatus.getSelectionModel().select("Todos");
        buscarCliente(null);
    }

    @FXML
    void buscarCliente(ActionEvent e) {
        Cliente filtro = new Cliente();
        if(!txtBuscarNombre.getText().isEmpty()) filtro.setNombre(txtBuscarNombre.getText());
        if(!txtBuscarPaterno.getText().isEmpty()) filtro.setApellidoPaterno(txtBuscarPaterno.getText());
        if(!txtBuscarMaterno.getText().isEmpty()) filtro.setApellidoMaterno(txtBuscarMaterno.getText());
        if(!txtBuscarTelefono.getText().isEmpty()) filtro.setTelefono(txtBuscarTelefono.getText());

        String estatus = cmbBuscarEstatus.getValue();
        if ("Activos".equals(estatus)) filtro.setActivo(true);
        else if ("Inactivos".equals(estatus)) filtro.setActivo(false);
        else filtro.setActivo(null);

        ArrayList<Cliente> res = clientesDAO.consultar(filtro);
        tblClientes.setItems(FXCollections.observableArrayList(res));
        lblTotal.setText("Registros: " + res.size());
    }

    @FXML
    void nuevoCliente(ActionEvent e) {
        limpiarFormulario();
        lblModo.setText("(Nuevo)");
        lblModo.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        btnEliminar.setVisible(false);
        tblClientes.getSelectionModel().clearSelection();
        clienteSeleccionado = null;
        lblMensaje.setText("");
    }

    @FXML
    void guardarCliente(ActionEvent e) {
        try {
            if(txtNombre.getText().trim().isEmpty() || txtPaterno.getText().trim().isEmpty())
                throw new RuntimeException("Nombre y Apellido Paterno son obligatorios.");

            String mensajeExito = "";

            if (clienteSeleccionado == null) {
                Cliente busqueda = new Cliente();
                busqueda.setNombre(txtNombre.getText());
                busqueda.setApellidoPaterno(txtPaterno.getText());
                busqueda.setActivo(null);
                ArrayList<Cliente> existentes = clientesDAO.consultar(busqueda);

                if (!existentes.isEmpty()) {
                    for(Cliente c : existentes) {
                        String matExistente = c.getApellidoMaterno() == null ? "" : c.getApellidoMaterno();
                        String matNuevo = txtMaterno.getText() == null ? "" : txtMaterno.getText();

                        if(matExistente.equalsIgnoreCase(matNuevo)) {
                            if (Boolean.FALSE.equals(c.isActivo())) {
                                leerDatos(c);
                                c.setActivo(true);
                                clientesDAO.actualizar(c);
                                mensajeExito = "Cliente reactivado correctamente.";
                                nuevoCliente(null);
                                buscarCliente(null);
                                mostrarMensaje(mensajeExito, true);
                                return;
                            } else {
                                throw new RuntimeException("El cliente ya existe.");
                            }
                        }
                    }
                }

                Cliente nuevo = new Cliente();
                leerDatos(nuevo);
                clientesDAO.insertar(nuevo);
                mensajeExito = "Cliente registrado exitosamente.";

            } else {
                leerDatos(clienteSeleccionado);
                clientesDAO.actualizar(clienteSeleccionado);
                mensajeExito = "Cliente actualizado correctamente.";
            }

            nuevoCliente(null);
            buscarCliente(null);
            mostrarMensaje(mensajeExito, true);

        } catch (Exception ex) {
            mostrarMensaje("Error: " + ex.getMessage(), false);
        }
    }

    private void leerDatos(Cliente c) {
        c.setNombre(txtNombre.getText());
        c.setApellidoPaterno(txtPaterno.getText());
        c.setApellidoMaterno(txtMaterno.getText());
        c.setTelefono(txtTelefono.getText());
        c.setActivo(chkActivo.isSelected());
    }

    private void cargarClienteEnFormulario(Cliente c) {
        clienteSeleccionado = c;
        lblModo.setText("(Editando ID: " + c.getId() + ")");
        lblModo.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        txtNombre.setText(c.getNombre());
        txtPaterno.setText(c.getApellidoPaterno());
        txtMaterno.setText(c.getApellidoMaterno());
        txtTelefono.setText(c.getTelefono());
        chkActivo.setSelected(Boolean.TRUE.equals(c.isActivo()));

        btnEliminar.setVisible(true);
        btnEliminar.setText("Eliminar / Dar de Baja");

        if (Boolean.FALSE.equals(c.isActivo())) {
            btnEliminar.setText("Reactivar");
            btnEliminar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        } else {
            btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        }
    }

    @FXML
    void eliminarOReactivar(ActionEvent e) {
        try {
            if(clienteSeleccionado == null) return;

            String mensajeExito = "";

            if (Boolean.TRUE.equals(clienteSeleccionado.isActivo())) {
                clientesDAO.borrar(clienteSeleccionado);
                mensajeExito = "Cliente eliminado o dado de baja correctamente.";
            } else {
                clienteSeleccionado.setActivo(true);
                clientesDAO.actualizar(clienteSeleccionado);
                mensajeExito = "Cliente reactivado correctamente.";
            }

            nuevoCliente(null);
            buscarCliente(null);
            mostrarMensaje(mensajeExito, true);

        } catch (Exception ex) {
            mostrarMensaje("Error: " + ex.getMessage(), false);
        }
    }

    private void limpiarFormulario() {
        txtNombre.clear(); txtPaterno.clear(); txtMaterno.clear(); txtTelefono.clear();
        chkActivo.setSelected(true);
    }

    private void mostrarMensaje(String mensaje, boolean exito) {
        lblMensaje.setText(mensaje);
        if (exito) {
            lblMensaje.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 13px;");
        } else {
            lblMensaje.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold; -fx-font-size: 13px;");
        }
    }
}