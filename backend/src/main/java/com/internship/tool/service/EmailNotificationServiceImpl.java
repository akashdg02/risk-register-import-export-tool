package com.internship.tool.service;

import com.internship.tool.entity.RiskRegister;
import com.internship.tool.repository.RiskRegisterRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationServiceImpl.class);
    private static final String REMINDER_TEMPLATE = "risk-reminder-email";
    private static final String DEADLINE_ALERT_TEMPLATE = "risk-deadline-alert-email";

    private final RiskRegisterRepository riskRegisterRepository;
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final String fromEmail;
    private final int reminderWindowDays;

    public EmailNotificationServiceImpl(
        RiskRegisterRepository riskRegisterRepository,
        JavaMailSender javaMailSender,
        TemplateEngine templateEngine,
        @Value("${app.notifications.from-email}") String fromEmail,
        @Value("${app.notifications.reminder-window-days}") int reminderWindowDays
    ) {
        this.riskRegisterRepository = riskRegisterRepository;
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.fromEmail = fromEmail;
        this.reminderWindowDays = reminderWindowDays;
    }

    @Override
    public void sendDailyReminderNotifications() {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(reminderWindowDays);

        List<RiskRegister> upcomingRisks = riskRegisterRepository.findByTargetResolutionDateBetweenAndActiveTrue(
            today.plusDays(1),
            endDate
        );

        sendGroupedNotifications(
            upcomingRisks,
            "Risk Register Daily Reminder",
            REMINDER_TEMPLATE,
            this::buildReminderContext
        );
    }

    @Override
    public void sendDeadlineAlertNotifications() {
        LocalDate today = LocalDate.now();
        List<RiskRegister> todayRisks = riskRegisterRepository.findByTargetResolutionDateAndActiveTrue(today);

        sendGroupedNotifications(
            todayRisks,
            "Risk Register Deadline Alert",
            DEADLINE_ALERT_TEMPLATE,
            this::buildDeadlineContext
        );
    }

    private void sendGroupedNotifications(
        List<RiskRegister> risks,
        String subject,
        String templateName,
        Function<List<RiskRegister>, Context> contextBuilder
    ) {
        if (risks.isEmpty()) {
            return;
        }

        Map<String, List<RiskRegister>> risksByOwner = risks.stream()
            .collect(Collectors.groupingBy(risk -> risk.getOwnerEmail().trim().toLowerCase()));

        risksByOwner.forEach((ownerEmail, ownerRisks) -> {
            try {
                sendHtmlEmail(ownerEmail, subject, templateName, contextBuilder.apply(ownerRisks));
            } catch (MessagingException | MailException exception) {
                LOGGER.error("Failed to send notification email to {}", ownerEmail, exception);
            }
        });
    }

    private Context buildReminderContext(List<RiskRegister> risks) {
        Context context = new Context();
        RiskRegister firstRisk = risks.get(0);
        context.setVariable("ownerName", firstRisk.getOwnerName());
        context.setVariable("risks", risks);
        context.setVariable("daysRemaining", buildDaysRemainingMap(risks));
        return context;
    }

    private Context buildDeadlineContext(List<RiskRegister> risks) {
        Context context = new Context();
        RiskRegister firstRisk = risks.get(0);
        context.setVariable("ownerName", firstRisk.getOwnerName());
        context.setVariable("risks", risks);
        context.setVariable("today", LocalDate.now());
        return context;
    }

    private Map<Long, Long> buildDaysRemainingMap(List<RiskRegister> risks) {
        LocalDate today = LocalDate.now();
        return risks.stream()
            .collect(Collectors.toMap(
                RiskRegister::getId,
                risk -> ChronoUnit.DAYS.between(today, risk.getTargetResolutionDate())
            ));
    }

    private void sendHtmlEmail(String toEmail, String subject, String templateName, Context context)
        throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(templateEngine.process(templateName, context), true);
        javaMailSender.send(mimeMessage);
    }
}
