package mx.uaemex.fi.bases.libreria.modelo.data;

/**
 * Representa un Estado de la República.
 * Mapea la tabla 'testado'.
 */
public class Estado extends ElementoConID implements Data {
    private String nombre;

    public Estado() {
        super();
    }

    public Estado(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return this.nombre;
    }
}
