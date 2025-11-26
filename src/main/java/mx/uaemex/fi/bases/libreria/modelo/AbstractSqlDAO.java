package mx.uaemex.fi.bases.libreria.modelo;

import java.sql.Connection;

public class AbstractSqlDAO {
    protected Connection conexion;

    public AbstractSqlDAO() {
        this.conexion = null;
    }

    public Connection getConexion() {
        return conexion;
    }

    public void setConexion(Connection conexion) {
        this.conexion = conexion;
    }
}
