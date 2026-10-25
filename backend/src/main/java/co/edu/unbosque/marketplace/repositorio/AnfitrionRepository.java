package co.edu.unbosque.marketplace.repositorio;

import co.edu.unbosque.marketplace.modelo.entidad.Anfitrion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AnfitrionRepository extends JpaRepository<Anfitrion, Long> {

    Optional<Anfitrion> findByUsuario_Email(String email);
}
