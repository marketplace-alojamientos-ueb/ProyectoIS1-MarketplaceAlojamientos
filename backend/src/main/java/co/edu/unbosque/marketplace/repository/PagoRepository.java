package co.edu.unbosque.marketplace.repository;

import co.edu.unbosque.marketplace.model.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    Optional<Pago> findFirstByReserva_IdReservaAndTipoMovimiento(Integer idReserva, String tipoMovimiento);
}
