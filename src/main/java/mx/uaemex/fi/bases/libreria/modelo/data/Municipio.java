package mx.uaemex.fi.bases.libreria.modelo.data;

public class Municipio extends ElementoConID implements Data {
    private String nombre;
    private Estado estado;

    public Municipio() {
        super();
        this.nombre = null;
        this.estado = new Estado();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return this.nombre;
    }
}
