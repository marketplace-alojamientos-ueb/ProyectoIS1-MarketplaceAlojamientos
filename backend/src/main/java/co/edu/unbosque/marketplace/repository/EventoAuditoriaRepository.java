package co.edu.unbosque.marketplace.repository;

import co.edu.unbosque.marketplace.model.entity.EventoAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoAuditoriaRepository extends JpaRepository<EventoAuditoria, Integer> {

    List<EventoAuditoria> findAllByOrderByFechaDescIdEventoDesc();

    List<EventoAuditoria> findByOperacionOrderByFechaDescIdEventoDesc(String operacion);

    List<EventoAuditoria> findByEntidadAfectadaOrderByIdEventoAsc(String entidadAfectada);

    List<EventoAuditoria> findByEntidadAfectadaAndIdEntidadOrderByIdEventoAsc(String entidadAfectada, Integer idEntidad);
}
