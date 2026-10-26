-- =====================================================================
--  Marketplace de Alojamientos - Esquema de base de datos (MySQL 8.0+)
--  Basado en el modelo de Oracle Data Modeler y alineado con el
--  diagrama de clases.
--
--  Convenciones:
--    * Tablas y columnas en snake_case minúscula (coincide con la
--      estrategia de nombres por defecto de Hibernate/JPA).
--    * Los enums del diagrama se guardan como VARCHAR + CHECK
--      (en JPA: @Enumerated(EnumType.STRING)).
--    * Booleanos como TINYINT(1) (0 / 1).
--    * Dinero como DECIMAL(12,2).
--    * CHECK constraints requieren MySQL 8.0.16 o superior.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS marketplace
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE marketplace;

-- =====================================================================
--  USUARIOS Y ROLES
-- =====================================================================

-- La llave es un id interno; la cédula se guarda aparte para no exponerla en los endpoints.
CREATE TABLE usuario (
    id_usuario        INT           NOT NULL AUTO_INCREMENT,
    cedula            VARCHAR(10)   NOT NULL,
    primer_nombre     VARCHAR(50)   NOT NULL,
    segundo_nombre    VARCHAR(50),
    primer_apellido   VARCHAR(50)   NOT NULL,
    segundo_apellido  VARCHAR(50)   NOT NULL,
    email             VARCHAR(200)  NOT NULL,
    contrasena        VARCHAR(100)  NOT NULL,  -- hash (BCrypt = 60 caracteres)
    estado_cuenta     VARCHAR(20)   NOT NULL DEFAULT 'ACTIVA',
    restringido       TINYINT(1)    NOT NULL DEFAULT 0,
    CONSTRAINT usuario_pk PRIMARY KEY (id_usuario),
    CONSTRAINT usuario_cedula_un UNIQUE (cedula),
    CONSTRAINT usuario_email_un UNIQUE (email),
    CONSTRAINT ck_usuario_estado_cuenta CHECK (estado_cuenta IN ('ACTIVA', 'DESACTIVADA')),
    CONSTRAINT ck_usuario_restringido CHECK (restringido IN (0, 1))
) ENGINE = InnoDB;

CREATE TABLE admin (
    id_usuario INT NOT NULL,
    CONSTRAINT admin_pk PRIMARY KEY (id_usuario),
    CONSTRAINT admin_usuario_fk FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
) ENGINE = InnoDB;

CREATE TABLE anfitrion (
    id_usuario INT NOT NULL,
    CONSTRAINT anfitrion_pk PRIMARY KEY (id_usuario),
    CONSTRAINT anfitrion_usuario_fk FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
) ENGINE = InnoDB;

CREATE TABLE huesped (
    id_usuario INT NOT NULL,
    CONSTRAINT huesped_pk PRIMARY KEY (id_usuario),
    CONSTRAINT huesped_usuario_fk FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
) ENGINE = InnoDB;

CREATE TABLE notificacion (
    id_notificacion  INT           NOT NULL AUTO_INCREMENT,
    id_usuario       INT           NOT NULL,
    tipo             VARCHAR(50)   NOT NULL,
    mensaje          VARCHAR(500)  NOT NULL,
    fecha            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    leida            TINYINT(1)    NOT NULL DEFAULT 0,
    CONSTRAINT notificacion_pk PRIMARY KEY (id_notificacion),
    CONSTRAINT notificacion_usuario_fk FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT ck_notificacion_leida CHECK (leida IN (0, 1))
) ENGINE = InnoDB;

CREATE TABLE evento_auditoria (
    id_evento         INT            NOT NULL AUTO_INCREMENT,
    id_usuario        INT,           -- NULL para eventos sin usuario (ej. login fallido de un correo inexistente)
    operacion         VARCHAR(50)    NOT NULL,
    entidad_afectada  VARCHAR(50)    NOT NULL,
    id_entidad        INT,
    fecha             DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resultado         VARCHAR(50)    NOT NULL,
    detalle           VARCHAR(1000),
    CONSTRAINT evento_auditoria_pk PRIMARY KEY (id_evento),
    CONSTRAINT evento_auditoria_usuario_fk FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
) ENGINE = InnoDB;

