package Taller6.Taller6ArgumentCaptor1.services;

import Taller6.Taller6ArgumentCaptor1.models.Pedido;

public class PedidoService {
    private EmailService emailService;
    private LoggingService loggingService;
    private HistorialPedidosService historialPedidosService;

    public PedidoService(EmailService emailService, LoggingService loggingService, HistorialPedidosService historialPedidosService) {
        this.emailService = emailService;
        this.loggingService = loggingService;
        this.historialPedidosService = historialPedidosService;
    }

    public void procesarPedido(String cliente, Pedido pedido, String tipoCliente) {
        if (pedido.getProductos().isEmpty()) {
            loggingService.registrarEvento("Pedido vacío para " + cliente);
            emailService.enviarEmail(cliente, "Confirmación de tu pedido", "Estimado " + cliente + ", tu pedido está vacío. Total: $0.0");
            return; // Salir si el pedido está vacío
        }

        // Procesar el pedido y calcular el total (se asume que el precio de los productos es siempre 10)
        double total = pedido.getProductos().size() * 10.0;

        // Registrar evento de procesamiento del pedido con el total
        String logMensaje = "Pedido procesado para " + cliente + " con total $" + total;
        loggingService.registrarEvento(logMensaje);

        // Aplicar descuento y luego registrar el evento del descuento
        double descuento = aplicarDescuento(tipoCliente, total);
        pedido.setTotal(total - descuento);

        // Enviar correo de confirmación
        String asunto = "Confirmación de tu pedido";
        String cuerpo = "Estimado " + cliente + ", tu pedido ha sido procesado. Total: $" + pedido.getTotal();

        try {
            emailService.enviarEmail(cliente, asunto, cuerpo);
        } catch (Exception e) {
            loggingService.registrarError("Error al enviar email a " + cliente);
            return; // Salir si ocurre un error al enviar el email
        }

        // Intentar actualizar historial de pedidos
        try {
            historialPedidosService.actualizarHistorial(cliente, pedido);
            loggingService.registrarEvento("Historial actualizado para cliente " + cliente);
        } catch (Exception e) {
            loggingService.registrarError("Error al actualizar el historial para cliente " + cliente);
        }
    }

    private double aplicarDescuento(String tipoCliente, double total) {
        if ("VIP".equalsIgnoreCase(tipoCliente)) {
            loggingService.registrarEvento("Descuento aplicado para cliente VIP");
            return total * 0.1;  // 10% de descuento
        } else {
            return 0;
        }
    }
}