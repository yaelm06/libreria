package mx.uaemex.fi.bases.libreria.modelo;
import mx.uaemex.fi.bases.libreria.modelo.data.Localidad;
import java.util.ArrayList;

public interface LocalidadDAO {
    public Localidad insertar(Localidad l);
    public ArrayList<Localidad> consultar();
    public ArrayList<Localidad> consultar(Localidad l);
    public void actualizar(Localidad l);
    public void borrar(Localidad l);
}