-- =====================================================================
--  CATÁLOGOS
-- =====================================================================

-- Catálogo de políticas (patrón estrategia: el código indica qué
-- implementación de PoliticaCancelacion usar, ej. 'FLEXIBLE').
CREATE TABLE politica_cancelacion (
    id_politica           INT            NOT NULL AUTO_INCREMENT,
    codigo                VARCHAR(30)    NOT NULL,
    titulo_politica       VARCHAR(50)    NOT NULL,
    descripcion_politica  VARCHAR(1000)  NOT NULL,
    CONSTRAINT politica_cancelacion_pk PRIMARY KEY (id_politica),
    CONSTRAINT politica_cancelacion_codigo_un UNIQUE (codigo)
) ENGINE = InnoDB;

CREATE TABLE servicio (
    id_servicio      INT          NOT NULL AUTO_INCREMENT,
    nombre_servicio  VARCHAR(50)  NOT NULL,
    CONSTRAINT servicio_pk PRIMARY KEY (id_servicio),
    CONSTRAINT servicio_nombre_un UNIQUE (nombre_servicio)
) ENGINE = InnoDB;

CREATE TABLE servicio_adicional (
    id_servicio_adicional  INT            NOT NULL AUTO_INCREMENT,
    nombre_servicio        VARCHAR(50)    NOT NULL,
    costo                  DECIMAL(12,2)  NOT NULL,
    CONSTRAINT servicio_adicional_pk PRIMARY KEY (id_servicio_adicional),
    CONSTRAINT ck_servicio_adicional_costo CHECK (costo >= 0)
) ENGINE = InnoDB;

-- =====================================================================
--  PUBLICACIÓN (incluye los datos del alojamiento)
-- =====================================================================

CREATE TABLE publicacion (
    id_publicacion     INT            NOT NULL AUTO_INCREMENT,
    titulo             VARCHAR(52)    NOT NULL,
    descripcion        VARCHAR(500)   NOT NULL,
    ubicacion_textual  VARCHAR(100)   NOT NULL,
    ciudad             VARCHAR(50)    NOT NULL,
    tipo_alojamiento   VARCHAR(20)    NOT NULL,
    capacidad          INT            NOT NULL,
    precio_noche       DECIMAL(12,2)  NOT NULL,
    tarifa_limpieza    DECIMAL(12,2)  NOT NULL DEFAULT 0,
    reglas             VARCHAR(1000),
    estado             VARCHAR(30)    NOT NULL DEFAULT 'BORRADOR',
    modo_aceptacion    VARCHAR(30)    NOT NULL,
    motivo_bloqueo     VARCHAR(500),
    id_anfitrion       INT            NOT NULL,
    id_politica        INT            NOT NULL,
    CONSTRAINT publicacion_pk PRIMARY KEY (id_publicacion),
    CONSTRAINT publicacion_anfitrion_fk FOREIGN KEY (id_anfitrion) REFERENCES anfitrion (id_usuario),
    CONSTRAINT publicacion_politica_fk FOREIGN KEY (id_politica) REFERENCES politica_cancelacion (id_politica),
    CONSTRAINT ck_publicacion_capacidad CHECK (capacidad > 0),
    CONSTRAINT ck_publicacion_precio_noche CHECK (precio_noche > 0),
    CONSTRAINT ck_publicacion_tarifa_limpieza CHECK (tarifa_limpieza >= 0),
    CONSTRAINT ck_publicacion_estado CHECK (estado IN ('BORRADOR', 'PENDIENTE_REVISION', 'ACTIVA', 'PAUSADA', 'BLOQUEADA')),
    CONSTRAINT ck_publicacion_modo_aceptacion CHECK (modo_aceptacion IN ('INMEDIATA', 'CON_APROBACION'))
) ENGINE = InnoDB;

