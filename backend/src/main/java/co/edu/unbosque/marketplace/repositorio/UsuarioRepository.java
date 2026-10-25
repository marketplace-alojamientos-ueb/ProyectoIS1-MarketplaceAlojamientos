package co.edu.unbosque.marketplace.repositorio;

import co.edu.unbosque.marketplace.modelo.entidad.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndCedulaNot(String email, Long cedula);
}
