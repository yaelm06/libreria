package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Cliente;
import java.util.ArrayList;
import java.sql.*;

public class ClientesDAOPsqlImp extends AbstractSqlDAO implements ClientesDAO {

    @Override
    public Cliente insertar(Cliente c) {
        PreparedStatement pstmt = null;
        ArrayList<Cliente> consultados;
        String sql = "INSERT INTO ventas.tcliente (" +
                "nombre, apellido_paterno, apellido_materno, telefono, activo" +
                ") VALUES (?, ?, ?, ?, ?)";

        // Validación mínima
        if (c.getNombre() == null || c.getApellidoPaterno() == null) {
            throw new RuntimeException("Nombre y Apellido Paterno son obligatorios.");
        }

        try {
            System.out.println("Ejecutando SQL (Insertar Cliente): " + sql);
            pstmt = this.conexion.prepareStatement(sql);

            pstmt.setString(1, c.getNombre());
            pstmt.setString(2, c.getApellidoPaterno());
            pstmt.setString(3, c.getApellidoMaterno());
            pstmt.setString(4, c.getTelefono());
            pstmt.setBoolean(5, true); // activo = true por defecto

            pstmt.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Error al insertar cliente: " + ex.getMessage(), ex);
        } finally {
            cerrar(pstmt);
        }

        // Recuperar el objeto insertado
        consultados = this.consultar(c);
        if (!consultados.isEmpty()) {
            // Retornamos el último encontrado (por si hay homónimos)
            return consultados.get(consultados.size() - 1);
        } else {
            return null;
        }
    }

    @Override
    public ArrayList<Cliente> consultar() {
        return this.consultar(new Cliente());
    }

