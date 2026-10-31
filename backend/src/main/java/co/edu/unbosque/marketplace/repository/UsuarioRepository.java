package co.edu.unbosque.marketplace.repository;

import co.edu.unbosque.marketplace.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByCedula(String cedula);

    boolean existsByCorreoAndIdUsuarioNot(String correo, Integer idUsuario);
}
