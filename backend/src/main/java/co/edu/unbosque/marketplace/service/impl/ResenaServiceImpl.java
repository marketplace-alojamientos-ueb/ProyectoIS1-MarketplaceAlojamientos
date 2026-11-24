package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.MensajeResponseDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaActualizarDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaModerarDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaReportadaDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaReportarDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaRequestDTO;
import co.edu.unbosque.marketplace.model.dto.ResenaResponseDTO;
import co.edu.unbosque.marketplace.model.entity.AccionModeracion;
import co.edu.unbosque.marketplace.model.entity.Admin;
import co.edu.unbosque.marketplace.model.entity.EventoAuditoria;
import co.edu.unbosque.marketplace.model.entity.Huesped;
import co.edu.unbosque.marketplace.model.entity.Notificacion;
import co.edu.unbosque.marketplace.model.entity.Resena;
import co.edu.unbosque.marketplace.model.entity.Reserva;
import co.edu.unbosque.marketplace.model.entity.Usuario;
import co.edu.unbosque.marketplace.model.enums.EstadoReserva;
import co.edu.unbosque.marketplace.repository.AccionModeracionRepository;
import co.edu.unbosque.marketplace.repository.AdminRepository;
import co.edu.unbosque.marketplace.repository.EventoAuditoriaRepository;
import co.edu.unbosque.marketplace.repository.HuespedRepository;
import co.edu.unbosque.marketplace.repository.NotificacionRepository;
import co.edu.unbosque.marketplace.repository.ResenaRepository;
import co.edu.unbosque.marketplace.repository.ReservaRepository;
import co.edu.unbosque.marketplace.repository.UsuarioRepository;
import co.edu.unbosque.marketplace.service.ResenaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ResenaServiceImpl implements ResenaService {

    private final ResenaRepository resenaRepository;
    private final ReservaRepository reservaRepository;
    private final HuespedRepository huespedRepository;
    private final UsuarioRepository usuarioRepository;
    private final AdminRepository adminRepository;
    private final AccionModeracionRepository accionModeracionRepository;
    private final EventoAuditoriaRepository eventoAuditoriaRepository;
    private final NotificacionRepository notificacionRepository;

    public ResenaServiceImpl(ResenaRepository resenaRepository, ReservaRepository reservaRepository,
                             HuespedRepository huespedRepository, UsuarioRepository usuarioRepository,
                             AdminRepository adminRepository, AccionModeracionRepository accionModeracionRepository,
                             EventoAuditoriaRepository eventoAuditoriaRepository,
                             NotificacionRepository notificacionRepository) {
        this.resenaRepository = resenaRepository;
        this.reservaRepository = reservaRepository;
        this.huespedRepository = huespedRepository;
        this.usuarioRepository = usuarioRepository;
        this.adminRepository = adminRepository;
        this.accionModeracionRepository = accionModeracionRepository;
        this.eventoAuditoriaRepository = eventoAuditoriaRepository;
        this.notificacionRepository = notificacionRepository;
    }

    @Override
    public ResenaResponseDTO crear(ResenaRequestDTO dto) {
        Huesped huesped = huespedActivo(dto.getIdHuesped(), "Solo un huésped con la cuenta activa puede dejar una reseña");
        Reserva reserva = reservaRepository.findById(dto.getIdReserva())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La reserva no existe"));
        if (!reserva.getHuesped().getIdUsuario().equals(huesped.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el huésped que hizo la reserva puede dejar la reseña");
        }
        if (!estanciaCompletada(reserva)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo puedes dejar una reseña cuando la estancia se haya completado");
        }
        if (resenaRepository.existsByReserva_IdReserva(reserva.getIdReserva())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya dejaste una reseña para esta reserva");
        }

        Resena resena = new Resena();
        resena.setReserva(reserva);
        resena.setCalificacion(dto.getCalificacion());
        resena.setComentario(limpiarComentario(dto.getComentario()));
        resena.setFecha(LocalDate.now());
        resena.setVisible(true);
        return toResponseDTO(resenaRepository.save(resena));
    }

    @Override
    @Transactional(readOnly = true)
    public ResenaResponseDTO obtener(Integer idResena) {
        Resena resena = buscar(idResena);
        if (!Boolean.TRUE.equals(resena.getVisible())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La reseña no existe");
        }
        return toResponseDTO(resena);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResenaResponseDTO> listarPorPublicacion(Integer idPublicacion) {
        return resenaRepository
                .findByReserva_Publicacion_IdPublicacionAndVisibleTrueOrderByFechaDescIdResenaDesc(idPublicacion)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResenaResponseDTO> listarPorHuesped(Integer idHuesped) {
        huespedActivo(idHuesped, "Solo un huésped con la cuenta activa puede consultar sus reseñas");
        return resenaRepository.findByReserva_Huesped_IdUsuarioOrderByFechaDescIdResenaDesc(idHuesped)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public ResenaResponseDTO actualizar(Integer idResena, ResenaActualizarDTO dto) {
        Resena resena = resenaDelAutor(idResena, dto.getIdHuesped(), "Solo el autor puede editar la reseña");
        resena.setCalificacion(dto.getCalificacion());
        resena.setComentario(limpiarComentario(dto.getComentario()));
        return toResponseDTO(resenaRepository.save(resena));
    }

    @Override
    public MensajeResponseDTO eliminar(Integer idResena, Integer idHuesped) {
        Resena resena = resenaDelAutor(idResena, idHuesped, "Solo el autor puede eliminar la reseña");
        resenaRepository.delete(resena);
        return mensaje("La reseña fue eliminada");
    }

    // ---------- Moderación (MA-82) ----------

    @Override
    public MensajeResponseDTO reportar(Integer idResena, ResenaReportarDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .filter(u -> u.estaActiva() && !Boolean.TRUE.equals(u.getRestringido()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Solo un usuario con la cuenta activa puede reportar una reseña"));
        Resena resena = buscar(idResena);
        exigirVisible(resena);
        if (resena.getReserva().getHuesped().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes reportar tu propia reseña");
        }
        boolean yaLaReporto = reportesPendientes(eventosDeLaResena(idResena)).stream()
                .anyMatch(r -> r.getUsuario() != null && usuario.getIdUsuario().equals(r.getUsuario().getIdUsuario()));
        if (yaLaReporto) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya reportaste esta reseña, un administrador la va a revisar");
        }

        registrarEvento(usuario, EventoAuditoria.REPORTAR_RESENA, idResena, "REPORTADA",
                "Motivo: " + dto.getMotivo().trim());
        return mensaje("La reseña fue reportada, un administrador la va a revisar");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResenaReportadaDTO> listarReportadas(Integer idAdmin) {
        adminActivo(idAdmin, "Solo un administrador puede consultar las reseñas reportadas");
        Map<Integer, List<EventoAuditoria>> eventosPorResena = new LinkedHashMap<>();
        for (EventoAuditoria evento : eventoAuditoriaRepository
                .findByEntidadAfectadaOrderByIdEventoAsc(EventoAuditoria.ENTIDAD_RESENA)) {
            eventosPorResena.computeIfAbsent(evento.getIdEntidad(), id -> new ArrayList<>()).add(evento);
        }

        List<ResenaReportadaDTO> reportadas = new ArrayList<>();
        for (Map.Entry<Integer, List<EventoAuditoria>> entrada : eventosPorResena.entrySet()) {
            List<EventoAuditoria> pendientes = reportesPendientes(entrada.getValue());
            if (pendientes.isEmpty()) {
                continue;
            }
            resenaRepository.findById(entrada.getKey())
                    .filter(r -> Boolean.TRUE.equals(r.getVisible()))
                    .ifPresent(r -> reportadas.add(toReportadaDTO(r, pendientes)));
        }
        reportadas.sort(Comparator.comparing(ResenaReportadaDTO::getUltimoReporte).reversed());
        return reportadas;
    }

    @Override
    public ResenaResponseDTO ocultar(Integer idResena, ResenaModerarDTO dto) {
        Admin admin = adminActivo(dto.getIdAdmin(), "Solo un administrador puede ocultar una reseña");
        Resena resena = buscar(idResena);
        exigirVisible(resena);
        String motivo = dto.getMotivo().trim();

        resena.setVisible(false);
        resena.setMotivoOcultamiento(motivo);
        resenaRepository.save(resena);

        AccionModeracion accion = new AccionModeracion();
        accion.setAdmin(admin);
        accion.setTipoAccion(AccionModeracion.OCULTAMIENTO_REVIEW);
        accion.setEntidadAfectada(AccionModeracion.ENTIDAD_RESENA);
        accion.setIdEntidad(idResena);
        accion.setMotivo(motivo);
        accionModeracionRepository.save(accion);

        registrarEvento(admin.getUsuario(), EventoAuditoria.OCULTAR_RESENA, idResena, "OCULTADA", "Motivo: " + motivo);

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(resena.getReserva().getHuesped().getUsuario());
        notificacion.setTipo("RESENA_OCULTADA");
        notificacion.setMensaje("Tu reseña de " + resena.getReserva().getPublicacion().getTitulo()
                + " fue ocultada por un administrador. Motivo: " + motivo);
        notificacionRepository.save(notificacion);
        return toResponseDTO(resena);
    }

    @Override
    public MensajeResponseDTO mantener(Integer idResena, ResenaModerarDTO dto) {
        Admin admin = adminActivo(dto.getIdAdmin(), "Solo un administrador puede moderar una reseña");
        Resena resena = buscar(idResena);
        exigirVisible(resena);
        if (reportesPendientes(eventosDeLaResena(idResena)).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reseña no tiene reportes pendientes");
        }

        registrarEvento(admin.getUsuario(), EventoAuditoria.MANTENER_RESENA, idResena, "MANTENIDA",
                "Motivo: " + dto.getMotivo().trim());
        return mensaje("Se revisaron los reportes y la reseña sigue visible");
    }

    private List<EventoAuditoria> eventosDeLaResena(Integer idResena) {
        return eventoAuditoriaRepository.findByEntidadAfectadaAndIdEntidadOrderByIdEventoAsc(
                EventoAuditoria.ENTIDAD_RESENA, idResena);
    }

    /**
     * Los reportes pendientes son los que llegaron después de la última decisión del administrador (mantener u
     * ocultar). Los eventos vienen ordenados del más antiguo al más reciente.
     */
    private List<EventoAuditoria> reportesPendientes(List<EventoAuditoria> eventos) {
        List<EventoAuditoria> pendientes = new ArrayList<>();
        for (EventoAuditoria evento : eventos) {
            if (EventoAuditoria.REPORTAR_RESENA.equals(evento.getOperacion())) {
                pendientes.add(evento);
            } else if (EventoAuditoria.MANTENER_RESENA.equals(evento.getOperacion())
                    || EventoAuditoria.OCULTAR_RESENA.equals(evento.getOperacion())) {
                pendientes.clear();
            }
        }
        return pendientes;
    }

    private void registrarEvento(Usuario usuario, String operacion, Integer idResena, String resultado, String detalle) {
        EventoAuditoria evento = new EventoAuditoria();
        evento.setUsuario(usuario);
        evento.setOperacion(operacion);
        evento.setEntidadAfectada(EventoAuditoria.ENTIDAD_RESENA);
        evento.setIdEntidad(idResena);
        evento.setResultado(resultado);
        evento.setDetalle(detalle);
        eventoAuditoriaRepository.save(evento);
    }

    private Admin adminActivo(Integer idAdmin, String mensajeSiNoPuede) {
        return adminRepository.findById(idAdmin)
                .filter(a -> a.getUsuario().estaActiva() && !Boolean.TRUE.equals(a.getUsuario().getRestringido()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, mensajeSiNoPuede));
    }

    private void exigirVisible(Resena resena) {
        if (!Boolean.TRUE.equals(resena.getVisible())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reseña ya fue ocultada por moderación");
        }
    }

    private MensajeResponseDTO mensaje(String texto) {
        MensajeResponseDTO respuesta = new MensajeResponseDTO();
        respuesta.setMensaje(texto);
        return respuesta;
    }

    private ResenaReportadaDTO toReportadaDTO(Resena resena, List<EventoAuditoria> reportes) {
        ResenaReportadaDTO dto = new ResenaReportadaDTO();
        dto.setResena(toResponseDTO(resena));
        dto.setNumeroReportes(reportes.size());
        dto.setMotivos(reportes.stream().map(EventoAuditoria::getDetalle).toList());
        dto.setUltimoReporte(reportes.get(reportes.size() - 1).getFecha());
        return dto;
    }

    /**
     * La estancia está completada si la reserva quedó COMPLETADA o si estaba CONFIRMADA y ya pasó la fecha de salida
     * (todavía no hay nada que pase las reservas a COMPLETADA de forma automática).
     */
    private boolean estanciaCompletada(Reserva reserva) {
        if (reserva.getEstado() == EstadoReserva.COMPLETADA) {
            return true;
        }
        return reserva.getEstado() == EstadoReserva.CONFIRMADA && !reserva.getFechaSalida().isAfter(LocalDate.now());
    }

    private Resena resenaDelAutor(Integer idResena, Integer idHuesped, String mensajeSiNoEsSuya) {
        Huesped huesped = huespedActivo(idHuesped, "Solo un huésped con la cuenta activa puede modificar sus reseñas");
        Resena resena = buscar(idResena);
        if (!resena.getReserva().getHuesped().getIdUsuario().equals(huesped.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, mensajeSiNoEsSuya);
        }
        if (!Boolean.TRUE.equals(resena.getVisible())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La reseña fue ocultada por moderación y no se puede modificar");
        }
        return resena;
    }

    private Resena buscar(Integer idResena) {
        return resenaRepository.findById(idResena)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La reseña no existe"));
    }

    private Huesped huespedActivo(Integer idHuesped, String mensajeSiNoPuede) {
        return huespedRepository.findById(idHuesped)
                .filter(h -> h.getUsuario().estaActiva() && !Boolean.TRUE.equals(h.getUsuario().getRestringido()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, mensajeSiNoPuede));
    }

    /** Un comentario vacío o solo con espacios se guarda como null. */
    private String limpiarComentario(String comentario) {
        return comentario == null || comentario.isBlank() ? null : comentario.trim();
    }

    private ResenaResponseDTO toResponseDTO(Resena resena) {
        Reserva reserva = resena.getReserva();
        ResenaResponseDTO dto = new ResenaResponseDTO();
        dto.setIdResena(resena.getIdResena());
        dto.setIdReserva(reserva.getIdReserva());
        dto.setIdPublicacion(reserva.getPublicacion().getIdPublicacion());
        dto.setTituloPublicacion(reserva.getPublicacion().getTitulo());
        dto.setIdHuesped(reserva.getHuesped().getIdUsuario());
        dto.setNombreHuesped(reserva.getHuesped().getUsuario().getNombreCompleto());
        dto.setCalificacion(resena.getCalificacion());
        dto.setComentario(resena.getComentario());
        dto.setFecha(resena.getFecha());
        dto.setVisible(resena.getVisible());
        return dto;
    }
}
