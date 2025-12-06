package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Estado;
import mx.uaemex.fi.bases.libreria.modelo.data.Municipio;
import java.util.ArrayList;
import java.sql.*;

public class MunicipioDAOPsqlImp extends AbstractSqlDAO implements MunicipioDAO {

    @Override
    public Municipio insertar(Municipio m) {
        String sql = "INSERT INTO ubicaciones.tmunicipio (municipio, id_estado) VALUES (?, ?)";
        PreparedStatement pstmt = null;
        try {
            System.out.println("Ejecutando SQL (Municipio Insert): " + sql);
            pstmt = this.conexion.prepareStatement(sql);
            pstmt.setString(1, m.getNombre());
            pstmt.setInt(2, m.getEstado().getId());

            pstmt.executeUpdate();
            System.out.println("-> Municipio insertado: " + m.getNombre());
            return m;
        } catch (SQLException ex) {
            throw new RuntimeException("Error al insertar municipio: " + ex.getMessage(), ex);
        } finally {
            cerrar(pstmt);
        }
    }

    @Override
    public ArrayList<Municipio> consultar() {
        return consultar(new Municipio());
    }

    @Override
    public ArrayList<Municipio> consultar(Municipio m) {
        ArrayList<Municipio> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM ubicaciones.vista_municipios_completos");
        int cols = 0;

        if (m.getId() > 0) {
            sql.append(" WHERE id_municipio=").append(m.getId());
            cols++;
        }
        if (m.getNombre() != null && !m.getNombre().isEmpty()) {
            if (cols > 0) sql.append(" AND municipio LIKE '%").append(m.getNombre()).append("%'");
            else sql.append(" WHERE municipio LIKE '%").append(m.getNombre()).append("%'");
            cols++;
        }
        if (m.getEstado() != null && m.getEstado().getId() > 0) {
            int idEst = m.getEstado().getId();
            if (cols > 0) sql.append(" AND id_estado=").append(idEst);
            else sql.append(" WHERE id_estado=").append(idEst);
            cols++;
        }

        Statement stmt = null;
        ResultSet rs = null;
        try {
            System.out.println("Ejecutando SQL (Municipio Vista): " + sql);
            stmt = this.conexion.createStatement();
            rs = stmt.executeQuery(sql.toString());

            while (rs.next()) {
                Municipio mun = new Municipio();
                mun.setId(rs.getInt("id_municipio"));
                mun.setNombre(rs.getString("municipio"));

                Estado est = new Estado();
                est.setId(rs.getInt("id_estado"));
                est.setNombre(rs.getString("estado"));
                mun.setEstado(est);

                lista.add(mun);
            }
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Error consultando vista municipios: " + ex.getMessage(), ex);
        } finally {
            cerrar(stmt, rs);
        }
    }

    @Override
    public void actualizar(Municipio m) {
        String sql = "UPDATE ubicaciones.tmunicipio SET municipio=?, id_estado=? WHERE id_municipio=?";
        PreparedStatement pstmt = null;
        try {
            System.out.println("Ejecutando SQL (Municipio Update): " + sql);
            pstmt = this.conexion.prepareStatement(sql);
            pstmt.setString(1, m.getNombre());
            pstmt.setInt(2, m.getEstado().getId());
            pstmt.setInt(3, m.getId());
            pstmt.executeUpdate();
            System.out.println("-> Municipio actualizado ID: " + m.getId());
        } catch (SQLException ex) {
            throw new RuntimeException("Error actualizando municipio: " + ex.getMessage(), ex);
        } finally {
            cerrar(pstmt);
        }
    }

    @Override
    public void borrar(Municipio m) {
        String sql = "DELETE FROM ubicaciones.tmunicipio WHERE id_municipio=?";
        PreparedStatement pstmt = null;
        try {
            System.out.println("Ejecutando SQL (Municipio Delete): " + sql);
            pstmt = this.conexion.prepareStatement(sql);
            pstmt.setInt(1, m.getId());
            pstmt.executeUpdate();
            System.out.println("-> Municipio eliminado ID: " + m.getId());
        } catch (SQLException ex) {
            throw new RuntimeException("Error borrando municipio: " + ex.getMessage(), ex);
        } finally {
            cerrar(pstmt);
        }
    }

    private void cerrar(Statement s) { try { if(s!=null) s.close(); } catch(Exception e){} }
    private void cerrar(Statement s, ResultSet r) { try { if(r!=null) r.close(); if(s!=null) s.close(); } catch(Exception e){} }
}