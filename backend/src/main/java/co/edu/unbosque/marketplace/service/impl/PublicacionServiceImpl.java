package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.AnfitrionResponseDTO;
import co.edu.unbosque.marketplace.model.dto.PoliticaCancelacionDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionRequestDTO;
import co.edu.unbosque.marketplace.model.dto.PublicacionResponseDTO;
import co.edu.unbosque.marketplace.model.entity.Anfitrion;
import co.edu.unbosque.marketplace.model.entity.Publicacion;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticasCancelacion;
import co.edu.unbosque.marketplace.model.enums.EstadoPublicacion;
import co.edu.unbosque.marketplace.repository.AnfitrionRepository;
import co.edu.unbosque.marketplace.repository.PublicacionRepository;
import co.edu.unbosque.marketplace.repository.TipoPoliticaCancelacionRepository;
import co.edu.unbosque.marketplace.service.PublicacionService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PublicacionServiceImpl implements PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final AnfitrionRepository anfitrionRepository;
    private final TipoPoliticaCancelacionRepository politicaRepository;
    private final ModelMapper mapper = new ModelMapper();

    public PublicacionServiceImpl(PublicacionRepository publicacionRepository,
                                  AnfitrionRepository anfitrionRepository,
                                  TipoPoliticaCancelacionRepository politicaRepository) {
        this.publicacionRepository = publicacionRepository;
        this.anfitrionRepository = anfitrionRepository;
        this.politicaRepository = politicaRepository;

        // STRICT: solo copia campos con el nombre exacto. Con la estrategia por defecto, Anfitrion.idUsuario y
        // Anfitrion.usuario.idUsuario se confunden entre sí y el mapper falla al arrancar.
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // El mapper no toca lo que cambia de tipo (política: String en el DTO y objeto en la entidad) ni al
        // anfitrión (entidad en un lado y DTO en el otro): eso se asigna a mano.
        mapper.typeMap(PublicacionRequestDTO.class, Publicacion.class).addMappings(m -> {
            m.skip(Publicacion::setAnfitrion);
            m.skip(Publicacion::setPoliticaCancelacion);
        });
        // Aquí se crea el mapa vacío, se marcan los campos a omitir y solo después se piden los mapeos automáticos.
        // Con typeMap(...) los mapeos automáticos se hacen primero, ya cubren anfitrion.idUsuario y skip() falla.
        TypeMap<Publicacion, PublicacionResponseDTO> aRespuesta =
                mapper.emptyTypeMap(Publicacion.class, PublicacionResponseDTO.class);
        aRespuesta.addMappings(m -> {
            m.skip(PublicacionResponseDTO::setAnfitrion);
            m.skip(PublicacionResponseDTO::setPoliticaCancelacion);
        });
        aRespuesta.implicitMappings();
    }

    @Override
    public PublicacionResponseDTO crear(PublicacionRequestDTO dto) {
        Anfitrion anfitrion = obtenerAnfitrionHabilitado(dto.getIdAnfitrion());
        boolean conPolitica = !estaVacio(dto.getPoliticaCancelacion());
        // Solo vale una política con clase implementada y con su fila en la tabla (las mismas que lista listarPoliticas)
        if (conPolitica && (!PoliticasCancelacion.existe(dto.getPoliticaCancelacion())
                || !politicaRepository.existsById(dto.getPoliticaCancelacion()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La política de cancelación no existe: " + dto.getPoliticaCancelacion());
        }

        // El mapper copia los campos que se llaman igual; anfitrion y política se asignan a mano
        Publicacion publicacion = mapper.map(dto, Publicacion.class);
        publicacion.setAnfitrion(anfitrion);
        publicacion.setPoliticaCancelacion(conPolitica ? PoliticasCancelacion.deCodigo(dto.getPoliticaCancelacion()) : null);

        // Incompleta: queda en BORRADOR y se informa qué falta. Completa: pasa a revisión.
        List<String> faltantes = camposFaltantes(dto);
        publicacion.setEstado(faltantes.isEmpty() ? EstadoPublicacion.PENDIENTE_REVISION : EstadoPublicacion.BORRADOR);

        PublicacionResponseDTO respuesta = toResponseDTO(publicacionRepository.save(publicacion));
        respuesta.setCamposFaltantes(faltantes);
        return respuesta;
    }

    /**
     * Campos mínimos que no vienen (null o texto en blanco), con el nombre que se muestra al anfitrión.
     * Tarifa de limpieza y reglas son opcionales.
     */
    private List<String> camposFaltantes(PublicacionRequestDTO dto) {
        List<String> faltantes = new ArrayList<>();
        if (estaVacio(dto.getTitulo())) faltantes.add("Título");
        if (estaVacio(dto.getDescripcion())) faltantes.add("Descripción");
        if (estaVacio(dto.getUbicacion())) faltantes.add("Ubicación");
        if (estaVacio(dto.getCiudad())) faltantes.add("Ciudad");
        if (estaVacio(dto.getTipoAlojamiento())) faltantes.add("Tipo de alojamiento");
        if (dto.getCapacidad() == null) faltantes.add("Capacidad");
        if (dto.getPrecioPorNoche() == null) faltantes.add("Precio por noche");
        if (dto.getModoAceptacion() == null) faltantes.add("Modo de aceptación");
        if (estaVacio(dto.getPoliticaCancelacion())) faltantes.add("Política de cancelación");
        return faltantes;
    }

    private static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PoliticaCancelacionDTO> listarPoliticas() {
        return politicaRepository.findAll().stream()
                .filter(politica -> PoliticasCancelacion.existe(politica.getCodigo()))
                .map(politica -> mapper.map(politica, PoliticaCancelacionDTO.class))
                .toList();
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
        // Un borrador puede no tener política todavía
        dto.setPoliticaCancelacion(publicacion.getPoliticaCancelacion() == null
                ? null : publicacion.getPoliticaCancelacion().codigo());
        return dto;
    }
}
