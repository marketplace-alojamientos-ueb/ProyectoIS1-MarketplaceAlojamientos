package co.edu.unbosque.marketplace.repository;

import co.edu.unbosque.marketplace.model.entity.Huesped;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HuespedRepository extends JpaRepository<Huesped, Integer> {

    Optional<Huesped> findByUsuario_Correo(String correo);
}
