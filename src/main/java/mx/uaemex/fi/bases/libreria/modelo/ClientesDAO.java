package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Cliente;
import java.util.ArrayList;

public interface ClientesDAO {
    public Cliente insertar(Cliente c);
    public ArrayList<Cliente> consultar();
    public ArrayList<Cliente> consultar(Cliente c);
    public void actualizar(Cliente c);
    public void borrar(Cliente c);
}