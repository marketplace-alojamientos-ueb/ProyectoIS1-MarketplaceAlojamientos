package co.edu.unbosque.marketplace.repository;

import co.edu.unbosque.marketplace.model.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Integer> {

    boolean existsByReserva_IdReserva(Integer idReserva);

    List<Resena> findByReserva_Publicacion_IdPublicacionAndVisibleTrueOrderByFechaDescIdResenaDesc(Integer idPublicacion);

    List<Resena> findByReserva_Huesped_IdUsuarioOrderByFechaDescIdResenaDesc(Integer idHuesped);
}
