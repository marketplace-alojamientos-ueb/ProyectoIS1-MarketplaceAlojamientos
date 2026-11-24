package co.edu.unbosque.marketplace.repository;

import co.edu.unbosque.marketplace.model.entity.Reserva;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    List<Reserva> findByPublicacion_Anfitrion_IdUsuarioAndEstadoOrderByFechaLlegadaAsc(Integer idAnfitrion,
                                                                                         EstadoReserva estado);

    List<Reserva> findByHuesped_IdUsuarioOrderByFechaLlegadaDesc(Integer idHuesped);

    List<Reserva> findByPublicacion_Anfitrion_IdUsuarioOrderByFechaLlegadaDesc(Integer idAnfitrion);

    Optional<Reserva> findByClaveIdempotencia(String claveIdempotencia);
}
