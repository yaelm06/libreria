package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Empleado;

import java.util.ArrayList;

public interface EmpleadosDAO {
    public Empleado insertar(Empleado e);
    public ArrayList<Empleado> consultar(); // Consultar todos
    public ArrayList<Empleado> consultar(Empleado e);
    public void actualizar(Empleado e);
    public void borrar(Empleado e);
}
