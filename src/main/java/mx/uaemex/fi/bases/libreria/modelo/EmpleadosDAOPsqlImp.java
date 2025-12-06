package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Empleado;
import java.util.ArrayList;
import java.sql.*;

public class EmpleadosDAOPsqlImp extends AbstractSqlDAO implements EmpleadosDAO {

    @Override
    public Empleado insertar(Empleado e) {
        // Validación de datos mínimos
        if (e.getUsuario() == null || e.getContrasenia() == null || e.getNombre() == null) {
            throw new RuntimeException("Información insuficiente, NO es posible hacer el registro");
        }

        String sql = "INSERT INTO personal.templeado (" +
                "nombre, apellido_paterno, apellido_materno, cargo, telefono, email, " +
                "calle, numero_calle, id_localidad, usuario, contrasenia, activo" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; // No usamos RETURNING aquí para compatibilidad básica, pero consultamos después

        PreparedStatement pstmt = null;
        try {
            System.out.println("Ejecutando SQL (Insertar): " + sql);

            pstmt = this.conexion.prepareStatement(sql);

            pstmt.setString(1, e.getNombre());
            pstmt.setString(2, e.getApellidoPaterno());
            pstmt.setString(3, e.getApellidoMaterno());
            pstmt.setString(4, e.getCargo());
            pstmt.setString(5, e.getTelefono());
            pstmt.setString(6, e.getEmail());
            pstmt.setString(7, e.getCalle());
            pstmt.setString(8, e.getNumeroCalle());
            pstmt.setInt(9, e.getIdLocalidad());
            pstmt.setString(10, e.getUsuario());
            pstmt.setString(11, e.getContrasenia());
            pstmt.setBoolean(12, true); // activo = true por defecto

            pstmt.executeUpdate();
            System.out.println("-> Empleado insertado correctamente: " + e.getUsuario());

            // Recuperar el ID generado consultando por usuario (Estrategia segura)
            ArrayList<Empleado> consultados = this.consultar(e);
            return consultados.isEmpty() ? null : consultados.get(0);

        } catch (SQLException ex) {
            throw new RuntimeException("Error al insertar empleado: " + ex.getMessage(), ex);
        } finally {
            cerrar(pstmt);
        }
    }

    @Override
    public ArrayList<Empleado> consultar() {
        return this.consultar(new Empleado());
    }

