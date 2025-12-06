package mx.uaemex.fi.bases.libreria.modelo.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Venta extends ElementoConID implements Data {

    private double total;
    private Timestamp fecha;
    private boolean activo;

    private Cliente cliente;
    private Empleado empleado;
    private String metodoPago;

    private List<DetalleVenta> detalles;

    public Venta() {
        super();
        this.cliente = new Cliente();
        this.empleado = new Empleado();
        this.detalles = new ArrayList<>();
        this.activo = true;
    }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }

    @Override
    public String toString() {
        return "Venta #" + id + " ($" + total + ")";
    }
}