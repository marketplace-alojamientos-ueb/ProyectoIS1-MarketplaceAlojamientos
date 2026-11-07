package co.edu.unbosque.marketplace.repository;

import co.edu.unbosque.marketplace.model.entity.Publicacion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PublicacionRepository extends JpaRepository<Publicacion, Integer>, JpaSpecificationExecutor<Publicacion> {

    List<Publicacion> findByAnfitrion_IdUsuario(Integer idUsuario);

    /**
     * Trae la publicación bloqueando su fila hasta que termine la transacción: si dos reservas piden las mismas fechas
     * a la vez, la segunda espera y ya ve los períodos que dejó la primera.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Publicacion p where p.idPublicacion = :idPublicacion")
    Optional<Publicacion> buscarParaBloquearPeriodos(@Param("idPublicacion") Integer idPublicacion);
}
