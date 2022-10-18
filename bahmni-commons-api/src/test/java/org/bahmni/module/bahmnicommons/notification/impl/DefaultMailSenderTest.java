package org.bahmni.module.bahmnicommons.notification.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.AdministrationService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doNothing;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Transport.class})
public class DefaultMailSenderTest {

    @Mock
    AdministrationService administrationService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test()
    public void shouldSendEmailIfAddressIsValid() throws Exception {
        final String subject = "Hello World";
        final String body = "nothing";
        DefaultMailSender mailSender = new DefaultMailSender(administrationService);

        when(administrationService.getGlobalProperty(eq("mail.transport_protocol"), eq("smtp"))).thenReturn("smtp");
        when(administrationService.getGlobalProperty("mail.smtp_host", "")).thenReturn("localhost");
        when(administrationService.getGlobalProperty("mail.smtp_port", "25")).thenReturn("25");
        when(administrationService.getGlobalProperty("mail.smtp_auth", "false")).thenReturn("true");
        when(administrationService.getGlobalProperty("mail.smtp.starttls.enable", "true")).thenReturn("true");
        when(administrationService.getGlobalProperty("mail.debug", "false")).thenReturn("false");
        when(administrationService.getGlobalProperty("mail.from", "")).thenReturn("noreply@bahmni.org");
        when(administrationService.getGlobalProperty("mail.user", "")).thenReturn("test");
        when(administrationService.getGlobalProperty("mail.password", "")).thenReturn("random");

        PowerMockito.mockStatic(Transport.class);
        doNothing().when(Transport.class, "send", Mockito.any(MimeMessage.class));

        mailSender.send(subject, body, new String[]{"test@bahmni.org"}, new String[]{"test-cc@bahmni.org"}, new String[]{"test-bcc@bahmni.org"});

        ArgumentCaptor<MimeMessage> argument = ArgumentCaptor.forClass(MimeMessage.class);
        PowerMockito.verifyStatic();
        Transport.send(argument.capture());
        MimeMessage msg = argument.getValue();
        assertNotNull(msg);
        assertEquals(msg.getSubject(), subject);
    }


    @Test
    public void shouldThrowErrorForInvalidEmailAddress() {
        DefaultMailSender mailSender = new DefaultMailSender(administrationService);

        when(administrationService.getGlobalProperty(eq("mail.transport_protocol"), eq("smtp"))).thenReturn("smtp");
        when(administrationService.getGlobalProperty("mail.smtp_host", "")).thenReturn("localhost");
        when(administrationService.getGlobalProperty("mail.smtp_port", "25")).thenReturn("25");
        when(administrationService.getGlobalProperty("mail.smtp_auth", "false")).thenReturn("true");
        when(administrationService.getGlobalProperty("mail.smtp.starttls.enable", "true")).thenReturn("true");
        when(administrationService.getGlobalProperty("mail.debug", "false")).thenReturn("false");
        when(administrationService.getGlobalProperty("mail.from", "")).thenReturn("noreply@bahmni.org");
        when(administrationService.getGlobalProperty("mail.user", "")).thenReturn("test");
        when(administrationService.getGlobalProperty("mail.password", "")).thenReturn("random");

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Error occurred while sending email");
        expectedEx.expectCause(instanceOf(javax.mail.internet.AddressException.class));
        mailSender.send("test", "nothing", new String[]{""}, null, null);
    }

}

