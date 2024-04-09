package fr.abes.wsmail;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.wsmail.controller.MailController;
import fr.abes.wsmail.mail.EmailService;
import fr.abes.wsmail.model.MailDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import javax.mail.internet.AddressException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
class WsmailApplicationTests {

    @Autowired
    EmailService emailService;
    @Autowired
    private MailController controller;
    @Autowired
    private Environment env;

    @Test
    void contextLoads() {
        assertThat(controller).isNotNull();
    }


    @Test
    void JSONToDto() throws IOException {
        String json = "{\"to\": [\"tcn@abes.fr\", \"chambon@abes.fr\"], \"cc\": [\"chambon@abes.fr\", \"tcn@abes.fr\"], \"cci\": [\"chambon@abes.fr\"], \"subject\": \"Test\", \"text\": \"Test.\"}";
        MailDto mail = new ObjectMapper().readValue(json, MailDto.class);
        assertThat(mail.getTo()).isEqualTo(new String[]{"tcn@abes.fr", "chambon@abes.fr"});
        assertThat(mail.getCci()).isEqualTo(new String[]{"chambon@abes.fr"});
        assertThat(mail.getCc()).isEqualTo(new String[]{"chambon@abes.fr", "tcn@abes.fr"});
        assertThat(mail.getSubject()).isEqualTo("Test");
        assertThat(mail.getText()).isEqualTo("Test.");
    }

    @Test
    void getConf() {
        assertThat(env.getProperty("exemple.mail.host")).isEqualTo("lotus.transition-bibliographique.fr");
        assertThat(env.getProperty("exemple.mail.password")).isEqualTo("43MSokwRJ4");
        assertThat(env.getProperty("exemple.mail.username")).isEqualTo("wsmail@wsmail.abes.fr");
        assertThat(env.getProperty("exemple.mail.sender")).isEqualTo("noreply@wsmail.abes.fr");
    }

    @Test
    void checkMailWrong() throws AddressException {
        AddressException thrown = Assertions.assertThrows(AddressException.class, () -> {
            emailService.checkMail(new String[]{"test"});
        });
    }

    @Test
    void checkMail() throws AddressException {
        emailService.checkMail(new String[]{"test@mail.com"});
    }
}
