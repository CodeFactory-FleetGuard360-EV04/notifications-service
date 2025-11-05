package com.fg360.service.implementation;

import com.fg360.presentation.controller.dto.AlertDTO;
import com.fg360.presentation.controller.dto.PushDTO;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class NotificationServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleEmail_sendsMailAndPush() throws Exception {
        // Arrange
        AlertDTO alert = new AlertDTO(
                new String[]{"test@fg360.com"}, // toUsers
                "Alerta de temperatura",        // alertType
                "Juan Pérez",                   // responsible
                "Alta",                         // priority
                "Carlos Rivas",                 // driver
                "Unidad 01",                    // generatingUnit
                "Activa",                       // state
                LocalDateTime.now()             // generationDate
        );

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("alert-email"), any(Context.class)))
                .thenReturn("<html>Mock Template</html>");

        // Act
        notificationService.handleEmail(alert);

        // Assert
        verify(templateEngine, times(1)).process(eq("alert-email"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/all/alerts"), any(PushDTO.class));
    }

    @Test
    void handlePush_sendsToWebsocket() {
        // Arrange
        AlertDTO alert = new AlertDTO(
                new String[]{"user@fg360.com"},
                "Prueba alerta",
                "María López",
                "Media",
                "Pedro Gómez",
                "Unidad 02",
                "Activa",
                LocalDateTime.now()
        );

        // Act
        notificationService.handlePush(alert);

        // Assert
        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/all/alerts"), any(PushDTO.class));
    }
}
