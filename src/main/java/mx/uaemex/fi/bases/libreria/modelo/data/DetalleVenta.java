package mx.uaemex.fi.bases.libreria.modelo.data;

public class DetalleVenta extends ElementoConID {

    private int cantidad;
    private double precioUnitario;
    private double subtotal; // En BD se llama subtotal, en la vista a veces importe

    // Relación con Libro
    private Libro libro;
    private int idVenta;

    public DetalleVenta() {
        super();
        this.libro = new Libro();
    }

    // Constructor para uso rápido en el Carrito (POS)
    public DetalleVenta(Libro libro, int cantidad) {
        this.libro = libro;
        this.cantidad = cantidad;
        this.precioUnitario = libro.getPrecio();
        this.subtotal = this.cantidad * this.precioUnitario;
    }

    // --- GETTERS Y SETTERS ---

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        // Recalcular subtotal automáticamente al cambiar cantidad
        this.subtotal = this.cantidad * this.precioUnitario;
    }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        this.subtotal = this.cantidad * this.precioUnitario;
    }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }

    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }

    // --- MÉTODOS AUXILIARES PARA LA TABLA (TableView) ---
    // La tabla busca métodos que empiecen con "get" seguidos del nombre de la columna

    public String getTituloLibro() {
        return (libro != null) ? libro.getTitulo() : "Desconocido";
    }

    // Alias para que la tabla encuentre "Importe" si así se configuró
    public double getImporte() {
        return subtotal;
    }
}