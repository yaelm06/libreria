package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Autor;
import java.util.ArrayList;

public interface AutorDAO {
    public Autor insertar(Autor a);
    public ArrayList<Autor> consultar();
    public ArrayList<Autor> consultar(Autor a);
    public void actualizar(Autor a);
    public void borrar(Autor a);
}