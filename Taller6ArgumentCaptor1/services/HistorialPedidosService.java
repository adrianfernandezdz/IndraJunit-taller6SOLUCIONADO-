package Taller6.Taller6ArgumentCaptor1.services;

import Taller6.Taller6ArgumentCaptor1.models.Pedido;

public class HistorialPedidosService {
    public void actualizarHistorial(String cliente, Pedido pedido) throws Exception {
        // Simulación de actualización de historial con posibilidad de excepción
        if (cliente.equals("cliente_problematico@example.com")) {
            throw new Exception("Error al actualizar el historial");
        }
    }
}