    @Override
    public ArrayList<Cliente> consultar(Cliente c) {
        ArrayList<Cliente> encontrados = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM ventas.tcliente");
        int cols = 0;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = this.conexion.createStatement();

            // --- Query By Example ---

            if (c.getId() > 0) {
                sql.append(" WHERE (id_cliente=").append(c.getId());
                cols++;
            }

            if (c.getNombre() != null && !c.getNombre().isEmpty()) {
                if (cols > 0) sql.append(" AND nombre LIKE '%").append(c.getNombre()).append("%'");
                else { sql.append(" WHERE (nombre LIKE '%").append(c.getNombre()).append("%'"); }
                cols++;
            }

            if (c.getApellidoPaterno() != null && !c.getApellidoPaterno().isEmpty()) {
                if (cols > 0) sql.append(" AND apellido_paterno LIKE '%").append(c.getApellidoPaterno()).append("%'");
                else { sql.append(" WHERE (apellido_paterno LIKE '%").append(c.getApellidoPaterno()).append("%'"); }
                cols++;
            }

            if (c.getApellidoMaterno() != null && !c.getApellidoMaterno().isEmpty()) {
                if (cols > 0) sql.append(" AND apellido_materno LIKE '%").append(c.getApellidoMaterno()).append("%'");
                else { sql.append(" WHERE (apellido_materno LIKE '%").append(c.getApellidoMaterno()).append("%'"); }
                cols++;
            }

            if (c.getTelefono() != null && !c.getTelefono().isEmpty()) {
                if (cols > 0) sql.append(" AND telefono LIKE '%").append(c.getTelefono()).append("%'");
                else { sql.append(" WHERE (telefono LIKE '%").append(c.getTelefono()).append("%'"); }
                cols++;
            }

            // Filtro Activo (Inteligente: null = todos, true/false = filtro)
            if (c.isActivo() != null) {
                if (cols > 0) sql.append(" AND activo=").append(c.isActivo());
                else { sql.append(" WHERE (activo=").append(c.isActivo()); }
                if(cols==0) cols++;
            }

            if (cols > 0) {
                sql.append(")");
            }

            sql.append(" ORDER BY apellido_paterno, nombre");

            System.out.println("Ejecutando SQL (Consultar Cliente): " + sql.toString());
            rs = stmt.executeQuery(sql.toString());

            while (rs.next()) {
                Cliente cli = new Cliente();
                cli.setId(rs.getInt("id_cliente"));
                cli.setNombre(rs.getString("nombre"));
                cli.setApellidoPaterno(rs.getString("apellido_paterno"));
                cli.setApellidoMaterno(rs.getString("apellido_materno"));
                cli.setTelefono(rs.getString("telefono"));
                cli.setActivo(rs.getBoolean("activo"));
                encontrados.add(cli);
            }
            return encontrados;

        } catch (SQLException ex) {
            throw new RuntimeException("Error en consulta clientes: " + ex.getMessage(), ex);
        } finally {
            cerrar(stmt, rs);
        }
    }

    @Override
    public void actualizar(Cliente c) {
        StringBuilder sql;
        int cols = 0;
        Statement stmt = null;

        try {
            stmt = this.conexion.createStatement();
            sql = new StringBuilder("UPDATE ventas.tcliente SET");

            if (c.getNombre() != null) { sql.append(" nombre='").append(c.getNombre()).append("'"); cols++; }
            if (c.getApellidoPaterno() != null) { if(cols>0) sql.append(","); sql.append(" apellido_paterno='").append(c.getApellidoPaterno()).append("'"); cols++; }
            if (c.getApellidoMaterno() != null) { if(cols>0) sql.append(","); sql.append(" apellido_materno='").append(c.getApellidoMaterno()).append("'"); cols++; }
            if (c.getTelefono() != null) { if(cols>0) sql.append(","); sql.append(" telefono='").append(c.getTelefono()).append("'"); cols++; }

            // Permitir actualización explícita de activo (para reactivar)
            if (c.isActivo() != null) {
                if(cols>0) sql.append(",");
                sql.append(" activo=").append(c.isActivo());
                cols++;
            }

            if (c.getId() > 0) {
                sql.append(" WHERE id_cliente=").append(c.getId());
            } else {
                throw new RuntimeException("Se requiere ID para actualizar cliente.");
            }

            if (cols > 0) {
                System.out.println("Ejecutando SQL (Actualizar Cliente): " + sql.toString());
                stmt.executeUpdate(sql.toString());
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar cliente: " + ex.getMessage(), ex);
        } finally {
            cerrar(stmt);
        }
    }

    /**
     * Método BORRAR HÍBRIDO:
     * 1. Intenta DELETE físico.
     * 2. Si falla por FK (ventas asociadas), hace UPDATE activo=false.
     */
    @Override
    public void borrar(Cliente c) {
        PreparedStatement pstmt = null;
        String sqlFisico = "DELETE FROM ventas.tcliente WHERE id_cliente=?";
        String sqlLogico = "UPDATE ventas.tcliente SET activo=false WHERE id_cliente=?";

        try {
            // INTENTO 1: Borrado Físico
            System.out.println("Intentando SQL (Borrado Físico Cliente): " + sqlFisico + " [ID=" + c.getId() + "]");
            pstmt = this.conexion.prepareStatement(sqlFisico);
            pstmt.setInt(1, c.getId());

            int filas = pstmt.executeUpdate();
            if (filas > 0) {
                System.out.println("-> Cliente eliminado físicamente (No tenía historial).");
            } else {
                System.out.println("-> No se encontró el cliente ID: " + c.getId());
            }

        } catch (SQLException ex) {
            // Verificamos error FK (23503)
            if ("23503".equals(ex.getSQLState())) {
                System.out.println("-> Falló borrado físico (Tiene ventas). Intentando borrado lógico...");

                // INTENTO 2: Borrado Lógico
                try (PreparedStatement pstmtLogico = this.conexion.prepareStatement(sqlLogico)) {
                    System.out.println("Ejecutando SQL (Baja Lógica Cliente): " + sqlLogico + " [ID=" + c.getId() + "]");
                    pstmtLogico.setInt(1, c.getId());
                    pstmtLogico.executeUpdate();
                    System.out.println("-> Cliente desactivado.");
                } catch (SQLException ex2) {
                    throw new RuntimeException("Error fatal al borrar cliente: " + ex2.getMessage(), ex2);
                }
            } else {
                throw new RuntimeException("Error al borrar cliente: " + ex.getMessage(), ex);
            }
        } finally {
            cerrar(pstmt);
        }
    }

    private void cerrar(Statement s) { try { if(s!=null) s.close(); System.out.println("Conexión Cerrada : Statement"); } catch(Exception e){} }
    private void cerrar(Statement s, ResultSet r) { try { if(r!=null) r.close(); if(s!=null) s.close(); System.out.println("Conexión Cerrada : Stmt/RS"); } catch(Exception e){} }
}