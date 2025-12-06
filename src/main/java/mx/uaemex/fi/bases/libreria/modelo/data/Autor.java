package mx.uaemex.fi.bases.libreria.modelo.data;

public class Autor extends ElementoConID implements Data {
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;

    public Autor() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public String getNombreCompleto() {
        return nombre + " " + apellidoPaterno + (apellidoMaterno != null ? " " + apellidoMaterno : "");
    }

    @Override
    public String toString() {
        return getNombreCompleto();
    }
}
