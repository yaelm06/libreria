package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.DetalleVenta; // Necesario
import mx.uaemex.fi.bases.libreria.modelo.data.Venta;
import java.util.ArrayList;
import java.sql.Date;

public interface VentaDAO {
    public void cancelarVenta(int idVenta);

    public ArrayList<Venta> consultar();

    public ArrayList<Venta> consultar(Venta filtro, Double minTotal, Double maxTotal, Date fechaInicio, Date fechaFin, String filtroLibro);

    public ArrayList<DetalleVenta> consultarDetalle(int idVenta);
}