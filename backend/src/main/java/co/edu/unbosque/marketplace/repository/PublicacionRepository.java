package co.edu.unbosque.marketplace.repository;

import co.edu.unbosque.marketplace.model.entity.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {

    List<Publicacion> findByAnfitrion_IdUsuario(Integer idUsuario);
}