-- Antes: alojamiento_servicio (Tener)
CREATE TABLE publicacion_servicio (
    id_publicacion  INT NOT NULL,
    id_servicio     INT NOT NULL,
    CONSTRAINT publicacion_servicio_pk PRIMARY KEY (id_publicacion, id_servicio),
    CONSTRAINT publicacion_servicio_publicacion_fk FOREIGN KEY (id_publicacion)
        REFERENCES publicacion (id_publicacion) ON DELETE CASCADE,
    CONSTRAINT publicacion_servicio_servicio_fk FOREIGN KEY (id_servicio)
        REFERENCES servicio (id_servicio) ON DELETE CASCADE
) ENGINE = InnoDB;

-- Antes: Tenerv1
CREATE TABLE publicacion_servicio_adicional (
    id_publicacion         INT NOT NULL,
    id_servicio_adicional  INT NOT NULL,
    CONSTRAINT publicacion_servicio_adicional_pk PRIMARY KEY (id_publicacion, id_servicio_adicional),
    CONSTRAINT pub_serv_adicional_publicacion_fk FOREIGN KEY (id_publicacion)
        REFERENCES publicacion (id_publicacion) ON DELETE CASCADE,
    CONSTRAINT pub_serv_adicional_servicio_fk FOREIGN KEY (id_servicio_adicional)
        REFERENCES servicio_adicional (id_servicio_adicional) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE referencia_imagen (
    id_imagen       INT            NOT NULL AUTO_INCREMENT,
    url_imagen      VARCHAR(2048),
    archivo_imagen  LONGBLOB,
    id_publicacion  INT            NOT NULL,
    CONSTRAINT referencia_imagen_pk PRIMARY KEY (id_imagen),
    CONSTRAINT referencia_imagen_publicacion_fk FOREIGN KEY (id_publicacion)
        REFERENCES publicacion (id_publicacion) ON DELETE CASCADE,
    CONSTRAINT ck_referencia_imagen_origen CHECK (url_imagen IS NOT NULL OR archivo_imagen IS NOT NULL)
) ENGINE = InnoDB;

CREATE TABLE periodo_disponibilidad (
    id_periodo      INT          NOT NULL AUTO_INCREMENT,
    id_publicacion  INT          NOT NULL,
    fecha_inicio    DATE         NOT NULL,
    fecha_fin       DATE         NOT NULL,
    tipo            VARCHAR(20)  NOT NULL,
    CONSTRAINT periodo_disponibilidad_pk PRIMARY KEY (id_periodo),
    CONSTRAINT periodo_disponibilidad_publicacion_fk FOREIGN KEY (id_publicacion)
        REFERENCES publicacion (id_publicacion) ON DELETE CASCADE,
    CONSTRAINT ck_periodo_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT ck_periodo_tipo CHECK (tipo IN ('DISPONIBLE', 'BLOQUEADO', 'RESERVADO'))
) ENGINE = InnoDB;

-- =====================================================================
--  RESERVAS
-- =====================================================================

-- La cotización se calcula antes de reservar, por eso la reserva la referencia.
CREATE TABLE cotizacion (
    id_cotizacion                INT            NOT NULL AUTO_INCREMENT,
    noches                       INT            NOT NULL,
    precio_base                  DECIMAL(12,2)  NOT NULL,
    tarifa_limpieza              DECIMAL(12,2)  NOT NULL DEFAULT 0,
    tarifa_servicio              DECIMAL(12,2)  NOT NULL DEFAULT 0,
    descuentos                   DECIMAL(12,2)  NOT NULL DEFAULT 0,
    costo_servicios_adicionales  DECIMAL(12,2)  NOT NULL DEFAULT 0,
    total                        DECIMAL(12,2)  NOT NULL,
    CONSTRAINT cotizacion_pk PRIMARY KEY (id_cotizacion),
    CONSTRAINT ck_cotizacion_noches CHECK (noches > 0),
    CONSTRAINT ck_cotizacion_valores CHECK (
        precio_base >= 0 AND tarifa_limpieza >= 0 AND tarifa_servicio >= 0
        AND descuentos >= 0 AND costo_servicios_adicionales >= 0 AND total >= 0
    )
) ENGINE = InnoDB;

CREATE TABLE reserva (
    id_reserva          INT           NOT NULL AUTO_INCREMENT,
    estado              VARCHAR(30)   NOT NULL DEFAULT 'PENDIENTE',
    fecha_llegada       DATE          NOT NULL,
    fecha_salida        DATE          NOT NULL,
    num_huespedes       INT           NOT NULL,
    clave_idempotencia  VARCHAR(100)  NOT NULL,
    id_huesped          INT           NOT NULL,
    id_publicacion      INT           NOT NULL,
    id_cotizacion       INT           NOT NULL,
    CONSTRAINT reserva_pk PRIMARY KEY (id_reserva),
    CONSTRAINT reserva_clave_idempotencia_un UNIQUE (clave_idempotencia),
    CONSTRAINT reserva_cotizacion_un UNIQUE (id_cotizacion),
    CONSTRAINT reserva_huesped_fk FOREIGN KEY (id_huesped) REFERENCES huesped (id_usuario),
    CONSTRAINT reserva_publicacion_fk FOREIGN KEY (id_publicacion) REFERENCES publicacion (id_publicacion),
    CONSTRAINT reserva_cotizacion_fk FOREIGN KEY (id_cotizacion) REFERENCES cotizacion (id_cotizacion),
    CONSTRAINT ck_reserva_fechas CHECK (fecha_salida > fecha_llegada),
    CONSTRAINT ck_reserva_num_huespedes CHECK (num_huespedes > 0),
    CONSTRAINT ck_reserva_estado CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'RECHAZADA', 'CANCELADA', 'COMPLETADA', 'EN_REVISION_MANUAL'))
) ENGINE = InnoDB;

