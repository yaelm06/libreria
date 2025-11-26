package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Localidad;
import mx.uaemex.fi.bases.libreria.modelo.data.Municipio;
import mx.uaemex.fi.bases.libreria.modelo.data.Estado;
import java.util.ArrayList;
import java.sql.*;

public class LocalidadDAOPsqlImp extends AbstractSqlDAO implements LocalidadDAO {

    @Override
    public Localidad insertar(Localidad l) {
        System.out.println("--- [LocalidadDAO] Insertar ---");
        PreparedStatement pstmt = null;
        ArrayList<Localidad> consultados;
        // INSERTAMOS EN LA TABLA FÍSICA, NO EN LA VISTA
        String sql = "INSERT INTO ubicaciones.tlocalidad (localidad, codigo_postal, id_municipio) VALUES (?, ?, ?)";

        try {
            pstmt = this.conexion.prepareStatement(sql);
            pstmt.setString(1, l.getLocalidad());
            pstmt.setString(2, l.getCodigoPostal());

            if (l.getMunicipio() != null && l.getMunicipio().getId() > 0) {
                pstmt.setInt(3, l.getMunicipio().getId());
            } else {
                throw new RuntimeException("Se requiere un Objeto Municipio con ID para insertar Localidad");
            }

            pstmt.executeUpdate();
            System.out.println("Localidad insertada correctamente en tlocalidad.");

        } catch (SQLException ex) {
            throw new RuntimeException("Error al insertar localidad: " + ex.getMessage(), ex);
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException ex) { }
        }

        consultados = this.consultar(l);
        return !consultados.isEmpty() ? consultados.get(0) : null;
    }

    @Override
    public ArrayList<Localidad> consultar() {
        return this.consultar(new Localidad());
    }

    @Override
    public ArrayList<Localidad> consultar(Localidad l) {
        ArrayList<Localidad> encontrados = new ArrayList<>();
        StringBuilder sql;
        int numColumnas = 0;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = this.conexion.createStatement();

            // --- IMPORTANTE: Consultamos a la VISTA para poder filtrar por estado/municipio ---
            sql = new StringBuilder("SELECT * FROM ubicaciones.vista_localidades_completas");

            // 1. ID Localidad
            if (l.getId() > 0) {
                sql.append(" WHERE (id_localidad=").append(l.getId());
                numColumnas++;
            }

            // 2. Localidad (Nombre)
            if (l.getLocalidad() != null) {
                if (numColumnas != 0) sql.append(" AND localidad='").append(l.getLocalidad()).append("'");
                else sql.append(" WHERE (localidad='").append(l.getLocalidad()).append("'");
                numColumnas++;
            }

            // 3. Código Postal
            if (l.getCodigoPostal() != null) {
                if (numColumnas != 0) sql.append(" AND codigo_postal='").append(l.getCodigoPostal()).append("'");
                else sql.append(" WHERE (codigo_postal='").append(l.getCodigoPostal()).append("'");
                numColumnas++;
            }

            // 4. ID Municipio (Filtro de Cascada Nivel 2)
            if (l.getMunicipio() != null && l.getMunicipio().getId() > 0) {
                if (numColumnas != 0) sql.append(" AND id_municipio=").append(l.getMunicipio().getId());
                else sql.append(" WHERE (id_municipio=").append(l.getMunicipio().getId());
                numColumnas++;
            }

            // 5. ID Estado (Filtro de Cascada Nivel 1 - Gracias a la VISTA)
            if (l.getEstado() != null && l.getEstado().getId() > 0) {
                if (numColumnas != 0) sql.append(" AND id_estado=").append(l.getEstado().getId());
                else sql.append(" WHERE (id_estado=").append(l.getEstado().getId());
                numColumnas++;
            }

            if (numColumnas != 0) sql.append(")");

            // System.out.println("[LocalidadDAO] QBE en Vista: " + sql.toString());
            rs = stmt.executeQuery(sql.toString());

            while (rs.next()) {
                Localidad loc = new Localidad();
                loc.setId(rs.getInt("id_localidad"));
                loc.setLocalidad(rs.getString("localidad"));
                loc.setCodigoPostal(rs.getString("codigo_postal"));

                // Mapeamos el Municipio completo desde la vista
                Municipio mun = new Municipio();
                mun.setId(rs.getInt("id_municipio"));
                mun.setNombre(rs.getString("municipio")); // Nombre viene de la vista
                loc.setMunicipio(mun);

                // Mapeamos el Estado completo desde la vista
                Estado est = new Estado();
                est.setId(rs.getInt("id_estado"));
                est.setNombre(rs.getString("estado")); // Nombre viene de la vista
                loc.setEstado(est);

                encontrados.add(loc);
            }
            return encontrados;

        } catch (SQLException ex) {
            throw new RuntimeException("Error consultar vista localidad: " + ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException ex) { }
        }
    }

    @Override
    public void actualizar(Localidad l) {
        // ACTUALIZAMOS LA TABLA FÍSICA
        StringBuilder sql = new StringBuilder("UPDATE ubicaciones.tlocalidad SET");
        int numColumnas = 0;
        Statement stmt = null;

        try {
            stmt = this.conexion.createStatement();

            if (l.getLocalidad() != null) {
                sql.append(" localidad='").append(l.getLocalidad()).append("'");
                numColumnas++;
            }
            if (l.getCodigoPostal() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" codigo_postal='").append(l.getCodigoPostal()).append("'");
                numColumnas++;
            }
            if (l.getMunicipio() != null && l.getMunicipio().getId() > 0) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" id_municipio=").append(l.getMunicipio().getId());
                numColumnas++;
            }

            if (l.getId() > 0) {
                sql.append(" WHERE id_localidad=").append(l.getId());
            } else {
                throw new RuntimeException("ID requerido para actualizar Localidad");
            }

            if (numColumnas > 0) stmt.executeUpdate(sql.toString());

        } catch (SQLException ex) {
            throw new RuntimeException("Error actualizar localidad: " + ex.getMessage(), ex);
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ex) { }
        }
    }

    @Override
    public void borrar(Localidad l) {
        // BORRAMOS DE LA TABLA FÍSICA
        String sql = "DELETE FROM ubicaciones.tlocalidad WHERE id_localidad = ?";
        try (PreparedStatement pstmt = this.conexion.prepareStatement(sql)) {
            pstmt.setInt(1, l.getId());
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error borrar localidad: " + ex.getMessage(), ex);
        }
    }
}