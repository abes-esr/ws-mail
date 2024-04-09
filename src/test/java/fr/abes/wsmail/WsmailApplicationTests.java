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

import jakarta.mail.internet.AddressException;
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
        String json = "{\"to\": [\"test@abes.fr\", \"test2@abes.fr\"], \"cc\": [\"test3@abes.fr\", \"test3@abes.fr\"], \"cci\": [\"test5@abes.fr\"], \"subject\": \"Test\", \"text\": \"Test.\"}";
        MailDto mail = new ObjectMapper().readValue(json, MailDto.class);
        assertThat(mail.getTo()).isEqualTo(new String[]{"test@abes.fr", "test2@abes.fr"});
        assertThat(mail.getCci()).isEqualTo(new String[]{"test5@abes.fr"});
        assertThat(mail.getCc()).isEqualTo(new String[]{"test3@abes.fr", "test3@abes.fr"});
        assertThat(mail.getSubject()).isEqualTo("Test");
        assertThat(mail.getText()).isEqualTo("Test.");
    }

    @Test
    void getConf() {
        assertThat(env.getProperty("exemple.mail.host")).isEqualTo("serveurmail.fr");
        assertThat(env.getProperty("exemple.mail.password")).isEqualTo("motDePasse");
        assertThat(env.getProperty("exemple.mail.username")).isEqualTo("exemple@abes.fr");
        assertThat(env.getProperty("exemple.mail.sender")).isEqualTo("exemple@abes.fr");
    }

    @Test
    void checkMailWrong() {
        AddressException thrown = Assertions.assertThrows(AddressException.class, () -> {
            emailService.checkMail(new String[]{"test"});
        });
    }

    @Test
    void checkMail() throws AddressException {
        emailService.checkMail(new String[]{"test@mail.com"});
    }
}