-- Antes: "SERVAD-RESERV"
CREATE TABLE reserva_servicio_adicional (
    id_reserva             INT NOT NULL,
    id_servicio_adicional  INT NOT NULL,
    CONSTRAINT reserva_servicio_adicional_pk PRIMARY KEY (id_reserva, id_servicio_adicional),
    CONSTRAINT res_serv_adicional_reserva_fk FOREIGN KEY (id_reserva)
        REFERENCES reserva (id_reserva) ON DELETE CASCADE,
    CONSTRAINT res_serv_adicional_servicio_fk FOREIGN KEY (id_servicio_adicional)
        REFERENCES servicio_adicional (id_servicio_adicional)
) ENGINE = InnoDB;

CREATE TABLE pago (
    id_pago                INT            NOT NULL AUTO_INCREMENT,
    id_reserva             INT            NOT NULL,
    tipo_movimiento        VARCHAR(20)    NOT NULL,
    valor                  DECIMAL(12,2)  NOT NULL,
    emisor                 VARCHAR(50)    NOT NULL,
    receptor               VARCHAR(50)    NOT NULL,
    id_transaccion_stripe  VARCHAR(100),
    estado                 VARCHAR(20)    NOT NULL,
    fecha                  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pago_pk PRIMARY KEY (id_pago),
    CONSTRAINT pago_transaccion_stripe_un UNIQUE (id_transaccion_stripe),
    CONSTRAINT pago_reserva_fk FOREIGN KEY (id_reserva) REFERENCES reserva (id_reserva),
    CONSTRAINT ck_pago_valor CHECK (valor > 0),
    CONSTRAINT ck_pago_tipo_movimiento CHECK (tipo_movimiento IN ('PAGO', 'DEVOLUCION')),
    CONSTRAINT ck_pago_estado CHECK (estado IN ('APROBADO', 'RECHAZADO'))
) ENGINE = InnoDB;

