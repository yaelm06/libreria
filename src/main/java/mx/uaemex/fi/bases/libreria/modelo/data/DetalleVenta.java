package mx.uaemex.fi.bases.libreria.modelo.data;

public class DetalleVenta extends ElementoConID {

    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    private Libro libro;
    private int idVenta;

    public DetalleVenta() {
        super();
        this.libro = new Libro();
    }

    public DetalleVenta(Libro libro, int cantidad) {
        this.libro = libro;
        this.cantidad = cantidad;
        this.precioUnitario = libro.getPrecio();
        this.subtotal = this.cantidad * this.precioUnitario;
    }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
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

    public String getTituloLibro() {
        return (libro != null) ? libro.getTitulo() : "Desconocido";
    }

    public double getImporte() {
        return subtotal;
    }
}