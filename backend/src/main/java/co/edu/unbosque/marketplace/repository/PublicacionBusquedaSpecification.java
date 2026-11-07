package co.edu.unbosque.marketplace.repository;

import co.edu.unbosque.marketplace.model.dto.PublicacionBusquedaRequestDTO;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.Servicio;
import co.edu.unbosque.marketplace.model.enums.EstadoPublicacion;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Arma la consulta de la búsqueda: solo publicaciones ACTIVAS y los filtros que vengan informados. */
public final class PublicacionBusquedaSpecification {

    private PublicacionBusquedaSpecification() {
    }

    public static Specification<Publicacion> de(PublicacionBusquedaRequestDTO filtros) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            condiciones.add(cb.equal(root.get("estado"), EstadoPublicacion.ACTIVA));

            if (tieneTexto(filtros.getCiudad())) {
                condiciones.add(cb.equal(cb.lower(root.get("ciudad")), filtros.getCiudad().trim().toLowerCase()));
            }
            if (tieneTexto(filtros.getTipo())) {
                condiciones.add(cb.equal(cb.lower(root.get("tipoAlojamiento")), filtros.getTipo().trim().toLowerCase()));
            }
            if (filtros.getCapacidad() != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("capacidad"), filtros.getCapacidad()));
            }
            if (filtros.getPrecioMin() != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("precioPorNoche"), filtros.getPrecioMin()));
            }
            if (filtros.getPrecioMax() != null) {
                condiciones.add(cb.lessThanOrEqualTo(root.get("precioPorNoche"), filtros.getPrecioMax()));
            }

            Set<String> servicios = nombresDeServicios(filtros.getServicios());
            if (!servicios.isEmpty()) {
                Subquery<Long> coincidencias = query.subquery(Long.class);
                var publicacion = coincidencias.correlate(root);
                Join<Publicacion, Servicio> servicio = publicacion.join("servicios");
                coincidencias.select(cb.countDistinct(servicio.get("idServicio")))
                        .where(cb.lower(servicio.<String>get("nombre")).in(servicios));
                condiciones.add(cb.equal(coincidencias, (long) servicios.size()));
            }

            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }

    private static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    private static Set<String> nombresDeServicios(List<String> servicios) {
        if (servicios == null) {
            return Set.of();
        }
        return servicios.stream()
                .filter(PublicacionBusquedaSpecification::tieneTexto)
                .map(nombre -> nombre.trim().toLowerCase())
                .collect(Collectors.toSet());
    }
}
