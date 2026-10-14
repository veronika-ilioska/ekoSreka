package com.ekosrekja.ekosrekjafullstack.config;

import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mail.javamail.JavaMailSender;
import com.ekosrekja.ekosrekjafullstack.contact.ContactController;
import static org.junit.jupiter.api.Assertions.*;

class DeploymentConfigurationTests {
    private MockEnvironment environment() throws Exception {
        var environment = new MockEnvironment();
        Properties properties = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"));
        environment.getPropertySources().addLast(new PropertiesPropertySource("application", properties));
        return environment;
    }

    @Test
    void renderRemoteVariablesConfigureAllDatabaseFields() throws Exception {
        var env = environment().withProperty("DB_REMOTE_NAME", "jdbc:postgresql://example.neon.tech/neondb?sslmode=require")
                .withProperty("DB_REMOTE_USERNAME", "remote-user").withProperty("DB_REMOTE_PASSWORD", "remote-password");
        assertEquals("jdbc:postgresql://example.neon.tech/neondb?sslmode=require", env.getProperty("spring.datasource.url"));
        assertEquals("remote-user", env.getProperty("spring.datasource.username"));
        assertEquals("remote-password", env.getProperty("spring.datasource.password"));
        env.setProperty("SPRING_DATASOURCE_URL", "jdbc:postgresql://localhost:5432/local");
        assertEquals("jdbc:postgresql://localhost:5432/local", env.getProperty("spring.datasource.url"));
    }

    @Test
    void localDatabaseConfigurationStillWorks() throws Exception {
        var env = environment().withProperty("DB_HOST", "localhost").withProperty("DB_NAME", "local")
                .withProperty("DB_USERNAME", "local-user").withProperty("DB_PASSWORD", "local-password");
        assertEquals("jdbc:postgresql://localhost:5432/local", env.getProperty("spring.datasource.url"));
        assertEquals("local-user", env.getProperty("spring.datasource.username"));
        assertEquals("local-password", env.getProperty("spring.datasource.password"));
    }

    @Test
    void mailAndContactBeansStartWithoutSmtpCredentials() throws Exception {
        var env = environment();
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(MailSenderAutoConfiguration.class))
                .withUserConfiguration(ContactController.class)
                .withPropertyValues("spring.mail.host=" + env.getProperty("spring.mail.host"),
                        "spring.mail.username=" + env.getProperty("spring.mail.username"),
                        "spring.mail.password=" + env.getProperty("spring.mail.password"))
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertNotNull(context.getBean(JavaMailSender.class));
                    assertNotNull(context.getBean(ContactController.class));
                });
    }
}
