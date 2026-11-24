package co.edu.unbosque.marketplace.service;

import co.edu.unbosque.marketplace.model.dto.EventoAuditoriaDTO;

import java.util.List;

public interface AuditoriaService {

    List<EventoAuditoriaDTO> listarEventos(Integer idAdmin, String operacion);
}
