package mx.uaemex.fi.bases.libreria.modelo.data;

public class Editorial extends ElementoConID implements Data {
    private String editorial;
    private String pais;

    public Editorial() {}

    public Editorial(int id, String editorial) {
        this.id = id;
        this.editorial = editorial;
    }

    public String getEditorial() { return editorial; }
    public void setEditorial(String editorial) { this.editorial = editorial; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    @Override
    public String toString() {
        return editorial;
    }
}