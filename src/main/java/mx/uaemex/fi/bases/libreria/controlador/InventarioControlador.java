package mx.uaemex.fi.bases.libreria.controlador;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import mx.uaemex.fi.bases.libreria.modelo.*;
import mx.uaemex.fi.bases.libreria.modelo.data.Autor;
import mx.uaemex.fi.bases.libreria.modelo.data.Editorial;
import mx.uaemex.fi.bases.libreria.modelo.data.Libro;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class InventarioControlador {

    // --- CONTROL DE PESTAÑAS ---
    @FXML private TabPane tabPanePrincipal;
    @FXML private Tab tabLibros, tabAutores, tabEditoriales;

    // --- LIBROS: BÚSQUEDA ---
    @FXML private TextField txtBuscarTitulo, txtBuscarISBN, txtBuscarAutorLibro;
    @FXML private ComboBox<Editorial> cmbBuscarEditorial;

    @FXML private CheckBox chkRangoPrecio;
    @FXML private TextField txtBuscarPrecioExacto;
    @FXML private HBox boxRangoPrecio;
    @FXML private TextField txtBuscarPrecioMin, txtBuscarPrecioMax;

    @FXML private CheckBox chkRangoAnio;
    @FXML private TextField txtBuscarAnioExacto;
    @FXML private HBox boxRangoAnio;
    @FXML private TextField txtBuscarAnioMin, txtBuscarAnioMax;

    // --- LIBROS: TABLA Y FORMULARIO ---
    @FXML private TableView<Libro> tblLibros;
    @FXML private TableColumn<Libro, Integer> colLibroId, colLibroAnio;
    @FXML private TableColumn<Libro, String> colLibroTitulo, colLibroISBN, colLibroAutor, colLibroEditorial;
    @FXML private TableColumn<Libro, Double> colLibroPrecio;
    @FXML private TableColumn<Libro, Boolean> colLibroActivo;

    @FXML private Label lblModoLibro; // NUEVO
    @FXML private TextField txtLibroTitulo, txtLibroISBN, txtLibroPrecio, txtLibroAnio;
    @FXML private ComboBox<Editorial> cmbLibroEditorial;

    @FXML private ListView<Autor> listAutoresDisponibles;
    @FXML private TextField txtFiltroAutorForm;
    private FilteredList<Autor> listaAutoresFiltrada;

    @FXML private CheckBox chkLibroActivo;
    @FXML private Button btnEliminarLibro;
    @FXML private Label lblMensajeLibro;
    private Libro libroSeleccionado;

    // --- AUTORES ---
    @FXML private TextField txtBuscarAutorNombre, txtBuscarAutorPaterno, txtBuscarAutorMaterno;
    @FXML private TableView<Autor> tblAutores;
    @FXML private TableColumn<Autor, Integer> colAutorId;
    @FXML private TableColumn<Autor, String> colAutorNombre, colAutorPaterno, colAutorMaterno;
    @FXML private TextField txtAutorNombre, txtAutorPaterno, txtAutorMaterno;
    @FXML private Label lblModoAutor; // NUEVO
    @FXML private Label lblMensajeAutor;
    @FXML private Button btnEliminarAutor; // NUEVO
    private Autor autorSeleccionado;

    // --- EDITORIALES ---
    @FXML private TextField txtBuscarEdiNombre, txtBuscarEdiPais;
    @FXML private TableView<Editorial> tblEditoriales;
    @FXML private TableColumn<Editorial, Integer> colEditorialId;
    @FXML private TableColumn<Editorial, String> colEditorialNombre, colEditorialPais;
    @FXML private TextField txtEditorialNombre, txtEditorialPais;
    @FXML private Label lblModoEditorial; // NUEVO
    @FXML private Label lblMensajeEditorial;
    @FXML private Button btnEliminarEditorial; // NUEVO
    private Editorial editorialSeleccionada;

    // --- DAOs ---
    private ConexionBD con;
    private LibroDAOPsqlImp libroDAO = new LibroDAOPsqlImp();
    private AutorDAOPsqlImp autorDAO = new AutorDAOPsqlImp();
    private EditorialDAOPsqlImp editorialDAO = new EditorialDAOPsqlImp();

    public void setConexionBD(ConexionBD conexion, Connection connSql) {
        this.con = conexion;
        this.libroDAO.setConexion(connSql);
        this.autorDAO.setConexion(connSql);
        this.editorialDAO.setConexion(connSql);

        configurarTablas();
        cargarDatosGenerales();
        configurarFiltroLocalAutores();
    }

    @FXML void regresarMenu(ActionEvent event) {
        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    }

    private void configurarTablas() {
        // --- LIBROS ---
        colLibroId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colLibroTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colLibroISBN.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colLibroPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colLibroAnio.setCellValueFactory(new PropertyValueFactory<>("anio"));
        colLibroActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colLibroEditorial.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getEditorial().getEditorial()));
        colLibroAutor.setCellValueFactory(new PropertyValueFactory<>("autoresTexto"));

        tblLibros.getSelectionModel().selectedItemProperty().addListener((obs, old, nev) -> cargarLibroEnFormulario(nev));
        listAutoresDisponibles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // --- AUTORES ---
        colAutorId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAutorNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colAutorPaterno.setCellValueFactory(new PropertyValueFactory<>("apellidoPaterno"));
        colAutorMaterno.setCellValueFactory(new PropertyValueFactory<>("apellidoMaterno"));
        tblAutores.getSelectionModel().selectedItemProperty().addListener((obs, old, nev) -> {
            autorSeleccionado = nev;
            if(nev!=null) {
                txtAutorNombre.setText(nev.getNombre());
                txtAutorPaterno.setText(nev.getApellidoPaterno());
                txtAutorMaterno.setText(nev.getApellidoMaterno());
                lblMensajeAutor.setText("");

                // MODO EDICIÓN
                lblModoAutor.setText("(Editando ID: " + nev.getId() + ")");
                lblModoAutor.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;"); // Naranja
                btnEliminarAutor.setVisible(true);
            }
        });

        // --- EDITORIALES ---
        colEditorialId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEditorialNombre.setCellValueFactory(new PropertyValueFactory<>("editorial"));
        colEditorialPais.setCellValueFactory(new PropertyValueFactory<>("pais"));
        tblEditoriales.getSelectionModel().selectedItemProperty().addListener((obs, old, nev) -> {
            editorialSeleccionada = nev;
            if(nev!=null) {
                txtEditorialNombre.setText(nev.getEditorial());
                txtEditorialPais.setText(nev.getPais());
                lblMensajeEditorial.setText("");

                // MODO EDICIÓN
                lblModoEditorial.setText("(Editando ID: " + nev.getId() + ")");
                lblModoEditorial.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;"); // Naranja
                btnEliminarEditorial.setVisible(true);
            }
        });
    }

    private void cargarDatosGenerales() {
        ArrayList<Editorial> editoriales = editorialDAO.consultar();
        cmbLibroEditorial.setItems(FXCollections.observableArrayList(editoriales));
        cmbBuscarEditorial.setItems(FXCollections.observableArrayList(editoriales));

        ArrayList<Autor> autores = autorDAO.consultar();
        listaAutoresFiltrada = new FilteredList<>(FXCollections.observableArrayList(autores), p -> true);
        listAutoresDisponibles.setItems(listaAutoresFiltrada);

        cargarLibros(null);
        recargarAutores(null);
        recargarEditoriales(null);
    }

    private void configurarFiltroLocalAutores() {
        txtFiltroAutorForm.textProperty().addListener((observable, oldValue, newValue) -> {
            listaAutoresFiltrada.setPredicate(autor -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                String nombreCompleto = (autor.getNombre() + " " + autor.getApellidoPaterno() + " " + autor.getApellidoMaterno()).toLowerCase();
                return nombreCompleto.contains(lowerCaseFilter);
            });
        });
    }

    // === UTILIDAD DE MENSAJES ===
    private void mostrarMensaje(Label label, String mensaje, boolean exito) {
        label.setText(mensaje);
        if (exito) {
            label.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 13px;"); // Verde éxito
        } else {
            label.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold; -fx-font-size: 13px;"); // Rojo error
        }
    }

    @FXML
    void irAGestionAutores(ActionEvent event) {
        tabPanePrincipal.getSelectionModel().select(tabAutores);
        nuevoAutor(null);
    }

    @FXML
    void irAGestionEditoriales(ActionEvent event) {
        tabPanePrincipal.getSelectionModel().select(tabEditoriales);
        nuevaEditorial(null);
    }

    // === TOGGLES ===
    @FXML void toggleRangoPrecio(ActionEvent e) {
        boolean esRango = chkRangoPrecio.isSelected();
        txtBuscarPrecioExacto.setVisible(!esRango);
        boxRangoPrecio.setVisible(esRango);
    }

    @FXML void toggleRangoAnio(ActionEvent e) {
        boolean esRango = chkRangoAnio.isSelected();
        txtBuscarAnioExacto.setVisible(!esRango);
        boxRangoAnio.setVisible(esRango);
    }

    // === GESTIÓN LIBROS ===
    @FXML void cargarLibros(ActionEvent e) {
        txtBuscarTitulo.clear(); txtBuscarISBN.clear(); txtBuscarAutorLibro.clear();
        cmbBuscarEditorial.getSelectionModel().clearSelection();
        cmbBuscarEditorial.setValue(null);

        chkRangoPrecio.setSelected(false); toggleRangoPrecio(null);
        chkRangoAnio.setSelected(false); toggleRangoAnio(null);
        txtBuscarPrecioExacto.clear(); txtBuscarPrecioMin.clear(); txtBuscarPrecioMax.clear();
        txtBuscarAnioExacto.clear(); txtBuscarAnioMin.clear(); txtBuscarAnioMax.clear();

        tblLibros.setItems(FXCollections.observableArrayList(libroDAO.consultar()));
    }

    @FXML void buscarLibro(ActionEvent e) {
        Libro filtro = new Libro();
        if(!txtBuscarTitulo.getText().isEmpty()) filtro.setTitulo(txtBuscarTitulo.getText());
        if(!txtBuscarISBN.getText().isEmpty()) filtro.setIsbn(txtBuscarISBN.getText());

        if(cmbBuscarEditorial.getValue() != null) filtro.setEditorial(cmbBuscarEditorial.getValue());

        String filtroAutor = txtBuscarAutorLibro.getText();

        Double minP = null, maxP = null;
        Integer minA = null, maxA = null;

        if (chkRangoPrecio.isSelected()) {
            minP = parseDoubleOrNull(txtBuscarPrecioMin.getText());
            maxP = parseDoubleOrNull(txtBuscarPrecioMax.getText());
        } else {
            Double val = parseDoubleOrNull(txtBuscarPrecioExacto.getText());
            if(val != null) { minP = val; maxP = val; }
        }

        if (chkRangoAnio.isSelected()) {
            minA = parseIntOrNull(txtBuscarAnioMin.getText());
            maxA = parseIntOrNull(txtBuscarAnioMax.getText());
        } else {
            Integer val = parseIntOrNull(txtBuscarAnioExacto.getText());
            if(val != null) { minA = val; maxA = val; }
        }

        tblLibros.setItems(FXCollections.observableArrayList(libroDAO.consultar(filtro, minP, maxP, minA, maxA, filtroAutor)));
    }

    private Double parseDoubleOrNull(String txt) {
        try { return (txt != null && !txt.isEmpty()) ? Double.parseDouble(txt) : null; } catch (Exception e) { return null; }
    }
    private Integer parseIntOrNull(String txt) {
        try { return (txt != null && !txt.isEmpty()) ? Integer.parseInt(txt) : null; } catch (Exception e) { return null; }
    }

    private void cargarLibroEnFormulario(Libro l) {
        lblMensajeLibro.setText(""); // Limpiar mensajes anteriores
        libroSeleccionado = l;
        if (l != null) {
            // MODO EDICIÓN
            lblModoLibro.setText("(Editando ID: " + l.getId() + ")");
            lblModoLibro.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;"); // Naranja

            txtLibroTitulo.setText(l.getTitulo());
            txtLibroISBN.setText(l.getIsbn());
            txtLibroPrecio.setText(String.valueOf(l.getPrecio()));
            txtLibroAnio.setText(String.valueOf(l.getAnio()));
            chkLibroActivo.setSelected(l.isActivo());

            for(Editorial ed : cmbLibroEditorial.getItems()) {
                if(ed.getId() == l.getEditorial().getId()) { cmbLibroEditorial.setValue(ed); break; }
            }

            txtFiltroAutorForm.clear();
            listAutoresDisponibles.getSelectionModel().clearSelection();
            List<Autor> autoresReales = libroDAO.obtenerAutoresPorLibro(l.getId());
            for(Autor aReal : autoresReales) {
                for(Autor aLista : listAutoresDisponibles.getItems()) {
                    if(aReal.getId() == aLista.getId()) {
                        listAutoresDisponibles.getSelectionModel().select(aLista);
                    }
                }
            }
            btnEliminarLibro.setVisible(true);
        }
    }

    @FXML void nuevoLibro(ActionEvent e) {
        libroSeleccionado = null;

        // MODO NUEVO
        lblModoLibro.setText("(Nuevo)");
        lblModoLibro.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Verde

        txtLibroTitulo.clear(); txtLibroISBN.clear(); txtLibroPrecio.clear(); txtLibroAnio.clear();
        cmbLibroEditorial.getSelectionModel().clearSelection();
        listAutoresDisponibles.getSelectionModel().clearSelection();
        txtFiltroAutorForm.clear();
        chkLibroActivo.setSelected(true);
        btnEliminarLibro.setVisible(false);
        lblMensajeLibro.setText("");
        tblLibros.getSelectionModel().clearSelection();
    }

    @FXML void guardarLibro(ActionEvent e) {
        try {
            if(cmbLibroEditorial.getValue() == null) throw new RuntimeException("Seleccione Editorial");
            List<Autor> autoresSeleccionados = listAutoresDisponibles.getSelectionModel().getSelectedItems();
            if(autoresSeleccionados.isEmpty()) throw new RuntimeException("Seleccione al menos un Autor (Ctrl+Clic)");

            Libro l = (libroSeleccionado != null) ? libroSeleccionado : new Libro();
            l.setTitulo(txtLibroTitulo.getText());
            l.setIsbn(txtLibroISBN.getText());
            l.setPrecio(Double.parseDouble(txtLibroPrecio.getText()));
            l.setAnio(Integer.parseInt(txtLibroAnio.getText()));
            l.setEditorial(cmbLibroEditorial.getValue());
            l.setAutores(new ArrayList<>(autoresSeleccionados));
            l.setActivo(chkLibroActivo.isSelected());

            boolean esNuevo = (libroSeleccionado == null);

            if(esNuevo) {
                libroDAO.insertar(l);
            } else {
                libroDAO.actualizar(l);
            }

            cargarLibros(null); // Recargar tabla
            nuevoLibro(null);   // Limpiar formulario y poner modo nuevo

            // IMPORTANTE: Mostrar mensaje DESPUÉS de limpiar (porque nuevoLibro borra el mensaje)
            mostrarMensaje(lblMensajeLibro, esNuevo ? "Libro registrado exitosamente." : "Libro actualizado exitosamente.", true);

        } catch (Exception ex) {
            mostrarMensaje(lblMensajeLibro, "Error: " + ex.getMessage(), false);
        }
    }

    @FXML void eliminarLibro(ActionEvent e) {
        try {
            if(libroSeleccionado == null) return;
            libroDAO.borrar(libroSeleccionado);

            cargarLibros(null);
            nuevoLibro(null);
            mostrarMensaje(lblMensajeLibro, "Libro dado de baja correctamente.", true);

        } catch(Exception ex) {
            mostrarMensaje(lblMensajeLibro, "Error al eliminar: " + ex.getMessage(), false);
        }
    }

    // === GESTIÓN AUTORES ===
    @FXML void recargarAutores(ActionEvent e) {
        txtBuscarAutorNombre.clear(); txtBuscarAutorPaterno.clear();
        ArrayList<Autor> autores = autorDAO.consultar();
        tblAutores.setItems(FXCollections.observableArrayList(autores));

        listaAutoresFiltrada = new FilteredList<>(FXCollections.observableArrayList(autores), p -> true);
        listAutoresDisponibles.setItems(listaAutoresFiltrada);
    }

    @FXML void buscarAutor(ActionEvent e) {
        Autor filtro = new Autor();
        if (!txtBuscarAutorNombre.getText().isEmpty()) filtro.setNombre(txtBuscarAutorNombre.getText());
        if (!txtBuscarAutorPaterno.getText().isEmpty()) filtro.setApellidoPaterno(txtBuscarAutorPaterno.getText());
        tblAutores.setItems(FXCollections.observableArrayList(autorDAO.consultar(filtro)));
    }

    @FXML void nuevoAutor(ActionEvent e) {
        autorSeleccionado=null;

        lblModoAutor.setText("(Nuevo)");
        lblModoAutor.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

        txtAutorNombre.clear(); txtAutorPaterno.clear(); txtAutorMaterno.clear();
        lblMensajeAutor.setText("");
        btnEliminarAutor.setVisible(false);
        tblAutores.getSelectionModel().clearSelection();
    }

    @FXML void guardarAutor(ActionEvent e) {
        try {
            Autor a = (autorSeleccionado != null) ? autorSeleccionado : new Autor();
            a.setNombre(txtAutorNombre.getText()); a.setApellidoPaterno(txtAutorPaterno.getText()); a.setApellidoMaterno(txtAutorMaterno.getText());

            boolean esNuevo = (autorSeleccionado == null);
            if(esNuevo) autorDAO.insertar(a); else autorDAO.actualizar(a);

            cargarDatosGenerales();
            nuevoAutor(null);
            mostrarMensaje(lblMensajeAutor, esNuevo ? "Autor registrado." : "Autor actualizado.", true);
        } catch(Exception ex) {
            mostrarMensaje(lblMensajeAutor, "Error: " + ex.getMessage(), false);
        }
    }

    @FXML void eliminarAutor(ActionEvent e) {
        try {
            autorDAO.borrar(autorSeleccionado);
            cargarDatosGenerales();
            nuevoAutor(null);
            mostrarMensaje(lblMensajeAutor, "Autor eliminado correctamente.", true);
        } catch(Exception ex){
            mostrarMensaje(lblMensajeAutor, "Error: " + ex.getMessage(), false);
        }
    }

    // === GESTIÓN EDITORIALES ===
    @FXML void recargarEditoriales(ActionEvent e) {
        txtBuscarEdiNombre.clear(); txtBuscarEdiPais.clear();
        ArrayList<Editorial> lista = editorialDAO.consultar();
        tblEditoriales.setItems(FXCollections.observableArrayList(lista));
        cmbLibroEditorial.setItems(FXCollections.observableArrayList(lista));
        cmbBuscarEditorial.setItems(FXCollections.observableArrayList(lista));
    }

    @FXML void buscarEditorial(ActionEvent e) {
        Editorial filtro = new Editorial();
        if (!txtBuscarEdiNombre.getText().isEmpty()) filtro.setEditorial(txtBuscarEdiNombre.getText());
        if (!txtBuscarEdiPais.getText().isEmpty()) filtro.setPais(txtBuscarEdiPais.getText());
        tblEditoriales.setItems(FXCollections.observableArrayList(editorialDAO.consultar(filtro)));
    }

    @FXML void nuevaEditorial(ActionEvent e) {
        editorialSeleccionada=null;

        lblModoEditorial.setText("(Nuevo)");
        lblModoEditorial.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

        txtEditorialNombre.clear(); txtEditorialPais.clear();
        lblMensajeEditorial.setText("");
        btnEliminarEditorial.setVisible(false);
        tblEditoriales.getSelectionModel().clearSelection();
    }

    @FXML void guardarEditorial(ActionEvent e) {
        try {
            Editorial ed = (editorialSeleccionada != null) ? editorialSeleccionada : new Editorial();
            ed.setEditorial(txtEditorialNombre.getText()); ed.setPais(txtEditorialPais.getText());

            boolean esNuevo = (editorialSeleccionada == null);
            if(esNuevo) editorialDAO.insertar(ed); else editorialDAO.actualizar(ed);

            cargarDatosGenerales();
            nuevaEditorial(null);
            mostrarMensaje(lblMensajeEditorial, esNuevo ? "Editorial registrada." : "Editorial actualizada.", true);
        } catch(Exception ex) {
            mostrarMensaje(lblMensajeEditorial, "Error: " + ex.getMessage(), false);
        }
    }

    @FXML void eliminarEditorial(ActionEvent e) {
        try {
            editorialDAO.borrar(editorialSeleccionada);
            cargarDatosGenerales();
            nuevaEditorial(null);
            mostrarMensaje(lblMensajeEditorial, "Editorial eliminada correctamente.", true);
        } catch(Exception ex){
            mostrarMensaje(lblMensajeEditorial, "Error: " + ex.getMessage(), false);
        }
    }
}