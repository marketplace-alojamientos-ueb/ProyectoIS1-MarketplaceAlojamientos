package co.edu.unbosque.marketplace.service.impl;

import co.edu.unbosque.marketplace.service.CorreoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envía correos con JavaMailSender si spring.mail.host está configurado.
 * Provisional para desarrollo: si no hay servidor de correo, el mensaje se escribe en la consola.
 */
@Service
public class CorreoServiceImpl implements CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoServiceImpl.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String remitente;

    public CorreoServiceImpl(ObjectProvider<JavaMailSender> mailSenderProvider,
                             @Value("${app.correo.remitente:no-reply@marketplace.local}") String remitente) {
        this.mailSenderProvider = mailSenderProvider;
        this.remitente = remitente;
    }

    @Override
    public void enviarCodigoRecuperacion(String destinatario, String nombre, String codigo, long minutosExpiracion) {
        String asunto = "Recuperación de contraseña - Marketplace de Alojamientos";
        String cuerpo = "Hola " + nombre + ",\n\n"
                + "Tu código para restablecer la contraseña es: " + codigo + "\n"
                + "Vence en " + minutosExpiracion + " minutos.\n\n"
                + "Si no solicitaste este cambio, ignora este correo.";

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("[CORREO NO CONFIGURADO] Para: {} | Asunto: {}\n{}", destinatario, asunto, cuerpo);
            return;
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);
        mailSender.send(mensaje);
    }
}
