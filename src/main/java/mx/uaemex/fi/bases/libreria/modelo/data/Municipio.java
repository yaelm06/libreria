package mx.uaemex.fi.bases.libreria.modelo.data;

/**
 * Representa un Municipio.
 * Mapea la tabla 'tmunicipio'.
 */
public class Municipio extends ElementoConID implements Data {
    private String nombre;
    private Estado estado; // Relación con el objeto Estado

    public Municipio() {
        super();
        this.nombre = null;
        this.estado = new Estado(); // Inicializamos para evitar NullPointerException
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
