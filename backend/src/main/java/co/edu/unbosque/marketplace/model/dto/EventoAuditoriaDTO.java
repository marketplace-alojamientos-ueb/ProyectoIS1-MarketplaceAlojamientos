package co.edu.unbosque.marketplace.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventoAuditoriaDTO {

    private Integer idEvento;
    private Integer idUsuario;
    private String operacion;
    private String entidadAfectada;
    private Integer idEntidad;
    private LocalDateTime fecha;
    private String resultado;
    private String detalle;
}
