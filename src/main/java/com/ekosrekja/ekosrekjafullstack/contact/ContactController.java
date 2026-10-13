package com.ekosrekja.ekosrekjafullstack.contact;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
    private final JavaMailSender sender;
    private final String from;
    private final String password;

    public ContactController(JavaMailSender sender,
            @Value("${spring.mail.username:}") String from,
            @Value("${spring.mail.password:}") String password) {
        this.sender = sender;
        this.from = from;
        this.password = password;
    }

    public record ContactRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 254) @Pattern(regexp = "[^\\r\\n]+") String email,
            @NotBlank @Size(max = 5000) String message) {}

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void send(@Valid @RequestBody ContactRequest request) {
        if (from.isBlank() || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Contact email is not configured.");
        }
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo("ekosrekja@gmail.com");
        mail.setReplyTo(request.email().trim());
        mail.setSubject("ЕкоСреќа — порака од контакт формата");
        mail.setText("Име: " + request.name().trim() + "\nЕ-пошта: " + request.email().trim()
                + "\n\n" + request.message().trim());
        try {
            sender.send(mail);
        } catch (MailException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "The message could not be sent. Please try again later.");
        }
    }
}
