package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Municipio;
import mx.uaemex.fi.bases.libreria.modelo.data.Estado;
import java.util.ArrayList;
import java.sql.*;

public class MunicipioDAOPsqlImp extends AbstractSqlDAO implements MunicipioDAO {

    @Override
    public Municipio insertar(Municipio m) {
        System.out.println("--- [MunicipioDAO] Insertar ---");
        PreparedStatement pstmt = null;
        ArrayList<Municipio> consultados;
        String sql = "INSERT INTO ubicaciones.tmunicipio (municipio, id_estado) VALUES (?, ?)";

        try {
            pstmt = this.conexion.prepareStatement(sql);
            pstmt.setString(1, m.getNombre());
            // Validamos que venga el estado
            if (m.getEstado() != null && m.getEstado().getId() > 0) {
                pstmt.setInt(2, m.getEstado().getId());
            } else {
                throw new RuntimeException("Se requiere un Objeto Estado con ID para insertar Municipio");
            }

            pstmt.executeUpdate();
            System.out.println("Municipio insertado correctamente.");

        } catch (SQLException ex) {
            throw new RuntimeException("Error al insertar municipio: " + ex.getMessage(), ex);
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException ex) { }
        }

        consultados = this.consultar(m);
        return !consultados.isEmpty() ? consultados.get(0) : null;
    }

    @Override
    public ArrayList<Municipio> consultar() {
        return this.consultar(new Municipio());
    }

    @Override
    public ArrayList<Municipio> consultar(Municipio m) {
        ArrayList<Municipio> encontrados = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM ubicaciones.tmunicipio");
        int numColumnas = 0;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = this.conexion.createStatement();

            // 1. ID Municipio
            if (m.getId() > 0) {
                sql.append(" WHERE (id_municipio=").append(m.getId());
                numColumnas++;
            }
            // 2. Nombre Municipio
            if (m.getNombre() != null) {
                if (numColumnas != 0) sql.append(" AND municipio='").append(m.getNombre()).append("'");
                else sql.append(" WHERE (municipio='").append(m.getNombre()).append("'");
                numColumnas++;
            }
            // 3. ID Estado (Relación)
            if (m.getEstado() != null && m.getEstado().getId() > 0) {
                if (numColumnas != 0) sql.append(" AND id_estado=").append(m.getEstado().getId());
                else sql.append(" WHERE (id_estado=").append(m.getEstado().getId());
                numColumnas++;
            }

            if (numColumnas != 0) sql.append(")");

            // System.out.println("[MunicipioDAO] SQL: " + sql.toString());
            rs = stmt.executeQuery(sql.toString());

            while (rs.next()) {
                Municipio mun = new Municipio();
                mun.setId(rs.getInt("id_municipio"));
                mun.setNombre(rs.getString("municipio"));

                // Recuperamos el ID del estado y creamos el objeto
                Estado est = new Estado();
                est.setId(rs.getInt("id_estado"));
                mun.setEstado(est);

                encontrados.add(mun);
            }
            return encontrados;

        } catch (SQLException ex) {
            throw new RuntimeException("Error consultar municipios: " + ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException ex) { }
        }
    }

    @Override
    public void actualizar(Municipio m) {
        StringBuilder sql = new StringBuilder("UPDATE ubicaciones.tmunicipio SET");
        int numColumnas = 0;
        Statement stmt = null;

        try {
            stmt = this.conexion.createStatement();

            if (m.getNombre() != null) {
                sql.append(" municipio='").append(m.getNombre()).append("'");
                numColumnas++;
            }
            if (m.getEstado() != null && m.getEstado().getId() > 0) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" id_estado=").append(m.getEstado().getId());
                numColumnas++;
            }

            if (m.getId() > 0) {
                sql.append(" WHERE id_municipio=").append(m.getId());
            } else {
                throw new RuntimeException("ID requerido para actualizar Municipio");
            }

            if (numColumnas > 0) stmt.executeUpdate(sql.toString());

        } catch (SQLException ex) {
            throw new RuntimeException("Error actualizar municipio: " + ex.getMessage(), ex);
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ex) { }
        }
    }

    @Override
    public void borrar(Municipio m) {
        String sql = "DELETE FROM ubicaciones.tmunicipio WHERE id_municipio = ?";
        try (PreparedStatement pstmt = this.conexion.prepareStatement(sql)) {
            pstmt.setInt(1, m.getId());
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error borrar municipio: " + ex.getMessage(), ex);
        }
    }
}