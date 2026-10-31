package co.edu.unbosque.marketplace.service;

public interface CorreoService {

    void enviarCodigoRecuperacion(String destinatario, String nombre, String codigo, long minutosExpiracion);
}
