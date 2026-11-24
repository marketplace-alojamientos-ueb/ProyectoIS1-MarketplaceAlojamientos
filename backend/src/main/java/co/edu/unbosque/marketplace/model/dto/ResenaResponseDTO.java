package co.edu.unbosque.marketplace.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ResenaResponseDTO {

    private Integer idResena;
    private Integer idReserva;
    private Integer idPublicacion;
    private String tituloPublicacion;
    private Integer idHuesped;
    private String nombreHuesped;
    private Integer calificacion;
    private String comentario;
    private LocalDate fecha;
    private Boolean visible;
}
