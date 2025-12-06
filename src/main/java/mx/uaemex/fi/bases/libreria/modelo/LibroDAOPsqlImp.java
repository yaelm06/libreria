package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Autor;
import mx.uaemex.fi.bases.libreria.modelo.data.Editorial;
import mx.uaemex.fi.bases.libreria.modelo.data.Libro;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class LibroDAOPsqlImp extends AbstractSqlDAO implements LibroDAO {

    @Override
    public Libro insertar(Libro l) {
        String sqlLibro = "INSERT INTO catalogo.tlibro (titulo, isbn, precio, anio, id_editorial, activo) VALUES (?, ?, ?, ?, ?, ?) RETURNING id_libro";
        String sqlRelacion = "INSERT INTO catalogo.tlibroAutor (id_libro, id_autor) VALUES (?, ?)";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = this.conexion.prepareStatement(sqlLibro);
            pstmt.setString(1, l.getTitulo());
            pstmt.setString(2, l.getIsbn());
            pstmt.setDouble(3, l.getPrecio());
            pstmt.setInt(4, l.getAnio());
            pstmt.setInt(5, l.getEditorial().getId());
            pstmt.setBoolean(6, true);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                l.setId(rs.getInt(1));
            }
            pstmt.close();

            if (l.getAutores() != null && !l.getAutores().isEmpty()) {
                pstmt = this.conexion.prepareStatement(sqlRelacion);
                for (Autor autor : l.getAutores()) {
                    pstmt.setInt(1, l.getId());
                    pstmt.setInt(2, autor.getId());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            return l;
        } catch (SQLException ex) {
            throw new RuntimeException("Error insertando libro: " + ex.getMessage(), ex);
        } finally {
            try { if(rs!=null) rs.close(); if(pstmt!=null) pstmt.close(); } catch(Exception e){}
        }
    }

    @Override
    public ArrayList<Libro> consultar() { return consultar(new Libro()); }

    @Override
    public ArrayList<Libro> consultar(Libro l) {
        return consultar(l, null, null, null, null, null);
    }

    // --- CONSULTA AVANZADA CON TODOS LOS FILTROS ---
    @Override
    public ArrayList<Libro> consultar(Libro l, Double minPrecio, Double maxPrecio, Integer minAnio, Integer maxAnio, String filtroAutor) {
        ArrayList<Libro> lista = new ArrayList<>();
        // Usamos la vista que ya tiene los autores concatenados
        StringBuilder sql = new StringBuilder("SELECT * FROM catalogo.vista_libros_completos");
        int cols = 0;

        // 1. Título
        if (l.getTitulo() != null && !l.getTitulo().isEmpty()) {
            sql.append(" WHERE titulo ILIKE '%").append(l.getTitulo()).append("%'");
            cols++;
        }

        // 2. ISBN
        if (l.getIsbn() != null && !l.getIsbn().isEmpty()) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" isbn LIKE '%").append(l.getIsbn()).append("%'");
            cols++;
        }

        // 3. Autor (Búsqueda sobre el texto concatenado de la vista)
        if (filtroAutor != null && !filtroAutor.isEmpty()) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" autores_texto ILIKE '%").append(filtroAutor).append("%'");
            cols++;
        }

        // 4. Filtro Editorial (Si se seleccionó en el objeto Libro)
        if (l.getEditorial() != null && l.getEditorial().getId() > 0) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" id_editorial=").append(l.getEditorial().getId());
            cols++;
        }

        // 5. Rango Precio
        if (minPrecio != null) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" precio >= ").append(minPrecio);
            cols++;
        }
        if (maxPrecio != null) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" precio <= ").append(maxPrecio);
            cols++;
        }

        // 6. Rango Año
        if (minAnio != null) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" anio >= ").append(minAnio);
            cols++;
        }
        if (maxAnio != null) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" anio <= ").append(maxAnio);
            cols++;
        }

        // 7. Activo
        if (l.isActivo()) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" activo=true");
        }

        sql.append(" ORDER BY titulo");

        System.out.println("SQL Avanzado Libros: " + sql.toString());

        try (Statement stmt = this.conexion.createStatement(); ResultSet rs = stmt.executeQuery(sql.toString())) {
            while (rs.next()) {
                Libro lib = new Libro();
                lib.setId(rs.getInt("id_libro"));
                lib.setTitulo(rs.getString("titulo"));
                lib.setIsbn(rs.getString("isbn"));
                lib.setPrecio(rs.getDouble("precio"));
                lib.setAnio(rs.getInt("anio"));
                lib.setActivo(rs.getBoolean("activo"));

                Editorial ed = new Editorial();
                ed.setId(rs.getInt("id_editorial"));
                ed.setEditorial(rs.getString("nombre_editorial"));
                lib.setEditorial(ed);

                lib.setAutoresTexto(rs.getString("autores_texto"));

                lista.add(lib);
            }
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Error consultando libros", ex);
        }
    }

    @Override
    public List<Autor> obtenerAutoresPorLibro(int idLibro) {
        List<Autor> autores = new ArrayList<>();
        String sql = "SELECT a.* FROM catalogo.tautor a " +
                "JOIN catalogo.tlibroAutor la ON a.id_autor = la.id_autor " +
                "WHERE la.id_libro = ?";

        try (PreparedStatement pstmt = this.conexion.prepareStatement(sql)) {
            pstmt.setInt(1, idLibro);
            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()) {
                    Autor a = new Autor();
                    a.setId(rs.getInt("id_autor"));
                    a.setNombre(rs.getString("nombre"));
                    a.setApellidoPaterno(rs.getString("apellido_paterno"));
                    autores.add(a);
                }
            }
            return autores;
        } catch (SQLException ex) {
            throw new RuntimeException("Error cargando autores del libro", ex);
        }
    }

    @Override
    public void actualizar(Libro l) {
        String sqlUpdate = "UPDATE catalogo.tlibro SET titulo=?, isbn=?, precio=?, anio=?, id_editorial=?, activo=? WHERE id_libro=?";
        String sqlDeleteRel = "DELETE FROM catalogo.tlibroAutor WHERE id_libro=?";
        String sqlInsertRel = "INSERT INTO catalogo.tlibroAutor (id_libro, id_autor) VALUES (?, ?)";

        PreparedStatement pstmt = null;
        try {
            pstmt = this.conexion.prepareStatement(sqlUpdate);
            pstmt.setString(1, l.getTitulo());
            pstmt.setString(2, l.getIsbn());
            pstmt.setDouble(3, l.getPrecio());
            pstmt.setInt(4, l.getAnio());
            pstmt.setInt(5, l.getEditorial().getId());
            pstmt.setBoolean(6, l.isActivo());
            pstmt.setInt(7, l.getId());
            pstmt.executeUpdate();
            pstmt.close();

            if (l.getAutores() != null) {
                pstmt = this.conexion.prepareStatement(sqlDeleteRel);
                pstmt.setInt(1, l.getId());
                pstmt.executeUpdate();
                pstmt.close();

                if (!l.getAutores().isEmpty()) {
                    pstmt = this.conexion.prepareStatement(sqlInsertRel);
                    for (Autor autor : l.getAutores()) {
                        pstmt.setInt(1, l.getId());
                        pstmt.setInt(2, autor.getId());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error actualizando libro", ex);
        } finally {
            try { if(pstmt!=null) pstmt.close(); } catch(Exception e){}
        }
    }

    @Override
    public void borrar(Libro l) {
        try (PreparedStatement pstmt = this.conexion.prepareStatement("UPDATE catalogo.tlibro SET activo=false WHERE id_libro=?")) {
            pstmt.setInt(1, l.getId());
            pstmt.executeUpdate();
        } catch (SQLException ex) { throw new RuntimeException("Error borrando libro", ex); }
    }
}