package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Editorial;
import java.util.ArrayList;
import java.sql.*;

public class EditorialDAOPsqlImp extends AbstractSqlDAO implements EditorialDAO {
    @Override
    public Editorial insertar(Editorial e) {
        String sql = "INSERT INTO catalogo.teditorial (editorial, pais) VALUES (?, ?)";
        try (PreparedStatement pstmt = this.conexion.prepareStatement(sql)) {
            pstmt.setString(1, e.getEditorial());
            pstmt.setString(2, e.getPais());
            pstmt.executeUpdate();
            return e;
        } catch (SQLException ex) {
            throw new RuntimeException("Error insertando editorial: " + ex.getMessage(), ex);
        }
    }

    @Override
    public ArrayList<Editorial> consultar() { return consultar(new Editorial()); }

    @Override
    public ArrayList<Editorial> consultar(Editorial e) {
        ArrayList<Editorial> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM catalogo.teditorial");
        int cols = 0;


        if(e.getId() > 0) {
            sql.append(" WHERE id_editorial=").append(e.getId());
            cols++;
        }

        if(e.getEditorial() != null && !e.getEditorial().isEmpty()) {
            if (cols > 0) sql.append(" AND editorial ILIKE '%").append(e.getEditorial()).append("%'");
            else { sql.append(" WHERE editorial ILIKE '%").append(e.getEditorial()).append("%'"); }
            cols++;
        }

        if(e.getPais() != null && !e.getPais().isEmpty()) {
            if (cols > 0) sql.append(" AND pais ILIKE '%").append(e.getPais()).append("%'");
            else { sql.append(" WHERE pais ILIKE '%").append(e.getPais()).append("%'"); }
            cols++;
        }

        sql.append(" ORDER BY editorial");

        System.out.println("SQL Editorial: " + sql.toString());

        try (Statement stmt = this.conexion.createStatement(); ResultSet rs = stmt.executeQuery(sql.toString())) {
            while (rs.next()) {
                Editorial ed = new Editorial();
                ed.setId(rs.getInt("id_editorial"));
                ed.setEditorial(rs.getString("editorial"));
                ed.setPais(rs.getString("pais"));
                lista.add(ed);
            }
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Error consultando editoriales", ex);
        }
    }

    @Override
    public void actualizar(Editorial e) {
        String sql = "UPDATE catalogo.teditorial SET editorial=?, pais=? WHERE id_editorial=?";
        try (PreparedStatement pstmt = this.conexion.prepareStatement(sql)) {
            pstmt.setString(1, e.getEditorial());
            pstmt.setString(2, e.getPais());
            pstmt.setInt(3, e.getId());
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error actualizando editorial", ex);
        }
    }

    @Override
    public void borrar(Editorial e) {
        try (PreparedStatement pstmt = this.conexion.prepareStatement("DELETE FROM catalogo.teditorial WHERE id_editorial=?")) {
            pstmt.setInt(1, e.getId());
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error borrando editorial (¿tiene libros asociados?)", ex);
        }
    }
}