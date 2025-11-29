package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Empleado;
import java.util.ArrayList;
import java.sql.*;

public class EmpleadosDAOPsqlImp extends AbstractSqlDAO implements EmpleadosDAO {

    @Override
    public Empleado insertar(Empleado e) {
        PreparedStatement pstmt = null;
        ArrayList<Empleado> consultados;
        String sql = "INSERT INTO personal.templeado (" +
                "nombre, apellido_paterno, apellido_materno, cargo, telefono, email, " +
                "calle, numero_calle, id_localidad, usuario, contrasenia, activo" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Validación de datos mínimos
        if (e.getUsuario() == null || e.getContrasenia() == null || e.getNombre() == null) {
            throw new RuntimeException("Informacion insuficiente, NO es posible hacer el registro");
        }

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

        } catch (SQLException ex) {
            throw new RuntimeException("Error al insertar empleado: " + ex.getMessage(), ex);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                    System.out.println("Conexión Cerrada : PreparedStatement");
                }
            } catch (SQLException ex) {
                System.out.println("Error al cerrar recursos: " + ex);
            }
        }

        // Retornar el objeto insertado consultándolo de vuelta
        consultados = this.consultar(e);

        if (!consultados.isEmpty()) {
            return consultados.get(0);
        } else {
            return null;
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
        int numColumnas = 0;
        Statement stmt = null;
        ResultSet resultado = null;

        try {
            stmt = this.conexion.createStatement();

            // --- Construcción Dinámica (QBE) con TODOS los campos ---

            // 1. ID
            if (empleado.getId() > 0) {
                sql.append(" WHERE (id_empleado=").append(empleado.getId());
                numColumnas++;
            }

            // 2. Usuario
            if (empleado.getUsuario() != null && !empleado.getUsuario().isEmpty()) {
                if (numColumnas != 0) sql.append(" AND usuario='").append(empleado.getUsuario()).append("'");
                else { sql.append(" WHERE (usuario='").append(empleado.getUsuario()).append("'"); }
                numColumnas++;
            }

            // 3. Contraseña
            if (empleado.getContrasenia() != null && !empleado.getContrasenia().isEmpty()) {
                if (numColumnas != 0) sql.append(" AND contrasenia='").append(empleado.getContrasenia()).append("'");
                else { sql.append(" WHERE (contrasenia='").append(empleado.getContrasenia()).append("'"); }
                numColumnas++;
            }

            // 4. Nombre
            if (empleado.getNombre() != null && !empleado.getNombre().isEmpty()) {
                if (numColumnas != 0) sql.append(" AND nombre LIKE '%").append(empleado.getNombre()).append("%'");
                else { sql.append(" WHERE (nombre LIKE '%").append(empleado.getNombre()).append("%'"); }
                numColumnas++;
            }

            // 5. Apellido Paterno
            if (empleado.getApellidoPaterno() != null && !empleado.getApellidoPaterno().isEmpty()) {
                if (numColumnas != 0) sql.append(" AND apellido_paterno LIKE '%").append(empleado.getApellidoPaterno()).append("%'");
                else { sql.append(" WHERE (apellido_paterno LIKE '%").append(empleado.getApellidoPaterno()).append("%'"); }
                numColumnas++;
            }

            // 6. Apellido Materno
            if (empleado.getApellidoMaterno() != null && !empleado.getApellidoMaterno().isEmpty()) {
                if (numColumnas != 0) sql.append(" AND apellido_materno LIKE '%").append(empleado.getApellidoMaterno()).append("%'");
                else { sql.append(" WHERE (apellido_materno LIKE '%").append(empleado.getApellidoMaterno()).append("%'"); }
                numColumnas++;
            }

            // 7. Cargo
            if (empleado.getCargo() != null && !empleado.getCargo().isEmpty()) {
                if (numColumnas != 0) sql.append(" AND cargo='").append(empleado.getCargo()).append("'");
                else { sql.append(" WHERE (cargo='").append(empleado.getCargo()).append("'"); }
                numColumnas++;
            }

            // 8. Email
            if (empleado.getEmail() != null && !empleado.getEmail().isEmpty()) {
                if (numColumnas != 0) sql.append(" AND email LIKE '%").append(empleado.getEmail()).append("%'");
                else { sql.append(" WHERE (email LIKE '%").append(empleado.getEmail()).append("%'"); }
                numColumnas++;
            }

            // 9. Telefono
            if (empleado.getTelefono() != null && !empleado.getTelefono().isEmpty()) {
                if (numColumnas != 0) sql.append(" AND telefono='").append(empleado.getTelefono()).append("'");
                else { sql.append(" WHERE (telefono='").append(empleado.getTelefono()).append("'"); }
                numColumnas++;
            }

            // 10. Calle
            if (empleado.getCalle() != null && !empleado.getCalle().isEmpty()) {
                if (numColumnas != 0) sql.append(" AND calle LIKE '%").append(empleado.getCalle()).append("%'");
                else { sql.append(" WHERE (calle LIKE '%").append(empleado.getCalle()).append("%'"); }
                numColumnas++;
            }

            // 11. Numero Calle
            if (empleado.getNumeroCalle() != null && !empleado.getNumeroCalle().isEmpty()) {
                if (numColumnas != 0) sql.append(" AND numero_calle='").append(empleado.getNumeroCalle()).append("'");
                else { sql.append(" WHERE (numero_calle='").append(empleado.getNumeroCalle()).append("'"); }
                numColumnas++;
            }

            // 12. Id Localidad
            if (empleado.getIdLocalidad() > 0) {
                if (numColumnas != 0) sql.append(" AND id_localidad=").append(empleado.getIdLocalidad());
                else { sql.append(" WHERE (id_localidad=").append(empleado.getIdLocalidad()); }
                numColumnas++;
            }

            // 13. Activo
            // Por defecto traemos activos si no se especifica ID, pero respetamos el filtro si viene false
            if (empleado.isActivo() != null) {
                if (numColumnas != 0) sql.append(" AND activo=").append(empleado.isActivo());
                else { sql.append(" WHERE (activo=").append(empleado.isActivo()); }
                if (numColumnas == 0) numColumnas++; // Ajuste para el paréntesis
            }

            if (numColumnas != 0) {
                sql.append(")");
            }

            // Ordenar por nombre
            sql.append(" ORDER BY nombre");

            System.out.println("Ejecutando SQL (Consultar): " + sql.toString());

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

            return encontrados;

        } catch (SQLException ex) {
            throw new RuntimeException("Error en la consulta : " + ex.getMessage(), ex);
        } finally {
            try {
                if (resultado != null) {
                    resultado.close();
                    System.out.println("Conexión Cerrada : ResultSet");
                }
                if (stmt != null) {
                    stmt.close();
                    System.out.println("Conexión Cerrada : Statement");
                }
            } catch (SQLException ex) {
                System.out.println("Error al cerrar recursos: " + ex);
            }
        }
    }

    @Override
    public void actualizar(Empleado e) {
        StringBuilder sql;
        int numColumnas = 0;
        Statement stmt = null;

        try {
            stmt = this.conexion.createStatement();
            sql = new StringBuilder("UPDATE personal.templeado SET");

            // --- Construcción Dinámica del UPDATE con TODOS los campos ---

            if (e.getNombre() != null) {
                sql.append(" nombre='").append(e.getNombre()).append("'");
                numColumnas++;
            }
            if (e.getApellidoPaterno() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" apellido_paterno='").append(e.getApellidoPaterno()).append("'");
                numColumnas++;
            }
            if (e.getApellidoMaterno() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" apellido_materno='").append(e.getApellidoMaterno()).append("'");
                numColumnas++;
            }
            if (e.getCargo() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" cargo='").append(e.getCargo()).append("'");
                numColumnas++;
            }
            if (e.getTelefono() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" telefono='").append(e.getTelefono()).append("'");
                numColumnas++;
            }
            if (e.getEmail() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" email='").append(e.getEmail()).append("'");
                numColumnas++;
            }
            if (e.getCalle() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" calle='").append(e.getCalle()).append("'");
                numColumnas++;
            }
            if (e.getNumeroCalle() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" numero_calle='").append(e.getNumeroCalle()).append("'");
                numColumnas++;
            }
            if (e.getIdLocalidad() > 0) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" id_localidad=").append(e.getIdLocalidad());
                numColumnas++;
            }
            if (e.getUsuario() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" usuario='").append(e.getUsuario()).append("'");
                numColumnas++;
            }
            if (e.getContrasenia() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" contrasenia='").append(e.getContrasenia()).append("'");
                numColumnas++;
            }
            // También podemos actualizar el estado activo/inactivo si se requiere
            if (e.isActivo() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" activo=").append(e.isActivo());
                numColumnas++;
            }

            // WHERE para identificar el registro
            if (e.getId() > 0) {
                sql.append(" WHERE id_empleado=").append(e.getId());
            } else if (e.getUsuario() != null) {
                sql.append(" WHERE usuario='").append(e.getUsuario()).append("'");
            } else {
                throw new RuntimeException("Se requiere ID o Usuario para actualizar.");
            }

            if (numColumnas > 0) {
                System.out.println("Ejecutando SQL (Actualizar): " + sql.toString());
                stmt.executeUpdate(sql.toString());
            } else {
                System.out.println("No hay campos a actualizar para: " + e.getUsuario());
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar: " + ex.getMessage(), ex);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                    System.out.println("Conexión Cerrada : Statement");
                }
            } catch (SQLException ex) {
                System.out.println("Error al cerrar recursos: " + ex);
            }
        }
    }

    @Override
    public void borrar(Empleado e) {
        PreparedStatement pstmt = null;
        // Borrado Lógico
        String sqlFisico = "DELETE FROM personal.templeado WHERE id_empleado=?";
        String sqlLogico = "UPDATE personal.templeado SET activo=false WHERE id_empleado=?";

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
            // Verificamos si el error es por Llave Foránea (Código SQLState 23503 en Postgres)
            if ("23503".equals(ex.getSQLState())) {
                System.out.println("-> Falló borrado físico (Tiene registros asociados). Intentando borrado lógico...");

                // INTENTO 2: Borrado Lógico (Fallback)
                try (PreparedStatement pstmtLogico = this.conexion.prepareStatement(sqlLogico)) {
                    System.out.println("Ejecutando SQL (Borrado Lógico): " + sqlLogico + " [ID=" + e.getId() + "]");
                    pstmtLogico.setInt(1, e.getId());
                    pstmtLogico.executeUpdate();
                    System.out.println("-> Empleado desactivado (Baja Lógica).");
                } catch (SQLException ex2) {
                    throw new RuntimeException("Error fatal: Falló tanto el borrado físico como el lógico. " + ex2.getMessage(), ex2);
                }
            } else {
                throw new RuntimeException("Error al borrar empleado: " + ex.getMessage(), ex);
            }
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                    System.out.println("Conexión Cerrada : PreparedStatement");
                }
            } catch (SQLException ex) {
                System.out.println("Error al cerrar recursos: " + ex);
            }
        }
    }
}