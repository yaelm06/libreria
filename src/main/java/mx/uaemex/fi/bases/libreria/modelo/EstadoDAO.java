package mx.uaemex.fi.bases.libreria.modelo;
import mx.uaemex.fi.bases.libreria.modelo.data.Estado;
import java.util.ArrayList;

public interface EstadoDAO {
    public Estado insertar(Estado e);
    public ArrayList<Estado> consultar();
    public ArrayList<Estado> consultar(Estado e);
    public void actualizar(Estado e);
    public void borrar(Estado e);
}