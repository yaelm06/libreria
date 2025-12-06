package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.Cliente;
import mx.uaemex.fi.bases.libreria.modelo.data.DetalleVenta;
import mx.uaemex.fi.bases.libreria.modelo.data.Empleado;
import mx.uaemex.fi.bases.libreria.modelo.data.Libro;
import mx.uaemex.fi.bases.libreria.modelo.data.Venta;
import java.sql.*;
import java.util.ArrayList;

public class VentaDAOPsqlImp extends AbstractSqlDAO implements VentaDAO {

    @Override
    public void cancelarVenta(int idVenta) {
        String sql = "UPDATE ventas.tventa SET activo = false WHERE id_venta = ?";
        try (PreparedStatement pstmt = this.conexion.prepareStatement(sql)) {
            pstmt.setInt(1, idVenta);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error al cancelar la venta", ex);
        }
    }

    @Override
    public ArrayList<Venta> consultar() {
        return consultar(new Venta(), null, null, null, null, null);
    }

    @Override
    public ArrayList<Venta> consultar(Venta filtro, Double minTotal, Double maxTotal, Date fechaInicio, Date fechaFin, String filtroLibro) {
        ArrayList<Venta> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT v.*, ");
        sql.append("c.nombre as c_nom, c.apellido_paterno as c_pat, ");
        sql.append("e.nombre as e_nom, e.apellido_paterno as e_pat, ");
        sql.append("mp.tipo as mp_tipo ");
        sql.append("FROM ventas.tventa v ");
        sql.append("JOIN ventas.tcliente c ON v.id_cliente = c.id_cliente ");
        sql.append("JOIN personal.templeado e ON v.id_empleado = e.id_empleado ");
        sql.append("JOIN ventas.tmetodoPago mp ON v.id_metodo_pago = mp.id_metodo_pago ");

        int cols = 0;

        if (filtro.getId() > 0) {
            sql.append(" WHERE v.id_venta = ").append(filtro.getId());
            cols++;
        }
        if (filtro.getCliente().getNombre() != null && !filtro.getCliente().getNombre().isEmpty()) {
            sql.append(cols > 0 ? " AND" : " WHERE");
            sql.append(" (c.nombre ILIKE '%").append(filtro.getCliente().getNombre()).append("%'");
            sql.append(" OR c.apellido_paterno ILIKE '%").append(filtro.getCliente().getNombre()).append("%')");
            cols++;
        }
        if (filtro.getEmpleado().getNombre() != null && !filtro.getEmpleado().getNombre().isEmpty()) {
            sql.append(cols > 0 ? " AND" : " WHERE");
            sql.append(" e.nombre ILIKE '%").append(filtro.getEmpleado().getNombre()).append("%'");
            cols++;
        }
        if (filtro.getMetodoPago() != null && !filtro.getMetodoPago().isEmpty()) {
            sql.append(cols > 0 ? " AND" : " WHERE");
            sql.append(" mp.tipo = '").append(filtro.getMetodoPago()).append("'");
            cols++;
        }

        if (minTotal != null) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" v.total >= ").append(minTotal);
            cols++;
        }
        if (maxTotal != null) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" v.total <= ").append(maxTotal);
            cols++;
        }
        if (fechaInicio != null) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" v.fecha::date >= '").append(fechaInicio).append("'");
            cols++;
        }
        if (fechaFin != null) {
            sql.append(cols > 0 ? " AND" : " WHERE").append(" v.fecha::date <= '").append(fechaFin).append("'");
            cols++;
        }

        if (filtroLibro != null && !filtroLibro.isEmpty()) {
            sql.append(cols > 0 ? " AND" : " WHERE");
            sql.append(" EXISTS (SELECT 1 FROM ventas.tdetalleVenta dv JOIN catalogo.tlibro l ON dv.id_libro = l.id_libro WHERE dv.id_venta = v.id_venta AND l.titulo ILIKE '%").append(filtroLibro).append("%')");
            cols++;
        }

        sql.append(" ORDER BY v.fecha DESC");

        System.out.println("SQL Ventas: " + sql.toString());

        try (Statement stmt = this.conexion.createStatement(); ResultSet rs = stmt.executeQuery(sql.toString())) {
            while (rs.next()) {
                Venta v = new Venta();
                v.setId(rs.getInt("id_venta"));
                v.setTotal(rs.getDouble("total"));
                v.setFecha(rs.getTimestamp("fecha"));
                v.setActivo(rs.getBoolean("activo"));

                Cliente c = new Cliente();
                c.setId(rs.getInt("id_cliente"));
                c.setNombre(rs.getString("c_nom"));
                c.setApellidoPaterno(rs.getString("c_pat"));
                v.setCliente(c);

                Empleado emp = new Empleado();
                emp.setId(rs.getInt("id_empleado"));
                emp.setNombre(rs.getString("e_nom"));
                emp.setApellidoPaterno(rs.getString("e_pat"));
                v.setEmpleado(emp);

                v.setMetodoPago(rs.getString("mp_tipo"));
                lista.add(v);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error consultando ventas", ex);
        }
        return lista;
    }

    @Override
    public ArrayList<DetalleVenta> consultarDetalle(int idVenta) {
        ArrayList<DetalleVenta> detalles = new ArrayList<>();
        String sql = "SELECT dv.*, l.titulo FROM ventas.tdetalleVenta dv " +
                "JOIN catalogo.tlibro l ON dv.id_libro = l.id_libro " +
                "WHERE dv.id_venta = ?";

        try (PreparedStatement pstmt = this.conexion.prepareStatement(sql)) {
            pstmt.setInt(1, idVenta);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                DetalleVenta dv = new DetalleVenta();
                dv.setCantidad(rs.getInt("cantidad"));
                dv.setPrecioUnitario(rs.getDouble("precioUnitario"));
                dv.setSubtotal(rs.getDouble("subtotal"));

                Libro l = new Libro();
                l.setId(rs.getInt("id_libro"));
                l.setTitulo(rs.getString("titulo"));
                dv.setLibro(l);

                detalles.add(dv);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error obteniendo detalles", ex);
        }
        return detalles;
    }
}