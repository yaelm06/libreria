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

    // Sobreescribir toString es CRUCIAL para que el ComboBox muestre el nombre y no la referencia de memoria
    @Override
    public String toString() {
        return tipo;
    }
}