package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.EventoAuditoriaDTO;
import co.edu.unbosque.marketplace.model.entity.EventoAuditoria;
import co.edu.unbosque.marketplace.repository.AdminRepository;
import co.edu.unbosque.marketplace.repository.EventoAuditoriaRepository;
import co.edu.unbosque.marketplace.service.AuditoriaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuditoriaServiceImpl implements AuditoriaService {

    private final EventoAuditoriaRepository eventoRepository;
    private final AdminRepository adminRepository;

    public AuditoriaServiceImpl(EventoAuditoriaRepository eventoRepository, AdminRepository adminRepository) {
        this.eventoRepository = eventoRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public List<EventoAuditoriaDTO> listarEventos(Integer idAdmin, String operacion) {
        adminRepository.findById(idAdmin)
                .filter(a -> a.getUsuario().estaActiva() && !Boolean.TRUE.equals(a.getUsuario().getRestringido()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Solo un administrador puede consultar la auditoría"));
        List<EventoAuditoria> eventos = operacion == null || operacion.isBlank()
                ? eventoRepository.findAllByOrderByFechaDescIdEventoDesc()
                : eventoRepository.findByOperacionOrderByFechaDescIdEventoDesc(operacion.trim());
        return eventos.stream().map(this::toDTO).toList();
    }

    private EventoAuditoriaDTO toDTO(EventoAuditoria evento) {
        EventoAuditoriaDTO dto = new EventoAuditoriaDTO();
        dto.setIdEvento(evento.getIdEvento());
        dto.setIdUsuario(evento.getUsuario() == null ? null : evento.getUsuario().getIdUsuario());
        dto.setOperacion(evento.getOperacion());
        dto.setEntidadAfectada(evento.getEntidadAfectada());
        dto.setIdEntidad(evento.getIdEntidad());
        dto.setFecha(evento.getFecha());
        dto.setResultado(evento.getResultado());
        dto.setDetalle(evento.getDetalle());
        return dto;
    }
}
