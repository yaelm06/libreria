package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Estado;
import java.util.ArrayList;
import java.sql.*;

public class EstadoDAOPsqlImp extends AbstractSqlDAO implements EstadoDAO {

    @Override
    public Estado insertar(Estado e) {
        String sql = "INSERT INTO ubicaciones.testado (estado) VALUES (?)";
        PreparedStatement pstmt = null;
        try {
            System.out.println("Ejecutando SQL (Estado Insert): " + sql);
            pstmt = this.conexion.prepareStatement(sql);
            pstmt.setString(1, e.getNombre());
            pstmt.executeUpdate();
            System.out.println("-> Estado insertado: " + e.getNombre());
            return e;
        } catch (SQLException ex) {
            throw new RuntimeException("Error al insertar estado: " + ex.getMessage(), ex);
        } finally {
            cerrar(pstmt);
        }
    }

    @Override
    public ArrayList<Estado> consultar() {
        return consultar(new Estado());
    }

    @Override
    public ArrayList<Estado> consultar(Estado e) {
        ArrayList<Estado> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM ubicaciones.testado");
        int cols = 0;

        if (e.getId() > 0) {
            sql.append(" WHERE id_estado=").append(e.getId());
            cols++;
        }
        if (e.getNombre() != null) {
            if (cols > 0) sql.append(" AND estado='").append(e.getNombre()).append("'");
            else sql.append(" WHERE estado='").append(e.getNombre()).append("'");
        }

        sql.append(" ORDER BY estado");

        Statement stmt = null;
        ResultSet rs = null;
        try {
            System.out.println("Ejecutando SQL (Estado Consultar): " + sql);
            stmt = this.conexion.createStatement();
            rs = stmt.executeQuery(sql.toString());
            while (rs.next()) {
                Estado estado = new Estado();
                estado.setId(rs.getInt("id_estado"));
                estado.setNombre(rs.getString("estado"));
                lista.add(estado);
            }
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Error consultando estados: " + ex.getMessage(), ex);
        } finally {
            cerrar(stmt, rs);
        }
    }

    @Override
    public void actualizar(Estado e) {
        String sql = "UPDATE ubicaciones.testado SET estado=? WHERE id_estado=?";
        PreparedStatement pstmt = null;
        try {
            System.out.println("Ejecutando SQL (Estado Update): " + sql);
            pstmt = this.conexion.prepareStatement(sql);
            pstmt.setString(1, e.getNombre());
            pstmt.setInt(2, e.getId());
            pstmt.executeUpdate();
            System.out.println("-> Estado actualizado ID: " + e.getId());
        } catch (SQLException ex) {
            throw new RuntimeException("Error actualizando estado: " + ex.getMessage(), ex);
        } finally {
            cerrar(pstmt);
        }
    }

    @Override
    public void borrar(Estado e) {
        String sql = "DELETE FROM ubicaciones.testado WHERE id_estado=?";
        PreparedStatement pstmt = null;
        try {
            System.out.println("Ejecutando SQL (Estado Delete): " + sql);
            pstmt = this.conexion.prepareStatement(sql);
            pstmt.setInt(1, e.getId());
            pstmt.executeUpdate();
            System.out.println("-> Estado eliminado ID: " + e.getId());
        } catch (SQLException ex) {
            throw new RuntimeException("Error borrando estado: " + ex.getMessage(), ex);
        } finally {
            cerrar(pstmt);
        }
    }

    private void cerrar(Statement s) { try { if(s!=null) s.close(); } catch(Exception e){} }
    private void cerrar(Statement s, ResultSet r) { try { if(r!=null) r.close(); if(s!=null) s.close(); } catch(Exception e){} }
}