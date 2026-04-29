package com.internship.tool.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RiskRegisterNotificationSchedulerTests {

    @Mock
    private EmailNotificationService emailNotificationService;

    @Test
    void shouldInvokeReminderAndDeadlineNotifications() {
        RiskRegisterNotificationScheduler scheduler = new RiskRegisterNotificationScheduler(emailNotificationService);

        scheduler.sendScheduledNotifications();

        verify(emailNotificationService).sendDailyReminderNotifications();
        verify(emailNotificationService).sendDeadlineAlertNotifications();
    }
}
