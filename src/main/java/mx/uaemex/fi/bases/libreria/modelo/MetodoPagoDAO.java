package mx.uaemex.fi.bases.libreria.modelo;

import mx.uaemex.fi.bases.libreria.modelo.data.MetodoPago;
import java.util.ArrayList;

public interface MetodoPagoDAO {
    // Solo necesitamos consultar para llenar el ComboBox
    public ArrayList<MetodoPago> consultar();
}