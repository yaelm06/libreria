package mx.uaemex.fi.bases.libreria.modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    // Agregamos ?characterEncoding=UTF-8 para arreglar los acentos
    private static final String DB_URL = "jdbc:postgresql://database-1.cnsiwgwsie1g.us-east-2.rds.amazonaws.com:5432/libreria?characterEncoding=UTF-8";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "Admin-AWS-123";

    public Connection obtenerConexion() {
        Connection conexion = null;
        try {
            conexion = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Conexión a AWS RDS exitosa.");
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