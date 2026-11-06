package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.AnfitrionResponseDTO;
import co.edu.unbosque.marketplace.model.dto.PeriodoDisponibilidadRequestDTO;
import co.edu.unbosque.marketplace.model.dto.PeriodoDisponibilidadResponseDTO;
import co.edu.unbosque.marketplace.model.dto.PoliticaCancelacionDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionEditarRequestDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionResponseDTO;
import co.edu.unbosque.marketplace.model.entity.Anfitrion;
import co.edu.unbosque.marketplace.model.entity.PeriodoDisponibilidad;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticasCancelacion;
import co.edu.unbosque.marketplace.model.enums.EstadoPublicacion;
import co.edu.unbosque.marketplace.model.enums.TipoPeriodo;
import co.edu.unbosque.marketplace.repository.AdminRepository;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.PublicacionRepository;
import co.edu.unbosque.marketplace.repository.TipoPoliticaCancelacionRepository;
import co.edu.unbosque.marketplace.service.PublicacionService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class PublicacionServiceImpl implements PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final AnfitrionRepository anfitrionRepository;
    private final TipoPoliticaCancelacionRepository politicaRepository;
    private final AdminRepository adminRepository;
    private final ModelMapper mapper = new ModelMapper();

    public PublicacionServiceImpl(PublicacionRepository publicacionRepository,
                                  AnfitrionRepository anfitrionRepository,
                                  TipoPoliticaCancelacionRepository politicaRepository,
                                  AdminRepository adminRepository) {
        this.publicacionRepository = publicacionRepository;
        this.anfitrionRepository = anfitrionRepository;
        this.politicaRepository = politicaRepository;
        this.adminRepository = adminRepository;

        // STRICT: solo copia campos con el nombre exacto. Con la estrategia por defecto, Anfitrion.idUsuario y
        // Anfitrion.usuario.idUsuario se confunden entre sí y el mapper falla al arrancar.
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // El mapper no toca lo que cambia de tipo (política: String en el DTO y objeto en la entidad) ni al
        // anfitrión y los períodos (entidad en un lado y DTO en el otro): eso se asigna a mano.
        mapper.typeMap(PublicacionRequestDTO.class, Publicacion.class).addMappings(m -> {
            m.skip(Publicacion::setAnfitrion);
            m.skip(Publicacion::setPoliticaCancelacion);
            m.skip(Publicacion::setPeriodos);
        });
        // Aquí se crea el mapa vacío, se marcan los campos a omitir y solo después se piden los mapeos automáticos.
        // Con typeMap(...) los mapeos automáticos se hacen primero, ya cubren anfitrion.idUsuario y skip() falla.
        TypeMap<Publicacion, PublicacionResponseDTO> aRespuesta =
                mapper.emptyTypeMap(Publicacion.class, PublicacionResponseDTO.class);
        aRespuesta.addMappings(m -> {
            m.skip(PublicacionResponseDTO::setAnfitrion);
            m.skip(PublicacionResponseDTO::setPoliticaCancelacion);
            m.skip(PublicacionResponseDTO::setPeriodos);
        });
        aRespuesta.implicitMappings();
    }

    @Override
    public PublicacionResponseDTO crear(PublicacionRequestDTO dto) {
        Anfitrion anfitrion = obtenerAnfitrionHabilitado(dto.getIdAnfitrion());
        // Solo vale una política con clase implementada y con su fila en la tabla (las mismas que lista listarPoliticas)
        exigirPoliticaValida(dto.getPoliticaCancelacion());
        exigirPeriodosValidos(dto.getPeriodos());

        // El mapper copia los campos que se llaman igual; anfitrion, política y períodos se asignan a mano
        Publicacion publicacion = mapper.map(dto, Publicacion.class);
        publicacion.setAnfitrion(anfitrion);
        publicacion.setPoliticaCancelacion(PoliticasCancelacion.deCodigo(dto.getPoliticaCancelacion()));
        publicacion.setEstado(EstadoPublicacion.PENDIENTE_REVISION);
        dto.getPeriodos().forEach(periodo -> publicacion.getPeriodos().add(toPeriodoDisponible(periodo)));

        return toResponseDTO(publicacionRepository.save(publicacion));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PoliticaCancelacionDTO> listarPoliticas() {
        return politicaRepository.findAll().stream()
                .filter(politica -> PoliticasCancelacion.existe(politica.getCodigo()))
                .map(politica -> mapper.map(politica, PoliticaCancelacionDTO.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PublicacionResponseDTO obtenerParaEditar(Integer idPublicacion, Integer idUsuario) {
        return toResponseDTO(obtenerAutorizada(idPublicacion, idUsuario, "Edición no autorizada"));
    }

    @Override
    public PublicacionResponseDTO editar(Integer idPublicacion, PublicacionEditarRequestDTO dto) {
        Publicacion publicacion = obtenerAutorizada(idPublicacion, dto.getIdUsuario(), "Edición no autorizada");
        exigirPoliticaValida(dto.getPoliticaCancelacion());

        publicacion.setTitulo(dto.getTitulo());
        publicacion.setDescripcion(dto.getDescripcion());
        publicacion.setUbicacion(dto.getUbicacion());
        publicacion.setCiudad(dto.getCiudad());
        publicacion.setTipoAlojamiento(dto.getTipoAlojamiento());
        publicacion.setCapacidad(dto.getCapacidad());
        publicacion.setPrecioPorNoche(dto.getPrecioPorNoche());
        publicacion.setTarifaLimpieza(dto.getTarifaLimpieza());
        publicacion.setReglas(dto.getReglas());
        publicacion.setModoAceptacion(dto.getModoAceptacion());
        publicacion.setPoliticaCancelacion(PoliticasCancelacion.deCodigo(dto.getPoliticaCancelacion()));

        return toResponseDTO(publicacionRepository.save(publicacion));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicacionResponseDTO> listar(Integer idAnfitrion) {
        List<Publicacion> publicaciones = idAnfitrion == null
                ? publicacionRepository.findAll()
                : publicacionRepository.findByAnfitrion_IdUsuario(idAnfitrion);
        return publicaciones.stream().map(this::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PublicacionResponseDTO obtener(Integer idPublicacion) {
        return toResponseDTO(buscar(idPublicacion));
    }

    @Override
    public void eliminar(Integer idPublicacion, Integer idUsuario) {
        Publicacion publicacion = obtenerAutorizada(idPublicacion, idUsuario, "Eliminación no autorizada");
        try {
            publicacionRepository.delete(publicacion);
            publicacionRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar la publicación porque tiene datos asociados, como reservas");
        }
    }

    @Override
    public PublicacionResponseDTO pausar(Integer idPublicacion, Integer idUsuario) {
        Publicacion publicacion = obtenerAutorizada(idPublicacion, idUsuario,
                "Solo el anfitrión propietario o un administrador puede pausar la publicación");
        if (publicacion.getEstado() != EstadoPublicacion.ACTIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede pausar una publicación en estado " + publicacion.getEstado()
                            + ": solo se pueden pausar las publicaciones ACTIVAS");
        }
        publicacion.setEstado(EstadoPublicacion.PAUSADA);
        return toResponseDTO(publicacionRepository.save(publicacion));
    }

    @Override
    public void bloquearPeriodoReservado(Integer idPublicacion, LocalDate fechaLlegada, LocalDate fechaSalida) {
        if (!fechaSalida.isAfter(fechaLlegada)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de salida debe ser posterior a la fecha de llegada");
        }
        Publicacion publicacion = publicacionRepository.buscarParaBloquearPeriodos(idPublicacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La publicación no existe"));

        // Los períodos disponibles que tocan el rango, en orden de fecha
        List<PeriodoDisponibilidad> afectados = publicacion.getPeriodos().stream()
                .filter(p -> p.getTipo() == TipoPeriodo.DISPONIBLE
                        && p.getFechaInicio().isBefore(fechaSalida)
                        && p.getFechaFin().isAfter(fechaLlegada))
                .sorted(Comparator.comparing(PeriodoDisponibilidad::getFechaInicio))
                .toList();
        exigirRangoCubierto(afectados, fechaLlegada, fechaSalida);

        // Cada período afectado se reemplaza por lo que le sobra antes y después del rango reservado
        publicacion.getPeriodos().removeAll(afectados);
        for (PeriodoDisponibilidad periodo : afectados) {
            if (periodo.getFechaInicio().isBefore(fechaLlegada)) {
                publicacion.getPeriodos().add(
                        nuevoPeriodo(periodo.getFechaInicio(), fechaLlegada, TipoPeriodo.DISPONIBLE));
            }
            if (periodo.getFechaFin().isAfter(fechaSalida)) {
                publicacion.getPeriodos().add(
                        nuevoPeriodo(fechaSalida, periodo.getFechaFin(), TipoPeriodo.DISPONIBLE));
            }
        }
        publicacion.getPeriodos().add(nuevoPeriodo(fechaLlegada, fechaSalida, TipoPeriodo.RESERVADO));
        publicacionRepository.save(publicacion);
    }

    /** Los períodos, ya ordenados, deben cubrir todas las noches del rango sin dejar huecos. */
    private void exigirRangoCubierto(List<PeriodoDisponibilidad> periodos, LocalDate fechaLlegada, LocalDate fechaSalida) {
        LocalDate cubiertoHasta = fechaLlegada;
        for (PeriodoDisponibilidad periodo : periodos) {
            if (periodo.getFechaInicio().isAfter(cubiertoHasta)) {
                break;
            }
            cubiertoHasta = periodo.getFechaFin();
        }
        if (cubiertoHasta.isBefore(fechaSalida)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Las fechas seleccionadas no están disponibles");
        }
    }

    private Publicacion buscar(Integer idPublicacion) {
        return publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La publicación no existe"));
    }

    private void exigirPoliticaValida(String codigo) {
        if (!PoliticasCancelacion.existe(codigo) || !politicaRepository.existsById(codigo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La política de cancelación no existe: " + codigo);
        }
    }

    /**
     * Cada rango debe terminar después de empezar y no estar en el pasado, y los rangos no se pueden cruzar. Dos rangos
     * pueden compartir un día: la fecha de fin es la de salida, así que esa noche ya no hace parte del primero.
     */
    private void exigirPeriodosValidos(List<PeriodoDisponibilidadRequestDTO> periodos) {
        LocalDate hoy = LocalDate.now();
        for (PeriodoDisponibilidadRequestDTO periodo : periodos) {
            if (!periodo.getFechaFin().isAfter(periodo.getFechaInicio())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La fecha de fin debe ser posterior a la fecha de inicio");
            }
            if (periodo.getFechaInicio().isBefore(hoy)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La fecha de inicio no puede estar en el pasado");
            }
        }

        List<PeriodoDisponibilidadRequestDTO> ordenados = periodos.stream()
                .sorted(Comparator.comparing(PeriodoDisponibilidadRequestDTO::getFechaInicio))
                .toList();
        for (int i = 1; i < ordenados.size(); i++) {
            if (ordenados.get(i).getFechaInicio().isBefore(ordenados.get(i - 1).getFechaFin())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los períodos no se pueden cruzar entre sí");
            }
        }
    }

    private PeriodoDisponibilidad toPeriodoDisponible(PeriodoDisponibilidadRequestDTO dto) {
        return nuevoPeriodo(dto.getFechaInicio(), dto.getFechaFin(), TipoPeriodo.DISPONIBLE);
    }

    private PeriodoDisponibilidad nuevoPeriodo(LocalDate fechaInicio, LocalDate fechaFin, TipoPeriodo tipo) {
        PeriodoDisponibilidad periodo = new PeriodoDisponibilidad();
        periodo.setFechaInicio(fechaInicio);
        periodo.setFechaFin(fechaFin);
        periodo.setTipo(tipo);
        return periodo;
    }

    private Publicacion obtenerAutorizada(Integer idPublicacion, Integer idUsuario, String mensajeSiNoAutorizado) {
        Publicacion publicacion = buscar(idPublicacion);

        if (!esPropietario(publicacion, idUsuario) && !adminRepository.existsById(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, mensajeSiNoAutorizado);
        }
        return publicacion;
    }

    private boolean esPropietario(Publicacion publicacion, Integer idUsuario) {
        Anfitrion propietario = publicacion.getAnfitrion();
        return propietario.getIdUsuario().equals(idUsuario)
                && propietario.getUsuario().estaActiva()
                && !Boolean.TRUE.equals(propietario.getUsuario().getRestringido());
    }

    /**
     * Solo un anfitrión con la cuenta activa y sin restricciones puede crear publicaciones.
     */
    private Anfitrion obtenerAnfitrionHabilitado(Integer idAnfitrion) {
        return anfitrionRepository.findById(idAnfitrion)
                .filter(a -> a.getUsuario().estaActiva() && !Boolean.TRUE.equals(a.getUsuario().getRestringido()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Solo un anfitrión puede crear publicaciones"));
    }

    private PublicacionResponseDTO toResponseDTO(Publicacion publicacion) {
        PublicacionResponseDTO dto = mapper.map(publicacion, PublicacionResponseDTO.class);
        dto.setAnfitrion(CuentaUsuario.aRespuesta(publicacion.getAnfitrion().getUsuario(), new AnfitrionResponseDTO()));
        dto.setPoliticaCancelacion(publicacion.getPoliticaCancelacion().codigo());
        dto.setPeriodos(publicacion.getPeriodos().stream()
                .map(periodo -> mapper.map(periodo, PeriodoDisponibilidadResponseDTO.class))
                .toList());
        return dto;
    }
}
