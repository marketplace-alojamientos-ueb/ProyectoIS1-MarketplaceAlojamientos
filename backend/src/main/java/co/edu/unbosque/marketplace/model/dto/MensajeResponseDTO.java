package co.edu.unbosque.marketplace.model.dto;

/** Respuesta simple con un mensaje para mostrar al usuario. */
public class MensajeResponseDTO {

    private String mensaje;

    public MensajeResponseDTO() {}

    public MensajeResponseDTO(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
