package mx.uaemex.fi.bases.libreria.modelo.data;

import java.util.ArrayList;
import java.util.List;

public class Libro extends ElementoConID implements Data {
    private String titulo;
    private String isbn;
    private double precio;
    private int anio;
    private boolean activo;

    // Relaciones
    private Editorial editorial;
    private List<Autor> autores;

    // Auxiliar para mostrar en tabla (traído desde la vista SQL)
    private String autoresTexto;

    public Libro() {
        this.activo = true;
        this.editorial = new Editorial();
        this.autores = new ArrayList<>();
    }

    // Getters y Setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Editorial getEditorial() { return editorial; }
    public void setEditorial(Editorial editorial) { this.editorial = editorial; }

    public List<Autor> getAutores() { return autores; }
    public void setAutores(List<Autor> autores) { this.autores = autores; }

    public String getAutoresTexto() { return autoresTexto; }
    public void setAutoresTexto(String autoresTexto) { this.autoresTexto = autoresTexto; }
}
