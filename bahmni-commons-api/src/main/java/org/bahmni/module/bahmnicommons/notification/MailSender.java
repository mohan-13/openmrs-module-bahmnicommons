package org.bahmni.module.bahmnicommons.notification;

public interface MailSender {
    void send(String subject, String body, String[] to, String[] cc, String[] bcc);
}
