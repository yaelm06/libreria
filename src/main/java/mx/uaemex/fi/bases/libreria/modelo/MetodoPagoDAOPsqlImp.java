package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.MetodoPago;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class MetodoPagoDAOPsqlImp extends AbstractSqlDAO implements MetodoPagoDAO {

    @Override
    public ArrayList<MetodoPago> consultar() {
        ArrayList<MetodoPago> lista = new ArrayList<>();
        String sql = "SELECT * FROM ventas.tmetodoPago ORDER BY id_metodo_pago";

        try (Statement stmt = this.conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                MetodoPago mp = new MetodoPago();
                mp.setId(rs.getInt("id_metodo_pago"));
                mp.setTipo(rs.getString("tipo"));
                lista.add(mp);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al consultar métodos de pago", ex);
        }
    }
}