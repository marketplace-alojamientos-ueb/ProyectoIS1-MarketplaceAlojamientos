package co.edu.unbosque.marketplace.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/** Pruebas del envío de correo, con y sin servidor de correo configurado. */
class CorreoServiceImplTest {

    @Test
    @SuppressWarnings("unchecked")
    void sinServidorDeCorreoSoloEscribeEnConsola() {
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        CorreoServiceImpl servicio = new CorreoServiceImpl(provider, "no-reply@marketplace.local");

        assertDoesNotThrow(() -> servicio.enviarCodigoRecuperacion("ana@correo.com", "Ana", "123456", 15));
    }

    @Test
    @SuppressWarnings("unchecked")
    void conServidorDeCorreoEnviaElMensaje() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(mailSender);
        CorreoServiceImpl servicio = new CorreoServiceImpl(provider, "no-reply@marketplace.local");

        servicio.enviarCodigoRecuperacion("ana@correo.com", "Ana", "123456", 15);

        verify(mailSender).send(argThat((SimpleMailMessage m) ->
                m.getTo()[0].equals("ana@correo.com") && m.getText().contains("123456")));
    }
}
