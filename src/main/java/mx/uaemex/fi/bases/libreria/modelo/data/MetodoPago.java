package mx.uaemex.fi.bases.libreria.modelo.data;

public class MetodoPago extends ElementoConID {

    private String tipo;

    public MetodoPago() {
        super();
    }

    public MetodoPago(int id, String tipo) {
        this.id = id;
        this.tipo = tipo;
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    @Override
    public String toString() {
        return tipo;
    }
}