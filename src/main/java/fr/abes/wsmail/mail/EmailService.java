package fr.abes.wsmail.mail;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Component
public class EmailService {
    private final JavaMailSenderImpl emailSender;
    private final Environment env;
    private String from;

    @Value("${spring.mail.password}")
    private String defaultPassword;

    @Value("${spring.mail.username}")
    private String defaultUsername;

    @Value("${spring.mail.host}")
    private String defaultHost;

    @Autowired
    public EmailService(JavaMailSenderImpl emailSender, Environment env) {
        this.emailSender = emailSender;
        this.env = env;
    }

    /**
     * Envoi un mail basique (texte simple, pas de HTML)
     *
     * @param to      tableau des adresses mails des destinataires
     * @param cc      tableau des adresses mails des destinataires cc
     * @param cci     tableau des adresses mails des destinataires cci
     * @param subject objet du mail
     * @param text    contenu du mail (texte simple)
     * @throws AddressException Lève une exception si l'adresse mail est malformée
     */
    public void sendSimpleMessage(String[] to, String[] cc, String[] cci, String subject, String text) throws AddressException {
        checkMail(to);
        checkMail(cc);
        checkMail(cci);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setCc(cc);
        message.setBcc(cci);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(from);
        emailSender.send(message);
    }

    /**
     * Envoie un mail (supportant le contenu HTML)
     *
     * @param to      tableau des adresses mails des destinataires
     * @param cc      tableau des adresses mails des destinataires cc (facultatif)
     * @param cci     tableau des adresses mails des destinataires cci (facultatif)
     * @param subject objet du mail
     * @param text    contenu du mail (supporte le HTML)
     * @throws MessagingException Lève une exception si l'adresse mail est malformée, le serveur mail est indisponible, ou autre...
     */
    public void sendHtmlMessage(String[] to, String[] cc, String[] cci, String subject, String text) throws MessagingException {
        checkMail(to);
        checkMail(cc);
        checkMail(cci);

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");
        helper.setTo(to);
        if(cc != null)
            helper.setCc(cc);
        if(cci != null)
            helper.setBcc(cci);
        helper.setFrom(from);
        helper.setSubject(subject);
        helper.setText(text, true);
        emailSender.send(message);
    }

    /**
     * Envoie un mail (supportant le contenu HTML) avec une ou plusieurs pièces jointes
     *
     * @param to      tableau des adresses mails des destinataires
     * @param cc      tableau des adresses mails des destinataires cc
     * @param cci     tableau des adresses mails des destinataires cci
     * @param subject objet du mail
     * @param text    contenu du mail (supporte le HTML)
     * @param files    pièce jointe au format MultipartFile[]
     * @throws MessagingException Lève une exception si l'adresse mail est malformée, le serveur mail est indisponible, ou autre...
     */
    public void sendMessageWithMultipleAttachment(String[] to, String[] cc, String[] cci, String subject, String text, MultipartFile[] files) throws MessagingException {
        checkMail(to);
        checkMail(cc);
        checkMail(cci);

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setCc(cc);
        helper.setBcc(cci);
        helper.setFrom(from);
        helper.setSubject(subject);
        helper.setText(text, true);
        Arrays.asList(files).stream().forEach(file -> {
            try {
                helper.addAttachment(file.getOriginalFilename(), () -> file.getInputStream());
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        });


        emailSender.send(message);
    }

    /**
     * Vérifie le bon format de l'adresse mail
     *
     * @param mailArray Tableau des adresses des destinataires
     * @throws AddressException Lève une exception si l'adresse mail est malformée
     */
    public void checkMail(String[] mailArray) throws AddressException {
        if(mailArray != null)
            for (String mail : mailArray) {
                InternetAddress emailAddr = new InternetAddress(mail);
                emailAddr.validate();
            }
    }

    /**
     * Modifie la configuration du mail en allant récupérer les valeurs correspondantes au nom de l'application appelante
     * dans le application.properties
     *
     * @param appName Nom de l'application appelant le WS
     */
    public void setConf(String appName) {
        String customHost = env.getProperty(appName + ".mail.host");
        String customPassword = env.getProperty(appName + ".mail.password");
        String customUsername = env.getProperty(appName + ".mail.username");
        from = env.getProperty(appName + ".mail.sender");

        if (customHost != null && customPassword != null && customUsername != null && from != null) {
            emailSender.setPassword(customPassword);
            emailSender.setUsername(customUsername);
            emailSender.setHost(customHost);
        } else {
            emailSender.setPassword(defaultPassword);
            emailSender.setUsername(defaultUsername);
            emailSender.setHost(defaultHost);
            from = env.getProperty("spring.mail.sender");
        }

    }
}