CREATE TABLE cancelacion (
    id_cancelacion     INT            NOT NULL AUTO_INCREMENT,
    id_reserva         INT            NOT NULL,
    id_usuario         INT            NOT NULL,  -- quien cancela (huésped o anfitrión)
    fecha              DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo             VARCHAR(500)   NOT NULL,
    valor_devolucion   DECIMAL(12,2)  NOT NULL DEFAULT 0,
    estado_devolucion  VARCHAR(20)    NOT NULL,
    CONSTRAINT cancelacion_pk PRIMARY KEY (id_cancelacion),
    CONSTRAINT cancelacion_reserva_un UNIQUE (id_reserva),
    CONSTRAINT cancelacion_reserva_fk FOREIGN KEY (id_reserva) REFERENCES reserva (id_reserva),
    CONSTRAINT cancelacion_usuario_fk FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT ck_cancelacion_valor_devolucion CHECK (valor_devolucion >= 0),
    CONSTRAINT ck_cancelacion_estado_devolucion CHECK (estado_devolucion IN ('DEVUELTA', 'SIN_DEVOLUCION'))
) ENGINE = InnoDB;

CREATE TABLE resena (
    id_resena            INT            NOT NULL AUTO_INCREMENT,
    id_reserva           INT            NOT NULL,
    calificacion         TINYINT        NOT NULL,
    comentario           VARCHAR(1000),
    fecha                DATE           NOT NULL,
    visible              TINYINT(1)     NOT NULL DEFAULT 1,
    motivo_ocultamiento  VARCHAR(500),
    CONSTRAINT resena_pk PRIMARY KEY (id_resena),
    CONSTRAINT resena_reserva_un UNIQUE (id_reserva),
    CONSTRAINT resena_reserva_fk FOREIGN KEY (id_reserva) REFERENCES reserva (id_reserva),
    CONSTRAINT ck_resena_calificacion CHECK (calificacion BETWEEN 1 AND 5),
    CONSTRAINT ck_resena_visible CHECK (visible IN (0, 1))
) ENGINE = InnoDB;

-- =====================================================================
--  ADMINISTRACIÓN
-- =====================================================================

CREATE TABLE accion_moderacion (
    id_accion         INT           NOT NULL AUTO_INCREMENT,
    id_admin          INT           NOT NULL,
    tipo_accion       VARCHAR(30)   NOT NULL,
    entidad_afectada  VARCHAR(50)   NOT NULL,
    id_entidad        INT           NOT NULL,
    motivo            VARCHAR(500)  NOT NULL,
    fecha             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT accion_moderacion_pk PRIMARY KEY (id_accion),
    CONSTRAINT accion_moderacion_admin_fk FOREIGN KEY (id_admin) REFERENCES admin (id_usuario),
    CONSTRAINT ck_accion_moderacion_tipo CHECK (tipo_accion IN ('BLOQUEO_PUBLICACION', 'RESTRICCION_USUARIO', 'OCULTAMIENTO_REVIEW'))
) ENGINE = InnoDB;

CREATE TABLE reporte_exportado (
    id_reporte    INT          NOT NULL AUTO_INCREMENT,
    id_admin      INT          NOT NULL,
    tipo          VARCHAR(30)  NOT NULL,
    formato       VARCHAR(10)  NOT NULL,
    fecha         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT reporte_exportado_pk PRIMARY KEY (id_reporte),
    CONSTRAINT reporte_exportado_admin_fk FOREIGN KEY (id_admin) REFERENCES admin (id_usuario),
    CONSTRAINT ck_reporte_tipo CHECK (tipo IN ('USUARIOS', 'PUBLICACIONES', 'RESERVAS', 'INGRESOS_ESTIMADOS', 'DEMANDA')),
    CONSTRAINT ck_reporte_formato CHECK (formato IN ('PDF', 'EXCEL'))
) ENGINE = InnoDB;
