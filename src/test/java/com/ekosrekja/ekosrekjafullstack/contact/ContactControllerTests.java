package com.ekosrekja.ekosrekjafullstack.contact;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ContactControllerTests {
    private final JavaMailSender sender = mock(JavaMailSender.class);
    private final String valid = """
            {"name":"Visitor","email":"visitor@example.com","message":"Hello from the website"}
            """;

    private MockMvc mvc(String username, String password) {
        return MockMvcBuilders.standaloneSetup(new ContactController(sender, username, password)).build();
    }

    @Test
    void sendsToFixedRecipientAndUsesVisitorAsReplyTo() throws Exception {
        mvc("sender@gmail.com", "test-password").perform(post("/api/contact")
                .contentType(MediaType.APPLICATION_JSON).content(valid)).andExpect(status().isNoContent());
        var mail = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(mail.capture());
        assertArrayEquals(new String[]{"ekosrekja@gmail.com"}, mail.getValue().getTo());
        assertEquals("sender@gmail.com", mail.getValue().getFrom());
        assertEquals("visitor@example.com", mail.getValue().getReplyTo());
        assertTrue(mail.getValue().getText().contains("Hello from the website"));
    }

    @Test
    void invalidInputIsRejectedWithoutSending() throws Exception {
        var mvc = mvc("sender@gmail.com", "test-password");
        for (String body : new String[]{valid.replace("visitor@example.com", "invalid"),
                valid.replace("Hello from the website", " "), valid.replace("Visitor", "x".repeat(101))}) {
            mvc.perform(post("/api/contact").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(sender);
    }

    @Test
    void missingCredentialsDoNotReportSuccess() throws Exception {
        mvc("", "").perform(post("/api/contact").contentType(MediaType.APPLICATION_JSON).content(valid))
                .andExpect(status().isServiceUnavailable());
        verifyNoInteractions(sender);
    }

    @Test
    void failedDeliveryDoesNotReportSuccess() throws Exception {
        doThrow(new MailSendException("SMTP failure")).when(sender).send(any(SimpleMailMessage.class));
        mvc("sender@gmail.com", "test-password").perform(post("/api/contact")
                .contentType(MediaType.APPLICATION_JSON).content(valid)).andExpect(status().isBadGateway());
    }
}
