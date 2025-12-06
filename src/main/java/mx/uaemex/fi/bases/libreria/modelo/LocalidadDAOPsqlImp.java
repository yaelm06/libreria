package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Localidad;
import mx.uaemex.fi.bases.libreria.modelo.data.Estado;
import mx.uaemex.fi.bases.libreria.modelo.data.Municipio;
import java.util.ArrayList;
import java.sql.*;

public class LocalidadDAOPsqlImp extends AbstractSqlDAO implements LocalidadDAO {

    @Override
    public Localidad insertar(Localidad l) {
        String sql = "INSERT INTO ubicaciones.tlocalidad (localidad, codigo_postal, id_municipio) VALUES (?, ?, ?)";
        PreparedStatement pstmt = null;
        try {
            System.out.println("Ejecutando SQL (Localidad Insert): " + sql);
            pstmt = this.conexion.prepareStatement(sql);
            pstmt.setString(1, l.getLocalidad());
            pstmt.setString(2, l.getCodigoPostal());

            if (l.getMunicipio() != null && l.getMunicipio().getId() > 0) {
                pstmt.setInt(3, l.getMunicipio().getId());
            } else {
                throw new RuntimeException("Se requiere un municipio válido.");
            }

            pstmt.executeUpdate();
            System.out.println("-> Localidad registrada con éxito: " + l.getLocalidad());
            return l;
        } catch (SQLException ex) {
            throw new RuntimeException("Error insertando localidad: " + ex.getMessage(), ex);
        } finally {
            cerrar(pstmt);
        }
    }

    @Override
    public ArrayList<Localidad> consultar() {
        return consultar(new Localidad());
    }

    @Override
    public ArrayList<Localidad> consultar(Localidad l) {
        ArrayList<Localidad> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM ubicaciones.vista_localidades_completas");
        int cols = 0;

        if (l.getId() > 0) {
            sql.append(" WHERE id_localidad=").append(l.getId());
            cols++;
        }
        if (l.getLocalidad() != null && !l.getLocalidad().isEmpty()) {
            if (cols > 0) sql.append(" AND localidad LIKE '%").append(l.getLocalidad()).append("%'");
            else sql.append(" WHERE localidad LIKE '%").append(l.getLocalidad()).append("%'");
            cols++;
        }
        if (l.getCodigoPostal() != null && !l.getCodigoPostal().isEmpty()) {
            if (cols > 0) sql.append(" AND codigo_postal LIKE '%").append(l.getCodigoPostal()).append("%'");
            else sql.append(" WHERE codigo_postal LIKE '%").append(l.getCodigoPostal()).append("%'");
            cols++;
        }
        if (l.getMunicipio() != null && l.getMunicipio().getNombre() != null && !l.getMunicipio().getNombre().isEmpty()) {
            if (cols > 0) sql.append(" AND municipio='").append(l.getMunicipio().getNombre()).append("'");
            else sql.append(" WHERE municipio='").append(l.getMunicipio().getNombre()).append("'");
            cols++;
        }
        if (l.getEstado() != null && l.getEstado().getNombre() != null && !l.getEstado().getNombre().isEmpty()) {
            if (cols > 0) sql.append(" AND estado='").append(l.getEstado().getNombre()).append("'");
            else sql.append(" WHERE estado='").append(l.getEstado().getNombre()).append("'");
            cols++;
        }

        Statement stmt = null;
        ResultSet rs = null;
        try {
            System.out.println("Ejecutando SQL (Localidad Consultar): " + sql);
            stmt = this.conexion.createStatement();
            rs = stmt.executeQuery(sql.toString());

            while (rs.next()) {
                Localidad loc = new Localidad();
                loc.setId(rs.getInt("id_localidad"));
                loc.setLocalidad(rs.getString("localidad"));
                loc.setCodigoPostal(rs.getString("codigo_postal"));

                Municipio mun = new Municipio();
                mun.setId(rs.getInt("id_municipio"));
                mun.setNombre(rs.getString("municipio"));
                loc.setMunicipio(mun);

                Estado est = new Estado();
                est.setId(rs.getInt("id_estado"));
                est.setNombre(rs.getString("estado"));
                loc.setEstado(est);

                lista.add(loc);
            }
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Error consultando localidades: " + ex.getMessage(), ex);
        } finally {
            cerrar(stmt, rs);
        }
    }

    @Override
    public void actualizar(Localidad l) {
        String sql = "UPDATE ubicaciones.tlocalidad SET localidad=?, codigo_postal=?, id_municipio=? WHERE id_localidad=?";
        PreparedStatement pstmt = null;
        try {
            System.out.println("Ejecutando SQL (Localidad Update): " + sql);
            pstmt = this.conexion.prepareStatement(sql);
            pstmt.setString(1, l.getLocalidad());
            pstmt.setString(2, l.getCodigoPostal());
            pstmt.setInt(3, l.getMunicipio().getId());
            pstmt.setInt(4, l.getId());
            pstmt.executeUpdate();
            System.out.println("-> Localidad actualizada correctamente ID: " + l.getId());
        } catch (SQLException ex) {
            throw new RuntimeException("Error actualizando localidad: " + ex.getMessage(), ex);
        } finally {
            cerrar(pstmt);
        }
    }

    @Override
    public void borrar(Localidad l) {
        String sql = "DELETE FROM ubicaciones.tlocalidad WHERE id_localidad=?";
        PreparedStatement pstmt = null;
        try {
            System.out.println("Ejecutando SQL (Localidad Delete): " + sql);
            pstmt = this.conexion.prepareStatement(sql);
            pstmt.setInt(1, l.getId());
            pstmt.executeUpdate();
            System.out.println("-> Localidad eliminada correctamente ID: " + l.getId());
        } catch (SQLException ex) {
            throw new RuntimeException("Error borrando localidad: " + ex.getMessage(), ex);
        } finally {
            cerrar(pstmt);
        }
    }

    private void cerrar(Statement s) { try { if(s!=null) s.close(); } catch(Exception e){} }
    private void cerrar(Statement s, ResultSet r) { try { if(r!=null) r.close(); if(s!=null) s.close(); } catch(Exception e){} }
}