package co.edu.unbosque.marketplace.model.dto;

import lombok.Getter;
import lombok.Setter;

/** Sesión de pago creada en Stripe: la URL a la que el front redirige al huésped y lo que se le va a cobrar. */
@Getter
@Setter
public class ReservaCheckoutDTO {

    private String idSesion;
    private String urlPago;
    private Integer idPublicacion;
    private String tituloPublicacion;
    private Integer noches;
    private Double precioBase;
    private Double tarifaLimpieza;
    private Double total;
}
