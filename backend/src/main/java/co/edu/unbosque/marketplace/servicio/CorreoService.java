package co.edu.unbosque.marketplace.servicio;

public interface CorreoService {

    void enviarCodigoRecuperacion(String destinatario, String nombre, String codigo, long minutosExpiracion);
}
