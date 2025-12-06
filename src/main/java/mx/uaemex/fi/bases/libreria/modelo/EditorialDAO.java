package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Editorial;
import java.util.ArrayList;

public interface EditorialDAO {
    public Editorial insertar(Editorial e);
    public ArrayList<Editorial> consultar();
    public ArrayList<Editorial> consultar(Editorial e);
    public void actualizar(Editorial e);
    public void borrar(Editorial e);
}