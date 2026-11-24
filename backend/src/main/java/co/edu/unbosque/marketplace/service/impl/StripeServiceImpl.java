package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.model.dto.SesionStripeDTO;
import co.edu.unbosque.marketplace.service.StripeService;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
public class StripeServiceImpl implements StripeService {

    /** Stripe exige que la sesión dure entre 30 minutos y 24 horas. */
    private static final long MINUTOS_PARA_PAGAR = 60;

    private final String llaveSecreta;
    private final String moneda;

    public StripeServiceImpl(@Value("${stripe.secret-key:}") String llaveSecreta,
                             @Value("${stripe.moneda:cop}") String moneda) {
        this.llaveSecreta = llaveSecreta;
        this.moneda = moneda;
    }

    @Override
    public SesionStripeDTO crearSesionDePago(double total, String concepto, String detalle, String correo,
                                             String urlExito, String urlCancelar, Map<String, String> metadatos) {
        SessionCreateParams.LineItem.PriceData.ProductData producto =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(concepto)
                        .setDescription(detalle)
                        .build();
        SessionCreateParams.LineItem linea = SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(moneda)
                        .setUnitAmount(aCentavos(total))
                        .setProductData(producto)
                        .build())
                .build();
        SessionCreateParams parametros = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .addLineItem(linea)
                .setCustomerEmail(correo)
                .setSuccessUrl(urlExito)
                .setCancelUrl(urlCancelar)
                .setExpiresAt(Instant.now().plus(MINUTOS_PARA_PAGAR, ChronoUnit.MINUTES).getEpochSecond())
                .putAllMetadata(metadatos)
                .build();
        try {
            return toDTO(Session.create(parametros, opciones(null)));
        } catch (StripeException e) {
            throw errorDeStripe(e);
        }
    }

    @Override
    public SesionStripeDTO consultarSesion(String idSesion) {
        try {
            return toDTO(Session.retrieve(idSesion, SessionRetrieveParams.builder().build(), opciones(null)));
        } catch (InvalidRequestException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La sesión de pago no existe");
        } catch (StripeException e) {
            throw errorDeStripe(e);
        }
    }

    @Override
    public void reembolsar(String idTransaccion, String claveIdempotencia) {
        try {
            Refund.create(RefundCreateParams.builder().setPaymentIntent(idTransaccion).build(),
                    opciones(claveIdempotencia));
        } catch (StripeException e) {
            throw errorDeStripe(e);
        }
    }

    private RequestOptions opciones(String claveIdempotencia) {
        if (llaveSecreta == null || llaveSecreta.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Los pagos con Stripe no están configurados: falta la variable de entorno STRIPE_SECRET_KEY");
        }
        RequestOptions.RequestOptionsBuilder opciones = RequestOptions.builder().setApiKey(llaveSecreta);
        if (claveIdempotencia != null) {
            opciones.setIdempotencyKey(claveIdempotencia);
        }
        return opciones.build();
    }

    private ResponseStatusException errorDeStripe(StripeException e) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "No se pudo completar la operación con Stripe: " + e.getMessage());
    }

    /** Stripe recibe los valores en la unidad más pequeña de la moneda (centavos). */
    private long aCentavos(double valor) {
        return Math.round(valor * 100);
    }

    private SesionStripeDTO toDTO(Session sesion) {
        SesionStripeDTO dto = new SesionStripeDTO();
        dto.setIdSesion(sesion.getId());
        dto.setUrlPago(sesion.getUrl());
        dto.setPagada("paid".equals(sesion.getPaymentStatus()));
        dto.setIdTransaccion(sesion.getPaymentIntent());
        dto.setTotalPagado(sesion.getAmountTotal() == null ? null : sesion.getAmountTotal() / 100.0);
        dto.setMetadatos(sesion.getMetadata());
        return dto;
    }
}
