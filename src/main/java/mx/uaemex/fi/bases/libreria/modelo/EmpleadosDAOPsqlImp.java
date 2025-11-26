package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Empleado;
import java.util.ArrayList;
import java.sql.*;

public class EmpleadosDAOPsqlImp extends AbstractSqlDAO implements EmpleadosDAO{

    @Override
    public Empleado insertar(Empleado e) {

        PreparedStatement pstmt = null;
        ArrayList<Empleado> consultados;
        String sql;

        try {
            sql = "INSERT INTO personal.templeado (" +
                    "nombre, apellido_paterno, apellido_materno, cargo, telefono, email, " +
                    "calle, numero_calle, id_localidad, usuario, contraseña" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

            pstmt.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Error al insertar empleado: " + ex.getMessage(), ex);
        } finally {
            try {
                if (pstmt != null){
                    pstmt.close();
                    System.out.println("Conexión Cerrada : PreparedStatement");
                }
            } catch (SQLException ex) {
                System.out.println("Error al cerrar recursos: " + ex);
            }
        }

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
        ArrayList<Empleado> encontrados;
        StringBuilder sql;
        int numColumnas = 0;
        Statement stmt = null;
        ResultSet resultado = null;
        Empleado emp;

        try {
            encontrados = new ArrayList<>();
            stmt = this.conexion.createStatement();

            // Paso (1): Sentencia base
            // IMPORTANTE: Usamos 'personal.templeado' con el esquema
            sql = new StringBuilder("SELECT * FROM personal.templeado");

            // Paso (2): Construir el WHERE basado en el objeto 'empleado'
            // -- ID --
            if (empleado.getId() > 0) {
                sql.append(" WHERE (id_empleado=").append(empleado.getId());
                numColumnas++;
            }

            // -- Nombre --
            if (empleado.getNombre() != null) {
                if (numColumnas != 0) sql.append(" AND nombre='").append(empleado.getNombre()).append("'");
                else { sql.append(" WHERE (nombre='").append(empleado.getNombre()).append("'"); }
                numColumnas++;
            }

            // -- Apellido Paterno --
            if (empleado.getApellidoPaterno() != null) {
                if (numColumnas != 0) sql.append(" AND apellido_paterno='").append(empleado.getApellidoPaterno()).append("'");
                else { sql.append(" WHERE (apellido_paterno='").append(empleado.getApellidoPaterno()).append("'"); }
                numColumnas++;
            }

            // -- Apellido Materno --
            if (empleado.getApellidoMaterno() != null) {
                if (numColumnas != 0) sql.append(" AND apellido_materno='").append(empleado.getApellidoMaterno()).append("'");
                else { sql.append(" WHERE (apellido_materno='").append(empleado.getApellidoMaterno()).append("'"); }
                numColumnas++;
            }

            // -- Cargo --
            if (empleado.getCargo() != null) {
                if (numColumnas != 0) sql.append(" AND cargo='").append(empleado.getCargo()).append("'");
                else { sql.append(" WHERE (cargo='").append(empleado.getCargo()).append("'"); }
                numColumnas++;
            }

            // -- Email --
            if (empleado.getEmail() != null) {
                if (numColumnas != 0) sql.append(" AND email='").append(empleado.getEmail()).append("'");
                else { sql.append(" WHERE (email='").append(empleado.getEmail()).append("'"); }
                numColumnas++;
            }

            // -- Telefono --
            if (empleado.getTelefono() != null) {
                if (numColumnas != 0) sql.append(" AND telefono='").append(empleado.getTelefono()).append("'");
                else { sql.append(" WHERE (telefono='").append(empleado.getTelefono()).append("'"); }
                numColumnas++;
            }

            // -- Calle --
            if (empleado.getCalle() != null) {
                if (numColumnas != 0) sql.append(" AND calle='").append(empleado.getCalle()).append("'");
                else { sql.append(" WHERE (calle='").append(empleado.getCalle()).append("'"); }
                numColumnas++;
            }

            // -- Localidad (int) --
            if (empleado.getIdLocalidad() > 0) {
                if (numColumnas != 0) sql.append(" AND id_localidad=").append(empleado.getIdLocalidad());
                else { sql.append(" WHERE (id_localidad=").append(empleado.getIdLocalidad()); }
                numColumnas++;
            }

            // -- Usuario --
            if (empleado.getUsuario() != null) {
                if (numColumnas != 0) sql.append(" AND usuario='").append(empleado.getUsuario()).append("'");
                else { sql.append(" WHERE (usuario='").append(empleado.getUsuario()).append("'"); }
                numColumnas++;
            }

            // -- Contraseña --
            if (empleado.getContrasenia() != null) {
                if (numColumnas != 0) sql.append(" AND contraseña='").append(empleado.getContrasenia()).append("'");
                else { sql.append(" WHERE (contraseña='").append(empleado.getContrasenia()).append("'"); }
                numColumnas++;
            }

            if (numColumnas != 0) {
                sql.append(")");
            }

            resultado = stmt.executeQuery(sql.toString());

            while (resultado.next()) {
                emp = new Empleado();

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
                emp.setContrasenia(resultado.getString("contraseña"));

                encontrados.add(emp);
            }

            return encontrados;

        } catch (SQLException ex) {
            throw new RuntimeException("Error en la consulta : " + ex.getMessage(), ex);
        } finally {
            try {
                if(resultado != null){
                    resultado.close();
                    System.out.println("Conexión Cerrada : resultSet");
                }

                if(stmt != null){
                    stmt.close();
                    System.out.println("Conexión Cerrada : preparedStatement");
                }

            }catch (SQLException ex) {
                System.out.println("Error al cerrar recursos : " + ex.toString());
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

            // 1. Nombre
            if (e.getNombre() != null) {
                sql.append(" nombre='").append(e.getNombre()).append("'");
                numColumnas++;
            }

            // 2. Apellido Paterno
            if (e.getApellidoPaterno() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" apellido_paterno='").append(e.getApellidoPaterno()).append("'");
                numColumnas++;
            }

            // 3. Apellido Materno
            if (e.getApellidoMaterno() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" apellido_materno='").append(e.getApellidoMaterno()).append("'");
                numColumnas++;
            }

            // 4. Cargo
            if (e.getCargo() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" cargo='").append(e.getCargo()).append("'");
                numColumnas++;
            }

