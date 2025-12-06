package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Autor;
import java.util.ArrayList;
import java.sql.*;

public class AutorDAOPsqlImp extends AbstractSqlDAO implements AutorDAO {
    @Override
    public Autor insertar(Autor a) {
        String sql = "INSERT INTO catalogo.tautor (nombre, apellido_paterno, apellido_materno) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = this.conexion.prepareStatement(sql)) {
            pstmt.setString(1, a.getNombre());
            pstmt.setString(2, a.getApellidoPaterno());
            pstmt.setString(3, a.getApellidoMaterno());
            pstmt.executeUpdate();
            return a;
        } catch (SQLException ex) {
            throw new RuntimeException("Error insertando autor", ex);
        }
    }

    @Override
    public ArrayList<Autor> consultar() { return consultar(new Autor()); }

    @Override
    public ArrayList<Autor> consultar(Autor a) {
        ArrayList<Autor> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM catalogo.tautor");
        int cols = 0;

        if(a.getNombre() != null && !a.getNombre().isEmpty()) {
            sql.append(" WHERE nombre ILIKE '%").append(a.getNombre()).append("%'");
            cols++;
        }

        if(a.getApellidoPaterno() != null && !a.getApellidoPaterno().isEmpty()) {
            if (cols > 0) sql.append(" AND apellido_paterno ILIKE '%").append(a.getApellidoPaterno()).append("%'");
            else { sql.append(" WHERE apellido_paterno ILIKE '%").append(a.getApellidoPaterno()).append("%'"); }
            cols++;
        }

        if(a.getApellidoMaterno() != null && !a.getApellidoMaterno().isEmpty()) {
            if (cols > 0) sql.append(" AND apellido_materno ILIKE '%").append(a.getApellidoMaterno()).append("%'");
            else { sql.append(" WHERE apellido_materno ILIKE '%").append(a.getApellidoMaterno()).append("%'"); }
            cols++;
        }

        sql.append(" ORDER BY apellido_paterno, nombre");

        System.out.println("SQL Autor: " + sql.toString());

        try (Statement stmt = this.conexion.createStatement(); ResultSet rs = stmt.executeQuery(sql.toString())) {
            while (rs.next()) {
                Autor aut = new Autor();
                aut.setId(rs.getInt("id_autor"));
                aut.setNombre(rs.getString("nombre"));
                aut.setApellidoPaterno(rs.getString("apellido_paterno"));
                aut.setApellidoMaterno(rs.getString("apellido_materno"));
                lista.add(aut);
            }
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Error consultando autores", ex);
        }
    }

    @Override
    public void actualizar(Autor a) {
        String sql = "UPDATE catalogo.tautor SET nombre=?, apellido_paterno=?, apellido_materno=? WHERE id_autor=?";
        try (PreparedStatement pstmt = this.conexion.prepareStatement(sql)) {
            pstmt.setString(1, a.getNombre());
            pstmt.setString(2, a.getApellidoPaterno());
            pstmt.setString(3, a.getApellidoMaterno());
            pstmt.setInt(4, a.getId());
            pstmt.executeUpdate();
        } catch (SQLException ex) { throw new RuntimeException("Error actualizando autor", ex); }
    }

    @Override
    public void borrar(Autor a) {
        try (PreparedStatement pstmt = this.conexion.prepareStatement("DELETE FROM catalogo.tautor WHERE id_autor=?")) {
            pstmt.setInt(1, a.getId());
            pstmt.executeUpdate();
        } catch (SQLException ex) { throw new RuntimeException("Error borrando autor. Verifique que no tenga libros asociados.", ex); }
    }
}