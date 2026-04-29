package com.internship.tool.service;

public interface EmailNotificationService {

    void sendDailyReminderNotifications();

    void sendDeadlineAlertNotifications();
}
