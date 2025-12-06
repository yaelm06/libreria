package mx.uaemex.fi.bases.libreria.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import mx.uaemex.fi.bases.libreria.modelo.*;
import mx.uaemex.fi.bases.libreria.modelo.data.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class VentasControlador {

    // --- PESTAÑA 1: POS ---
    @FXML private TextField txtBuscarLibro;
    @FXML private TextField txtCantidadAgregar;
    @FXML private TableView<Libro> tblCatalogoLibros;
    @FXML private TableColumn<Libro, String> colCatTitulo, colCatAutor;
    @FXML private TableColumn<Libro, Double> colCatPrecio;
    @FXML private TableColumn<Libro, Integer> colCatAnio;

    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private ComboBox<MetodoPago> cmbMetodoPago;
    @FXML private Label lblEmpleado;

    @FXML private TableView<DetalleVenta> tblCarrito;
    @FXML private TableColumn<DetalleVenta, String> colCarTitulo;
    @FXML private TableColumn<DetalleVenta, Integer> colCarCantidad;
    @FXML private TableColumn<DetalleVenta, Double> colCarPrecio, colCarImporte;

    @FXML private Label lblTotalVenta;
    @FXML private Label lblMensaje;

    // --- PESTAÑA 2: HISTORIAL ---
    @FXML private TextField txtHistIdVenta, txtHistCliente, txtHistEmpleado;
    @FXML private TextField txtHistLibro; // NUEVO CAMPO
    @FXML private ComboBox<String> cmbHistMetodo;
    @FXML private DatePicker dpFechaInicio, dpFechaFin;
    @FXML private TextField txtHistMinTotal, txtHistMaxTotal;
    @FXML private Label lblTotalVentas;

    @FXML private TableView<Venta> tblHistorialVentas;
    @FXML private TableColumn<Venta, Integer> colHistId;
    @FXML private TableColumn<Venta, String> colHistFecha, colHistCliente, colHistEmpleado, colHistMetodo, colHistEstado;
    @FXML private TableColumn<Venta, Double> colHistTotal;

    // --- DAOs y Modelos ---
    private ObservableList<DetalleVenta> listaCarrito = FXCollections.observableArrayList();
    private ConexionBD con;

    private LibroDAOPsqlImp libroDAO = new LibroDAOPsqlImp();
    private ClientesDAO clientesDAO = new ClientesDAOPsqlImp();
    private MetodoPagoDAO metodoPagoDAO = new MetodoPagoDAOPsqlImp();
    private VentaDAOPsqlImp ventaDAO = new VentaDAOPsqlImp();

    private Empleado empleadoLogueado;

    public void initData(ConexionBD conexion, Connection connSql, Empleado empleado) {
        this.con = conexion;
        this.empleadoLogueado = empleado;

        libroDAO.setConexion(connSql);
        ((ClientesDAOPsqlImp)clientesDAO).setConexion(connSql);
        ((MetodoPagoDAOPsqlImp)metodoPagoDAO).setConexion(connSql);
        ventaDAO.setConexion(connSql);

        lblEmpleado.setText("Cajero: " + empleado.getNombre());

        configurarTablasPOS();
        configurarTablasHistorial();
        cargarDatosIniciales();
    }

    private void configurarTablasPOS() {
        colCatTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colCatAutor.setCellValueFactory(new PropertyValueFactory<>("autoresTexto"));
        colCatPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colCatAnio.setCellValueFactory(new PropertyValueFactory<>("anio"));

        colCarTitulo.setCellValueFactory(new PropertyValueFactory<>("tituloLibro"));
        colCarCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCarPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colCarImporte.setCellValueFactory(new PropertyValueFactory<>("importe"));

        tblCarrito.setItems(listaCarrito);
    }

    private void configurarTablasHistorial() {
        colHistId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colHistFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHistTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        colHistCliente.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getCliente().getNombre() + " " + cell.getValue().getCliente().getApellidoPaterno()));

        colHistEmpleado.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getEmpleado().getNombre()));

        colHistMetodo.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));

        colHistEstado.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().isActivo() ? "Completada" : "Cancelada"));
    }

    private void cargarDatosIniciales() {
        try {
            ArrayList<MetodoPago> metodos = metodoPagoDAO.consultar();
            cmbMetodoPago.setItems(FXCollections.observableArrayList(metodos));

            cmbHistMetodo.getItems().add("Todos");
            for(MetodoPago mp : metodos) cmbHistMetodo.getItems().add(mp.getTipo());

            cmbCliente.setItems(FXCollections.observableArrayList(clientesDAO.consultar()));
            if(!cmbCliente.getItems().isEmpty()) cmbCliente.getSelectionModel().selectFirst();

        } catch (Exception e) { e.printStackTrace(); }

        recargarLibros(null);
        buscarHistorialVentas(null);
    }

    // --- LÓGICA POS ---
    @FXML void buscarLibros(ActionEvent e) {
        Libro filtro = new Libro();
        filtro.setTitulo(txtBuscarLibro.getText());
        tblCatalogoLibros.setItems(FXCollections.observableArrayList(libroDAO.consultar(filtro)));
    }

    @FXML void recargarLibros(ActionEvent e) {
        txtBuscarLibro.clear();
        tblCatalogoLibros.setItems(FXCollections.observableArrayList(libroDAO.consultar()));
    }

    @FXML void agregarAlCarrito(ActionEvent e) {
        Libro l = tblCatalogoLibros.getSelectionModel().getSelectedItem();
        if (l == null) {
            mostrarMensaje("Seleccione un libro.", false);
            return;
        }

        int cantidad = 1;
        try {
            cantidad = Integer.parseInt(txtCantidadAgregar.getText());
            if(cantidad <= 0) throw new NumberFormatException();
        } catch(Exception ex) {
            mostrarMensaje("Cantidad inválida.", false);
            return;
        }

        boolean encontrado = false;
        for (DetalleVenta d : listaCarrito) {
            if (d.getLibro().getId() == l.getId()) {
                d.setCantidad(d.getCantidad() + cantidad);
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            listaCarrito.add(new DetalleVenta(l, cantidad));
        }

        tblCarrito.refresh();
        calcularTotalVisual();
        lblMensaje.setText("");
        txtCantidadAgregar.setText("1");
    }

    @FXML void quitarDelCarrito(ActionEvent e) {
        DetalleVenta d = tblCarrito.getSelectionModel().getSelectedItem();
        if (d != null) {
            listaCarrito.remove(d);
            calcularTotalVisual();
        }
    }

    @FXML void vaciarCarrito(ActionEvent e) {
        listaCarrito.clear();
        calcularTotalVisual();
    }

    private void calcularTotalVisual() {
        double total = 0;
        for (DetalleVenta d : listaCarrito) total += d.getSubtotal();
        lblTotalVenta.setText(String.format("$%.2f", total));
    }

    @FXML void realizarVenta(ActionEvent e) {
        if (listaCarrito.isEmpty()) { mostrarMensaje("Carrito vacío.", false); return; }
        if (cmbCliente.getValue() == null || cmbMetodoPago.getValue() == null) { mostrarMensaje("Faltan datos.", false); return; }

        Connection conn = null;
        try {
            conn = con.obtenerConexion();
            conn.setAutoCommit(false);

            String sqlVenta = "INSERT INTO ventas.tventa (id_cliente, id_empleado, id_metodo_pago, total, activo) VALUES (?, ?, ?, 0, true) RETURNING id_venta";
            PreparedStatement pstVenta = conn.prepareStatement(sqlVenta);
            pstVenta.setInt(1, cmbCliente.getValue().getId());
            pstVenta.setInt(2, empleadoLogueado.getId());
            pstVenta.setInt(3, cmbMetodoPago.getValue().getId());

            ResultSet rs = pstVenta.executeQuery();
            int idVenta = 0;
            if (rs.next()) idVenta = rs.getInt(1);
            pstVenta.close();

            String sqlDetalle = "INSERT INTO ventas.tdetalleVenta (id_venta, id_libro, cantidad, precioUnitario) VALUES (?, ?, ?, ?)";
            PreparedStatement pstDetalle = conn.prepareStatement(sqlDetalle);

            for (DetalleVenta d : listaCarrito) {
                pstDetalle.setInt(1, idVenta);
                pstDetalle.setInt(2, d.getLibro().getId());
                pstDetalle.setInt(3, d.getCantidad());
                pstDetalle.setDouble(4, d.getPrecioUnitario());
                pstDetalle.addBatch();
            }
            pstDetalle.executeBatch();
            pstDetalle.close();

            conn.commit();
            mostrarMensaje("Venta #" + idVenta + " registrada.", true);
            vaciarCarrito(null);

            buscarHistorialVentas(null);

        } catch (SQLException ex) {
            try { if(conn!=null) conn.rollback(); } catch(Exception roll){}
            mostrarMensaje("Error: " + ex.getMessage(), false);
        } finally {
            try { if(conn!=null) conn.setAutoCommit(true); } catch(Exception end){}
        }
    }

    // --- LÓGICA HISTORIAL ---
    @FXML void buscarHistorialVentas(ActionEvent e) {
        Venta filtro = new Venta();
        try {
            if(!txtHistIdVenta.getText().isEmpty()) filtro.setId(Integer.parseInt(txtHistIdVenta.getText()));
        } catch(Exception ex){}

        filtro.getCliente().setNombre(txtHistCliente.getText());
        filtro.getEmpleado().setNombre(txtHistEmpleado.getText());

        if(cmbHistMetodo.getValue() != null && !cmbHistMetodo.getValue().equals("Todos")) {
            filtro.setMetodoPago(cmbHistMetodo.getValue());
        }

        Double minT = null, maxT = null;
        Date fechaIni = null, fechaFin = null;
        String filtroLibro = txtHistLibro.getText(); // NUEVO: LEER CAMPO LIBRO

        try {
            if(!txtHistMinTotal.getText().isEmpty()) minT = Double.parseDouble(txtHistMinTotal.getText());
            if(!txtHistMaxTotal.getText().isEmpty()) maxT = Double.parseDouble(txtHistMaxTotal.getText());
            if(dpFechaInicio.getValue() != null) fechaIni = Date.valueOf(dpFechaInicio.getValue());
            if(dpFechaFin.getValue() != null) fechaFin = Date.valueOf(dpFechaFin.getValue());
        } catch(Exception ex) {}

        // Pasamos el nuevo filtroLibro al DAO
        ArrayList<Venta> resultados = ventaDAO.consultar(filtro, minT, maxT, fechaIni, fechaFin, filtroLibro);
        tblHistorialVentas.setItems(FXCollections.observableArrayList(resultados));
        lblTotalVentas.setText("Encontradas: " + resultados.size());
    }

    @FXML void limpiarHistorial(ActionEvent e) {
        txtHistIdVenta.clear();
        txtHistCliente.clear();
        txtHistEmpleado.clear();
        txtHistLibro.clear();
        cmbHistMetodo.getSelectionModel().selectFirst();
        txtHistMinTotal.clear();
        txtHistMaxTotal.clear();
        dpFechaInicio.setValue(null);
        dpFechaFin.setValue(null);
        buscarHistorialVentas(null);
    }

    // NUEVO: Ver detalles (ticket)
    @FXML void verDetallesVenta(ActionEvent e) {
        Venta v = tblHistorialVentas.getSelectionModel().getSelectedItem();
        if (v == null) {
            mostrarMensaje("Seleccione una venta para ver el detalle.", false);
            return;
        }

        try {
            ArrayList<DetalleVenta> detalles = ventaDAO.consultarDetalle(v.getId());
            StringBuilder sb = new StringBuilder();
            sb.append("Venta #").append(v.getId()).append("\n");
            sb.append("Cliente: ").append(v.getCliente().getNombre()).append(" ").append(v.getCliente().getApellidoPaterno()).append("\n");
            sb.append("------------------------------------------------\n");
            sb.append(String.format("%-30s %-5s %-10s\n", "Producto", "Cant", "Subtotal"));
            sb.append("------------------------------------------------\n");

            for(DetalleVenta d : detalles) {
                sb.append(String.format("%-30s %-5d $%-10.2f\n",
                        d.getLibro().getTitulo().length() > 25 ? d.getLibro().getTitulo().substring(0, 25)+"..." : d.getLibro().getTitulo(),
                        d.getCantidad(),
                        d.getSubtotal()));
            }
            sb.append("------------------------------------------------\n");
            sb.append("TOTAL: $").append(v.getTotal());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Detalle de Venta");
            alert.setHeaderText(null);
            alert.setContentText(sb.toString());
            // Truco para usar fuente monoespaciada en el alert si se pudiera, pero default está bien
            alert.getDialogPane().setMinWidth(400);
            alert.showAndWait();

        } catch(Exception ex) {
            mostrarMensaje("Error al cargar detalles: " + ex.getMessage(), false);
        }
    }

    @FXML void cancelarVentaHistorial(ActionEvent e) {
        Venta v = tblHistorialVentas.getSelectionModel().getSelectedItem();
        if(v != null) {
            try {
                ventaDAO.cancelarVenta(v.getId());
                buscarHistorialVentas(null);
                mostrarMensaje("Venta #" + v.getId() + " cancelada.", true);
            } catch(Exception ex) {
                mostrarMensaje("Error al cancelar: " + ex.getMessage(), false);
            }
        }
    }

    @FXML void regresarMenu(ActionEvent event) {
        ((Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow()).close();
    }

    private void mostrarMensaje(String msg, boolean exito) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle("-fx-text-fill: " + (exito ? "#27ae60" : "#c0392b") + "; -fx-font-weight: bold; -fx-font-size: 14px;");
    }
}