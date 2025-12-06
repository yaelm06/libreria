package mx.uaemex.fi.bases.libreria.modelo.data;

public class Localidad extends ElementoConID implements Data {
    private String localidad;
    private String codigoPostal;
    private Municipio municipio;
    private Estado estado;

    public Localidad() {
        super();
        this.localidad = null;
        this.codigoPostal = null;
        this.municipio = new Municipio();
        this.estado = new Estado();
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return this.localidad + " (" + this.codigoPostal + ")";
    }
}
