package com.internship.tool.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RiskRegisterNotificationScheduler {

    private final EmailNotificationService emailNotificationService;

    public RiskRegisterNotificationScheduler(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @Scheduled(cron = "${app.notifications.reminder-cron}")
    public void sendScheduledNotifications() {
        emailNotificationService.sendDailyReminderNotifications();
        emailNotificationService.sendDeadlineAlertNotifications();
    }
}
