package mx.uaemex.fi.bases.libreria.modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String DB_URL = "jdbc:postgresql://localhost:5433/libreria";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "admin";

    public Connection obtenerConexion() {
        Connection conexion = null;
        try {
            conexion = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Conexión a la BD exitosa.");
        } catch (SQLException e) {
            System.err.println("Error al conectar a la BD: " + e.getMessage());
            e.printStackTrace();
        }
        return conexion;
    }

    public void cerrarRecursos(AutoCloseable... recursos) {
        for (AutoCloseable recurso : recursos) {
            if (recurso != null) {
                try {
                    recurso.close();
                } catch (Exception e) {
                    System.err.println("Error al cerrar recurso: " + e.getMessage());
                }
            }
        }
    }
}
