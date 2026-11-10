package co.edu.unbosque.marketplace.service;

import co.edu.unbosque.marketplace.model.dto.ReservaPendienteDTO;

import java.util.List;

public interface ReservaService {

    List<ReservaPendienteDTO> listarPendientes(Integer idAnfitrion);
}