            // 5. Telefono
            if (e.getTelefono() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" telefono='").append(e.getTelefono()).append("'");
                numColumnas++;
            }

            // 6. Email
            if (e.getEmail() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" email='").append(e.getEmail()).append("'");
                numColumnas++;
            }

            // 7. Calle
            if (e.getCalle() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" calle='").append(e.getCalle()).append("'");
                numColumnas++;
            }

            // 8. Numero Calle
            if (e.getNumeroCalle() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" numero_calle='").append(e.getNumeroCalle()).append("'");
                numColumnas++;
            }

            // 9. Id Localidad (int) - Validamos que sea mayor a 0 para actualizar
            if (e.getIdLocalidad() > 0) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" id_localidad=").append(e.getIdLocalidad());
                numColumnas++;
            }

            // 10. Usuario
            if (e.getUsuario() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" usuario='").append(e.getUsuario()).append("'");
                numColumnas++;
            }

            // 11. Contraseña
            if (e.getContrasenia() != null) {
                if (numColumnas > 0) sql.append(",");
                sql.append(" contraseña='").append(e.getContrasenia()).append("'");
                numColumnas++;
            }

            if (e.getId() > 0) {
                sql.append(" WHERE id_empleado=").append(e.getId());
            } else if (e.getUsuario() != null) {
                sql.append(" WHERE usuario='").append(e.getUsuario()).append("'");
            } else {
                throw new RuntimeException("No se puede actualizar: Se requiere ID o Usuario para identificar el registro.");
            }

            if (numColumnas > 0) {
                stmt.executeUpdate(sql.toString());
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar empleado: " + ex.getMessage(), ex);
        } finally {
            try {
                if(stmt != null){
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
        String sql = "DELETE FROM personal.templeado WHERE id_empleado = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = this.conexion.prepareStatement(sql);
            pstmt.setInt(1, e.getId());
        } catch (SQLException ex) {
            throw new RuntimeException("Error al borrar empleado: " + ex.getMessage(), ex);
        } finally {
            try {
                if(pstmt != null){
                    pstmt.close();
                    System.out.println("Conexión Cerrada : PreparedStatement");
                }
            } catch (SQLException ex) {
                System.out.println("Error al cerrar recursos: " + ex);
            }
        }
    }
}
