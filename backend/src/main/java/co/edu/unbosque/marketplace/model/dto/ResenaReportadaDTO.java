package co.edu.unbosque.marketplace.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/** Reseña con reportes pendientes, tal como la ve el administrador para moderarla. */
@Getter
@Setter
public class ResenaReportadaDTO {

    private ResenaResponseDTO resena;
    private Integer numeroReportes;
    private List<String> motivos;
    private LocalDateTime ultimoReporte;
}
