package mx.uaemex.fi.bases.libreria.modelo.data;

public class Cliente extends ElementoConID implements Data {

    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String telefono;
    private Boolean activo; // Boolean objeto para permitir null en filtros

    public Cliente() {
        super();
        this.activo = true; // Por defecto activo al crear
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Boolean isActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombre + " " + apellidoPaterno + " " + (apellidoMaterno != null ? apellidoMaterno : "");
    }
}