package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Autor;
import mx.uaemex.fi.bases.libreria.modelo.data.Libro;
import java.util.ArrayList;
import java.util.List;

public interface LibroDAO {
    public Libro insertar(Libro l);
    public ArrayList<Libro> consultar();
    public ArrayList<Libro> consultar(Libro l);

    // ACTUALIZADO: Agregamos filtroAutor (String)
    public ArrayList<Libro> consultar(Libro l, Double minPrecio, Double maxPrecio, Integer minAnio, Integer maxAnio, String filtroAutor);

    public void actualizar(Libro l);
    public void borrar(Libro l);
    public List<Autor> obtenerAutoresPorLibro(int idLibro);
}