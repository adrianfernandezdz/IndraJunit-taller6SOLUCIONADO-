package Taller6.Taller6ArgumentCaptor1.tests;

import java.util.List;

import Taller6.Taller6ArgumentCaptor1.models.Pedido;
import Taller6.Taller6ArgumentCaptor1.services.EmailService;
import Taller6.Taller6ArgumentCaptor1.services.HistorialPedidosService;
import Taller6.Taller6ArgumentCaptor1.services.LoggingService;
import Taller6.Taller6ArgumentCaptor1.services.PedidoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private LoggingService loggingService;

    @Mock
    private HistorialPedidosService historialPedidosService;

    @InjectMocks
    private PedidoService pedidoService;

    //Este test testeará un pedido con descuento procesado correctamente y:
    //1. Verificará que s eha llamado a enviarEmail(), y capturará los argumentos usados, y los comprobará con unos asserts.


    //2. Verificará el número de veces que se ha llamado al loginService.registrarEvento, capturará el argumento cada vez (1 por vez)
    // y los comprobará con unos asserts.

    //3. Verificará que se ha llamado a actualizar historial, se capturarán sus argumentos y los comprobará con unos asserts.
    @Test
    public void testProcesarPedido_conDescuentoYHistorialExitoso() throws Exception {
        // Preparar el pedido y el cliente
        Pedido pedido = new Pedido(List.of("Pizza", "Refresco"));
        String cliente = "cliente_vip@example.com";

        // Procesar el pedido para un cliente VIP
        pedidoService.procesarPedido(cliente, pedido, "VIP");

        // 1. Capturar los argumentos para el método enviarEmail
        ArgumentCaptor<String> captorDestinatario = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> captorAsunto = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> captorCuerpo = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviarEmail(captorDestinatario.capture(), captorAsunto.capture(), captorCuerpo.capture());
        assertEquals("cliente_vip@example.com", captorDestinatario.getValue());
        assertEquals("Confirmación de tu pedido", captorAsunto.getValue());
        assertEquals("Estimado cliente_vip@example.com, tu pedido ha sido procesado. Total: $18.0", captorCuerpo.getValue());

        // 2. Capturar los eventos registrados en loggingService
        ArgumentCaptor<String> captorLogMensaje = ArgumentCaptor.forClass(String.class);
        verify(loggingService, times(3)).registrarEvento(captorLogMensaje.capture());
        List<String> mensajesLog = captorLogMensaje.getAllValues();
        assertEquals("Pedido procesado para cliente_vip@example.com con total $20.0", mensajesLog.get(0));
        assertEquals("Descuento aplicado para cliente VIP", mensajesLog.get(1));
        assertEquals("Historial actualizado para cliente cliente_vip@example.com", mensajesLog.get(2));

        // 3. Capturar los argumentos para el método actualizarHistorial
        ArgumentCaptor<String> captorClienteHistorial = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Pedido> captorPedidoHistorial = ArgumentCaptor.forClass(Pedido.class);
        verify(historialPedidosService).actualizarHistorial(captorClienteHistorial.capture(), captorPedidoHistorial.capture());
        assertEquals("cliente_vip@example.com", captorClienteHistorial.getValue());
        assertEquals(18.0, captorPedidoHistorial.getValue().getTotal());
    }

    //Este test testeará un pedido con error en el historial
    //1. Se validará que se lanza una excepción al actualizar Historial.

    //2. Se capturará el argumento para registrarError y se validarará.

    //3. Se verificará que nunca se llamó exitosamente a registrarEvento con ... "Historial actualizado para cliente ..."
    @Test
    public void testProcesarPedido_conErrorEnHistorial() throws Exception {
        // Preparar el pedido y el cliente problemático
        Pedido pedido = new Pedido(List.of("Pizza", "Refresco"));
        String cliente = "cliente_problematico@example.com";

        // Simular excepción cuando se actualiza el historial
        // se recomienda usar doThrow cuando el método que lanza la excepción es void. En el resto de casos, podríamos usar when.thenThrow().
        doThrow(new Exception("Error al actualizar el historial")).when(historialPedidosService).actualizarHistorial(anyString(), any(Pedido.class));

        // Procesar el pedido
        pedidoService.procesarPedido(cliente, pedido, "Normal");

        // Capturar el evento de error registrado
        ArgumentCaptor<String> captorError = ArgumentCaptor.forClass(String.class);
        verify(loggingService).registrarError(captorError.capture());
        assertEquals("Error al actualizar el historial para cliente cliente_problematico@example.com", captorError.getValue());

        // Validar que no se registró el mensaje de historial actualizado
        verify(loggingService, never()).registrarEvento("Historial actualizado para cliente cliente_problematico@example.com");
    }

    //Este test testeará un pedido sin descuento procesado correctamente y:
    //1. Verificará que s eha llamado a enviarEmail(), y capturará los argumentos usados, y los comprobará con unos asserts.
    //2. Verificará el número de veces que se ha llamado al loginService.registrarEvento, capturará el argumento cada vez (1 por vez)
    // y los comprobará con unos asserts.
    @Test
    public void testProcesarPedido_sinDescuento() throws Exception {
        // Preparar el pedido y el cliente
        Pedido pedido = new Pedido(List.of("Pizza", "Refresco"));
        String cliente = "cliente_normal@example.com";

        // Procesar el pedido para un cliente normal
        pedidoService.procesarPedido(cliente, pedido, "Normal");

        // Verificar que no se aplicó descuento
        ArgumentCaptor<String> captorDestinatario = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> captorAsunto = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> captorCuerpo = ArgumentCaptor.forClass(String.class);

        verify(emailService).enviarEmail(captorDestinatario.capture(), captorAsunto.capture(), captorCuerpo.capture());
        assertEquals("cliente_normal@example.com", captorDestinatario.getValue());
        assertEquals("Confirmación de tu pedido", captorAsunto.getValue());
        assertEquals("Estimado cliente_normal@example.com, tu pedido ha sido procesado. Total: $20.0", captorCuerpo.getValue());

        // Capturar los eventos de logging
        ArgumentCaptor<String> captorLogMensaje = ArgumentCaptor.forClass(String.class);
        verify(loggingService, times(2)).registrarEvento(captorLogMensaje.capture());
        List<String> mensajesLog = captorLogMensaje.getAllValues();
        assertEquals("Pedido procesado para cliente_normal@example.com con total $20.0", mensajesLog.get(0));
        assertEquals("Historial actualizado para cliente cliente_normal@example.com", mensajesLog.get(1));
    }


    // Este test validará múltiples pedidos
    //1. verificaremos cuántas veces se llama a enviarEmail, Capturaremos los argumentos de todas las veces, y los validaremos.
    //2. verificaremos cuántas veces se llama a registrarEvento, Capturaremos los argumentos de todas las veces, y los validaremos.
    @Test
    public void testProcesarMultiplesPedidos() throws Exception {
        // Preparar pedidos y cliente
        Pedido pedido1 = new Pedido(List.of("Hamburguesa"));
        Pedido pedido2 = new Pedido(List.of("Pizza", "Refresco"));
        String cliente = "cliente_frecuente@example.com";

        // Procesar dos pedidos
        pedidoService.procesarPedido(cliente, pedido1, "Normal");
        pedidoService.procesarPedido(cliente, pedido2, "Normal");

        // Capturar los correos electrónicos enviados
        ArgumentCaptor<String> captorDestinatario = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> captorCuerpo = ArgumentCaptor.forClass(String.class);

        verify(emailService, times(2)).enviarEmail(captorDestinatario.capture(), anyString(), captorCuerpo.capture());
        List<String> destinatarios = captorDestinatario.getAllValues();
        List<String> cuerpos = captorCuerpo.getAllValues();

        assertEquals("cliente_frecuente@example.com", destinatarios.get(0));
        assertEquals("Estimado cliente_frecuente@example.com, tu pedido ha sido procesado. Total: $10.0", cuerpos.get(0));
        assertEquals("cliente_frecuente@example.com", destinatarios.get(1));
        assertEquals("Estimado cliente_frecuente@example.com, tu pedido ha sido procesado. Total: $20.0", cuerpos.get(1));

        // Verificar los logs
        ArgumentCaptor<String> captorLogMensaje = ArgumentCaptor.forClass(String.class);
        verify(loggingService, times(4)).registrarEvento(captorLogMensaje.capture());
    }


    // Este test validará un pedido vacio
    //1. Verificaremos los argumentos del email
    //2. Veriricaremos los argumentos del registrarEvento.
    @Test
    public void testProcesarPedido_vacio() throws Exception {
        // Preparar un pedido vacío
        Pedido pedidoVacio = new Pedido(List.of());
        String cliente = "cliente_sin_pedido@example.com";

        // Procesar el pedido vacío
        pedidoService.procesarPedido(cliente, pedidoVacio, "Normal");

        // Verificar que el total es 0
        ArgumentCaptor<String> captorCuerpo = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviarEmail(anyString(), anyString(), captorCuerpo.capture());
        assertTrue(captorCuerpo.getValue().contains("Total: $0.0"));

        // Verificar logs
        ArgumentCaptor<String> captorLogMensaje = ArgumentCaptor.forClass(String.class);
        verify(loggingService).registrarEvento(captorLogMensaje.capture());
        assertEquals("Pedido vacío para cliente_sin_pedido@example.com", captorLogMensaje.getValue());
    }


    //Este test testeará un pedido con error al mandar el email
    //1. Se validará la excepción lanzada al enviarEmail
    //2. Se capturará y verificará el mensaje utilizado en registrarError
    @Test
    public void testErrorEnEnvioDeEmail() throws Exception {
        // Preparar pedido y cliente
        Pedido pedido = new Pedido(List.of("Pizza", "Refresco"));
        String cliente = "cliente_con_error@example.com";

        // Simular excepción en el envío de email
        doThrow(new RuntimeException("Error en el servicio de email")).when(emailService).enviarEmail(anyString(), anyString(), anyString());

        // Procesar el pedido
        pedidoService.procesarPedido(cliente, pedido, "Normal");

        // Verificar que se capturó el error en los logs
        ArgumentCaptor<String> captorError = ArgumentCaptor.forClass(String.class);
        verify(loggingService).registrarError(captorError.capture());
        assertEquals("Error al enviar email a cliente_con_error@example.com", captorError.getValue());
    }
}
