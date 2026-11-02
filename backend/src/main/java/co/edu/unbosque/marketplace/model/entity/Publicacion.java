package co.edu.unbosque.marketplace.model.entity;

import co.edu.unbosque.marketplace.model.enums.EstadoPublicacion;
import co.edu.unbosque.marketplace.model.enums.ModoAceptacion;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticaCancelacion;
import co.edu.unbosque.marketplace.model.entity.politica.PoliticaCancelacionConverter;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "publicacion")
@Getter
@Setter
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_publicacion")
    private Integer idPublicacion;

    @Column(name = "titulo", nullable = false, length = 52)
    private String titulo;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "ubicacion_textual", nullable = false, length = 100)
    private String ubicacion;

    @Column(name = "ciudad", nullable = false, length = 50)
    private String ciudad;

    @Column(name = "tipo_alojamiento", nullable = false, length = 20)
    private String tipoAlojamiento;

    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    /**
     * La columna es DECIMAL(12,2). Se usa Double como en el diagrama de clases.
     */
    @Column(name = "precio_noche", nullable = false)
    private Double precioPorNoche;

    @Column(name = "tarifa_limpieza", nullable = false)
    private Double tarifaLimpieza = 0.0;

    @Column(name = "reglas", length = 1000)
    private String reglas;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoPublicacion estado = EstadoPublicacion.BORRADOR;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_aceptacion", nullable = false, length = 30)
    private ModoAceptacion modoAceptacion;

    @Column(name = "motivo_bloqueo", length = 500)
    private String motivoBloqueo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_anfitrion", nullable = false)
    private Anfitrion anfitrion;

    @Convert(converter = PoliticaCancelacionConverter.class)
    @Column(name = "politica_cancelacion", nullable = false, length = 30)
    private PoliticaCancelacion politicaCancelacion;

    /** Servicios que ofrece el alojamiento (tabla intermedia PUBLICACION_SERVICIO). */
    @ManyToMany
    @JoinTable(name = "publicacion_servicio",
            joinColumns = @JoinColumn(name = "id_publicacion"),
            inverseJoinColumns = @JoinColumn(name = "id_servicio"))
    private List<Servicio> servicios = new ArrayList<>();

    /** Servicios adicionales con costo que ofrece la publicación (tabla intermedia PUBLICACION_SERVICIO_ADICIONAL). */
    @ManyToMany
    @JoinTable(name = "publicacion_servicio_adicional",
            joinColumns = @JoinColumn(name = "id_publicacion"),
            inverseJoinColumns = @JoinColumn(name = "id_servicio_adicional"))
    private List<ServicioAdicional> serviciosAdicionales = new ArrayList<>();

    /** Imágenes de la publicación (tabla REFERENCIA_IMAGEN). Se guardan y se borran junto con la publicación. */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_publicacion", nullable = false)
    private List<Imagen> imagenes = new ArrayList<>();

    /** Períodos de disponibilidad (tabla PERIODO_DISPONIBILIDAD). Se guardan y se borran junto con la publicación. */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_publicacion", nullable = false)
    private List<PeriodoDisponibilidad> periodos = new ArrayList<>();

    public Publicacion() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Publicacion otra)) return false;
        return idPublicacion != null && idPublicacion.equals(otra.idPublicacion);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idPublicacion);
    }
}