    @Override
    public ArrayList<Empleado> consultar(Empleado empleado) {
        ArrayList<Empleado> encontrados = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM personal.templeado");
        int cols = 0;

        // --- Construcción Dinámica Limpia ---

        // 1. ID
        if (empleado.getId() > 0) {
            sql.append(" WHERE id_empleado=").append(empleado.getId());
            cols++;
        }

        // 2. Usuario
        if (empleado.getUsuario() != null && !empleado.getUsuario().isEmpty()) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" usuario='").append(empleado.getUsuario()).append("'");
            cols++;
        }

        // 3. Contraseña (Login)
        if (empleado.getContrasenia() != null && !empleado.getContrasenia().isEmpty()) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" contrasenia='").append(empleado.getContrasenia()).append("'");
            cols++;
        }

        // 4. Nombre
        if (empleado.getNombre() != null && !empleado.getNombre().isEmpty()) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" nombre ILIKE '%").append(empleado.getNombre()).append("%'");
            cols++;
        }

        // 5. Apellido Paterno
        if (empleado.getApellidoPaterno() != null && !empleado.getApellidoPaterno().isEmpty()) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" apellido_paterno ILIKE '%").append(empleado.getApellidoPaterno()).append("%'");
            cols++;
        }

        // 6. Apellido Materno
        if (empleado.getApellidoMaterno() != null && !empleado.getApellidoMaterno().isEmpty()) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" apellido_materno ILIKE '%").append(empleado.getApellidoMaterno()).append("%'");
            cols++;
        }

        // 7. Cargo
        if (empleado.getCargo() != null && !empleado.getCargo().isEmpty()) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" cargo='").append(empleado.getCargo()).append("'");
            cols++;
        }

        // 8. Email
        if (empleado.getEmail() != null && !empleado.getEmail().isEmpty()) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" email ILIKE '%").append(empleado.getEmail()).append("%'");
            cols++;
        }

        // 9. Telefono
        if (empleado.getTelefono() != null && !empleado.getTelefono().isEmpty()) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" telefono='").append(empleado.getTelefono()).append("'");
            cols++;
        }

        // 10. Calle
        if (empleado.getCalle() != null && !empleado.getCalle().isEmpty()) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" calle ILIKE '%").append(empleado.getCalle()).append("%'");
            cols++;
        }

        // 11. Numero Calle
        if (empleado.getNumeroCalle() != null && !empleado.getNumeroCalle().isEmpty()) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" numero_calle='").append(empleado.getNumeroCalle()).append("'");
            cols++;
        }

        // 12. Id Localidad
        if (empleado.getIdLocalidad() > 0) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" id_localidad=").append(empleado.getIdLocalidad());
            cols++;
        }

        // 13. Activo (IMPORTANTE: Solo agregar filtro si el objeto lo especifica explícitamente)
        if (empleado.isActivo() != null) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" activo=").append(empleado.isActivo());
            cols++;
        }

        // Ordenar por nombre
        sql.append(" ORDER BY nombre");

        Statement stmt = null;
        ResultSet resultado = null;

        try {
            System.out.println("Ejecutando SQL (Consultar): " + sql.toString()); // AHORA SÍ SE VERÁ
            stmt = this.conexion.createStatement();
            resultado = stmt.executeQuery(sql.toString());

            while (resultado.next()) {
                Empleado emp = new Empleado();
                emp.setId(resultado.getInt("id_empleado"));
                emp.setNombre(resultado.getString("nombre"));
                emp.setApellidoPaterno(resultado.getString("apellido_paterno"));
                emp.setApellidoMaterno(resultado.getString("apellido_materno"));
                emp.setCargo(resultado.getString("cargo"));
                emp.setTelefono(resultado.getString("telefono"));
                emp.setEmail(resultado.getString("email"));
                emp.setCalle(resultado.getString("calle"));
                emp.setNumeroCalle(resultado.getString("numero_calle"));
                emp.setIdLocalidad(resultado.getInt("id_localidad"));
                emp.setUsuario(resultado.getString("usuario"));
                emp.setContrasenia(resultado.getString("contrasenia"));
                emp.setActivo(resultado.getBoolean("activo"));

                encontrados.add(emp);
            }

            System.out.println("-> Registros encontrados: " + encontrados.size());
            return encontrados;

        } catch (SQLException ex) {
            throw new RuntimeException("Error en la consulta : " + ex.getMessage(), ex);
        } finally {
            cerrar(stmt, resultado);
        }
    }

    @Override
    public void actualizar(Empleado e) {
        StringBuilder sql = new StringBuilder("UPDATE personal.templeado SET");
        int cols = 0;

        if (e.getNombre() != null) { sql.append(" nombre='").append(e.getNombre()).append("'"); cols++; }
        if (e.getApellidoPaterno() != null) { sql.append(cols>0?",":"").append(" apellido_paterno='").append(e.getApellidoPaterno()).append("'"); cols++; }
        if (e.getApellidoMaterno() != null) { sql.append(cols>0?",":"").append(" apellido_materno='").append(e.getApellidoMaterno()).append("'"); cols++; }
        if (e.getCargo() != null) { sql.append(cols>0?",":"").append(" cargo='").append(e.getCargo()).append("'"); cols++; }
        if (e.getTelefono() != null) { sql.append(cols>0?",":"").append(" telefono='").append(e.getTelefono()).append("'"); cols++; }
        if (e.getEmail() != null) { sql.append(cols>0?",":"").append(" email='").append(e.getEmail()).append("'"); cols++; }
        if (e.getCalle() != null) { sql.append(cols>0?",":"").append(" calle='").append(e.getCalle()).append("'"); cols++; }
        if (e.getNumeroCalle() != null) { sql.append(cols>0?",":"").append(" numero_calle='").append(e.getNumeroCalle()).append("'"); cols++; }
        if (e.getIdLocalidad() > 0) { sql.append(cols>0?",":"").append(" id_localidad=").append(e.getIdLocalidad()); cols++; }
        if (e.getUsuario() != null) { sql.append(cols>0?",":"").append(" usuario='").append(e.getUsuario()).append("'"); cols++; }
        if (e.getContrasenia() != null) { sql.append(cols>0?",":"").append(" contrasenia='").append(e.getContrasenia()).append("'"); cols++; }
        if (e.isActivo() != null) { sql.append(cols>0?",":"").append(" activo=").append(e.isActivo()); cols++; }

        if (cols == 0) {
            System.out.println("No hay cambios para actualizar.");
            return;
        }

        if (e.getId() > 0) {
            sql.append(" WHERE id_empleado=").append(e.getId());
        } else if (e.getUsuario() != null) {
            sql.append(" WHERE usuario='").append(e.getUsuario()).append("'");
        } else {
            throw new RuntimeException("Se requiere ID o Usuario para actualizar.");
        }

        Statement stmt = null;
        try {
            System.out.println("Ejecutando SQL (Actualizar): " + sql.toString());
            stmt = this.conexion.createStatement();
            int afectados = stmt.executeUpdate(sql.toString());
            System.out.println("-> Empleado actualizado. Filas afectadas: " + afectados);
        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar: " + ex.getMessage(), ex);
        } finally {
            cerrar(stmt);
        }
    }

    @Override
    public void borrar(Empleado e) {
        String sqlFisico = "DELETE FROM personal.templeado WHERE id_empleado=?";
        String sqlLogico = "UPDATE personal.templeado SET activo=false WHERE id_empleado=?";
        PreparedStatement pstmt = null;

        try {
            // INTENTO 1: Borrado Físico
            System.out.println("Intentando SQL (Borrado Físico): " + sqlFisico + " [ID=" + e.getId() + "]");
            pstmt = this.conexion.prepareStatement(sqlFisico);
            pstmt.setInt(1, e.getId());

            int filas = pstmt.executeUpdate();
            if (filas > 0) {
                System.out.println("-> Empleado eliminado físicamente (No tenía ventas).");
            } else {
                System.out.println("-> No se encontró el empleado con ID: " + e.getId());
            }

        } catch (SQLException ex) {
            // Error 23503: Violación de llave foránea (tiene ventas)
            if ("23503".equals(ex.getSQLState())) {
                System.out.println("-> Falló borrado físico (Tiene registros). Intentando borrado lógico...");
                cerrar(pstmt); // Cerramos el anterior

                try {
                    System.out.println("Ejecutando SQL (Borrado Lógico): " + sqlLogico);
                    pstmt = this.conexion.prepareStatement(sqlLogico);
                    pstmt.setInt(1, e.getId());
                    pstmt.executeUpdate();
                    System.out.println("-> Empleado desactivado (Baja Lógica) correctamente.");
                } catch (SQLException ex2) {
                    throw new RuntimeException("Error fatal al desactivar empleado: " + ex2.getMessage(), ex2);
                }
            } else {
                throw new RuntimeException("Error al borrar empleado: " + ex.getMessage(), ex);
            }
        } finally {
            cerrar(pstmt);
        }
    }

    // Métodos auxiliares para cerrar recursos y evitar duplicidad de try-catch
    private void cerrar(Statement s) { try { if(s!=null) s.close(); } catch(Exception e){} }
    private void cerrar(Statement s, ResultSet r) { try { if(r!=null) r.close(); if(s!=null) s.close(); } catch(Exception e){} }
}