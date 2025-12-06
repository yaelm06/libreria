package mx.uaemex.fi.bases.libreria.modelo.data;

public class DetalleVenta extends ElementoConID {

    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    // Relación con Libro para mostrar Título e ISBN en el reporte
    private Libro libro;

    // ID de la venta padre (útil a veces, aunque opcional si navegas desde Venta)
    private int idVenta;

    public DetalleVenta() {
        super();
        this.libro = new Libro();
    }

    // Getters y Setters
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }

    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }

    // Importante para reportes rápidos
    public String getTituloLibro() {
        return (libro != null) ? libro.getTitulo() : "Desconocido";
    }
}