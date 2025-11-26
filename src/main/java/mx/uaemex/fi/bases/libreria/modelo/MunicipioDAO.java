package mx.uaemex.fi.bases.libreria.modelo;
import mx.uaemex.fi.bases.libreria.modelo.data.Municipio;
import java.util.ArrayList;

public interface MunicipioDAO {
    public Municipio insertar(Municipio m);
    public ArrayList<Municipio> consultar();
    public ArrayList<Municipio> consultar(Municipio m);
    public void actualizar(Municipio m);
    public void borrar(Municipio m);
}