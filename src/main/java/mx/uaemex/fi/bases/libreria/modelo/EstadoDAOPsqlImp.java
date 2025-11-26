package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Estado;
import java.util.ArrayList;
import java.sql.*;

public class EstadoDAOPsqlImp extends AbstractSqlDAO implements EstadoDAO {

    @Override
    public Estado insertar(Estado e) {
        System.out.println("--- [EstadoDAO] Insertar ---");
        PreparedStatement pstmt = null;
        ArrayList<Estado> consultados;
        String sql = "INSERT INTO ubicaciones.testado (estado) VALUES (?)";

        try {
            pstmt = this.conexion.prepareStatement(sql);
            pstmt.setString(1, e.getNombre());
            pstmt.executeUpdate();
            System.out.println("Estado insertado correctamente.");

        } catch (SQLException ex) {
            throw new RuntimeException("Error al insertar estado: " + ex.getMessage(), ex);
        } finally {
            try {
                if (pstmt != null) { pstmt.close(); System.out.println("Conexión Cerrada : PreparedStatement"); }
            } catch (SQLException ex) { System.out.println("Error cerrar: " + ex); }
        }

        consultados = this.consultar(e);
        return !consultados.isEmpty() ? consultados.get(0) : null;
    }

    @Override
    public ArrayList<Estado> consultar() {
        return this.consultar(new Estado());
    }

    @Override
    public ArrayList<Estado> consultar(Estado estado) {
        ArrayList<Estado> encontrados = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM ubicaciones.testado");
        int numColumnas = 0;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = this.conexion.createStatement();

            if (estado.getId() > 0) {
                sql.append(" WHERE (id_estado=").append(estado.getId());
                numColumnas++;
            }
            if (estado.getNombre() != null) {
                if (numColumnas != 0) sql.append(" AND estado='").append(estado.getNombre()).append("'");
                else sql.append(" WHERE (estado='").append(estado.getNombre()).append("'");
                numColumnas++;
            }
            if (numColumnas != 0) sql.append(")");

            // System.out.println("[EstadoDAO] SQL: " + sql.toString());
            rs = stmt.executeQuery(sql.toString());

            while (rs.next()) {
                Estado est = new Estado();
                est.setId(rs.getInt("id_estado"));
                est.setNombre(rs.getString("estado"));
                encontrados.add(est);
            }
            return encontrados;

        } catch (SQLException ex) {
            throw new RuntimeException("Error consulta estados: " + ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException ex) { }
        }
    }

    @Override
    public void actualizar(Estado e) {
        StringBuilder sql = new StringBuilder("UPDATE ubicaciones.testado SET");
        int numColumnas = 0;
        Statement stmt = null;

        try {
            stmt = this.conexion.createStatement();
            if (e.getNombre() != null) {
                sql.append(" estado='").append(e.getNombre()).append("'");
                numColumnas++;
            }

            if (e.getId() > 0) {
                sql.append(" WHERE id_estado=").append(e.getId());
            } else {
                throw new RuntimeException("ID requerido para actualizar Estado");
            }

            if (numColumnas > 0) stmt.executeUpdate(sql.toString());

        } catch (SQLException ex) {
            throw new RuntimeException("Error actualizar estado: " + ex.getMessage(), ex);
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ex) { }
        }
    }

    @Override
    public void borrar(Estado e) {
        String sql = "DELETE FROM ubicaciones.testado WHERE id_estado = ?";
        try (PreparedStatement pstmt = this.conexion.prepareStatement(sql)) {
            pstmt.setInt(1, e.getId());
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error borrar estado: " + ex.getMessage(), ex);
        }
    }
}