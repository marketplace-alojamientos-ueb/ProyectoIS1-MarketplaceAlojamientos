package co.edu.unbosque.marketplace.repository;

import co.edu.unbosque.marketplace.model.entity.Anfitrion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AnfitrionRepository extends JpaRepository<Anfitrion, Integer> {

    Optional<Anfitrion> findByUsuario_Correo(String correo);
}
