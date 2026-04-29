package com.internship.tool.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.internship.tool.entity.RiskRegister;
import com.internship.tool.repository.RiskRegisterRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceImplTests {

    @Mock
    private RiskRegisterRepository riskRegisterRepository;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private TemplateEngine templateEngine;

    private EmailNotificationServiceImpl emailNotificationService;

    @BeforeEach
    void setUp() {
        emailNotificationService = new EmailNotificationServiceImpl(
            riskRegisterRepository,
            javaMailSender,
            templateEngine,
            "no-reply@example.com",
            3
        );

        lenient().when(javaMailSender.createMimeMessage())
            .thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        lenient().when(templateEngine.process(any(String.class), any())).thenReturn("<html>content</html>");
    }

    @Test
    void shouldSendReminderEmailsGroupedByOwner() {
        when(riskRegisterRepository.findByTargetResolutionDateBetweenAndActiveTrue(any(), any()))
            .thenReturn(List.of(
                createRisk(1L, "owner1@example.com", LocalDate.now().plusDays(1)),
                createRisk(2L, "owner1@example.com", LocalDate.now().plusDays(2)),
                createRisk(3L, "owner2@example.com", LocalDate.now().plusDays(3))
            ));

        emailNotificationService.sendDailyReminderNotifications();

        verify(templateEngine, times(2)).process(eq("risk-reminder-email"), any());
        verify(javaMailSender, times(2)).send(any(MimeMessage.class));
    }

    @Test
    void shouldSendDeadlineAlertEmailsGroupedByOwner() {
        when(riskRegisterRepository.findByTargetResolutionDateAndActiveTrue(any()))
            .thenReturn(List.of(
                createRisk(4L, "owner1@example.com", LocalDate.now()),
                createRisk(5L, "owner2@example.com", LocalDate.now())
            ));

        emailNotificationService.sendDeadlineAlertNotifications();

        verify(templateEngine, times(2)).process(eq("risk-deadline-alert-email"), any());
        verify(javaMailSender, times(2)).send(any(MimeMessage.class));
    }

    @Test
    void shouldSkipEmailWhenNoReminderRisksExist() {
        when(riskRegisterRepository.findByTargetResolutionDateBetweenAndActiveTrue(any(), any()))
            .thenReturn(List.of());

        emailNotificationService.sendDailyReminderNotifications();

        verify(javaMailSender, times(0)).send(any(MimeMessage.class));
    }

    private RiskRegister createRisk(Long id, String ownerEmail, LocalDate targetDate) {
        RiskRegister riskRegister = new RiskRegister();
        riskRegister.setId(id);
        riskRegister.setRiskCode("RISK-" + id);
        riskRegister.setTitle("Risk " + id);
        riskRegister.setOwnerName("Owner");
        riskRegister.setOwnerEmail(ownerEmail);
        riskRegister.setPriority("High");
        riskRegister.setTargetResolutionDate(targetDate);
        riskRegister.setActive(true);
        return riskRegister;
    }
}
