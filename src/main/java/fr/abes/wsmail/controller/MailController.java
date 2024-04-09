package fr.abes.wsmail.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.wsmail.mail.EmailService;
import fr.abes.wsmail.model.MailDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.validation.constraints.NotNull;
import javax.websocket.server.PathParam;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@RestController
public class MailController {

    private static final String OK_MESSAGE = "Mail envoyé.";
    private static final String OK_TEST = "OK";

    private final EmailService emailService;

    @Value("${error.filepath}")
    private String errorLog;

    @Autowired
    public MailController(EmailService emailService) {
        this.emailService = emailService;
    }


    @ApiOperation(value = "Envoi d'un mail basique (texte simple, sans HTML).")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Mail envoyé."),
            @ApiResponse(code = 503, message = "Service indisponible."),
            @ApiResponse(code = 400, message = "Mauvaise requête. Le paramètre problématique sera précisé par le message d'erreur. Par exemple : paramètre manquant, adresse erronnée...")})
    @PostMapping(value = "/simpleMail")
    @ResponseBody
    public String createSimpleMail(@ApiParam(value = "Objet JSON contenant les informations sur le mail a envoyer. Tous les champs sont nécessaires mais peuvent être null/vide. Les to/cc/cci sont des tableaux de String. App correspond au nom de votre application et doit correspondre à la configuration ajoutée dans le WS.", required = true) @PathParam("mail")
                                   @RequestBody @NotNull MailDto mail) throws AddressException {
        setConf(mail.getApp());
        try {
            emailService.sendSimpleMessage(mail.getTo(), mail.getCc(), mail.getCci(), mail.getSubject(), mail.getText());
        } catch (Exception e) {
            logErrorMail(mail);
            throw e;
        }

        return OK_MESSAGE;
    }

    @ApiOperation(value = "Envoi d'un mail classique, texte et HTML possible.")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Mail envoyé."),
            @ApiResponse(code = 503, message = "Service indisponible."),
            @ApiResponse(code = 400, message = "Mauvaise requête. Le paramètre problématique sera précisé par le message d'erreur. Par exemple : paramètre manquant, adresse erronnée...")})
    @PostMapping(value = "/htmlMail")
    @ResponseBody
    public String createHtmlMail(@ApiParam(value = "Objet JSON contenant les informations sur le mail a envoyer. Tous les champs sont nécessaires mais peuvent être null/vide. Les to/cc/cci sont des tableaux de String. App correspond au nom de votre application et doit correspondre à la configuration ajoutée dans le WS.", required = true) @PathParam("mail")
                                 @RequestBody @NotNull MailDto mail) throws MessagingException {
        setConf(mail.getApp());
        try {
            emailService.sendHtmlMessage(mail.getTo(), mail.getCc(), mail.getCci(), mail.getSubject(), mail.getText());
        } catch (Exception e) {
            logErrorMail(mail);
            throw e;
        }

        return OK_MESSAGE;
    }

    @ApiOperation(value = "Envoi d'un mail classique, texte et HTML possible. Ce endpoint prend en entrée un Form Multipart, contenant dans mailJSON un objet JSON correspondant aux infos du mails (voir les autres WS), ainsi que plusieurs pièces jointes au format MultipartFile, dans un tableau")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Mail envoyé."),
            @ApiResponse(code = 503, message = "Service indisponible."),
            @ApiResponse(code = 400, message = "Mauvaise requête. Le paramètre problématique sera précisé par le message d'erreur. Par exemple : paramètre manquant, adresse erronnée, PJ trop volumineuse...")})
    @PostMapping(value = "/htmlMailAttachment", headers = {"content-type=multipart/mixed", "content-type=multipart/form-data"})
    public String createHtmlMailWithMultipleAttachment(@ApiParam(value = "Objet JSON contenant les informations sur le mail a envoyer. Tous les champs sont nécessaires mais peuvent être null/vide. Les to/cc/cci sont des tableaux de String. App correspond au nom de votre application et doit correspondre à la configuration ajoutée dans le WS.", required = true) @PathParam("mailJSON")
                                               @RequestParam("mail") @NotNull String mailJSON,
                                               @ApiParam(value = "Pièces jointes. C'est un MultipartFile. Il peut être vide si pas de PJ nécessaire. La taille maximale des pièces jointes est de 10Mo.", required = true) @PathParam("attachment")
                                               @RequestParam("attachment") MultipartFile[] attachment) throws IOException, MessagingException {

        MailDto mail = new ObjectMapper().readValue(mailJSON, MailDto.class);

        try {
            setConf(mail.getApp());
            emailService.sendMessageWithMultipleAttachment(mail.getTo(), mail.getCc(), mail.getCci(), mail.getSubject(), mail.getText(), attachment);
        } catch (Exception e) {
            logErrorMail(mail);
            throw e;
        }

        return OK_MESSAGE;
    }

    /**
     * WS de test (/test-eole et /test-lotus), utilisés par uptimerobot,
     * Il permettent de savoir si le serveur mail répond.
     *
     * @return OK si tout répond, une erreur sinon
     * @throws AddressException Exception si l'adresse mail est malformée
     */
    @GetMapping(value = "/test-eole")
    public String testEndpoint() throws AddressException {

        //log.info("coucou from eole !!!!!");

        MailDto m = new MailDto();
        m.setApp("eole");
        m.setTo(new String[]{"uptimerobot-noreply@abes.fr"});
        m.setCc(new String[]{});
        m.setCci(new String[]{});
        m.setSubject("Test uptimerobot");
        m.setText("test uptimerobot");

        this.createSimpleMail(m);
        return OK_TEST;
    }

    @GetMapping(value = "/test-default")
    public String testLotusEndpoint() throws AddressException {

        //log.info("coucou from lotus !!!!!");

        MailDto m = new MailDto();
        m.setTo(new String[]{"uptimerobot-noreply@abes.fr"});
        m.setCc(new String[]{});
        m.setCci(new String[]{});
        m.setSubject("Test uptimerobot");
        m.setText("test uptimerobot");

        this.createSimpleMail(m);
        return OK_TEST;
    }

    /**
     * Appelle la méthode setConf du service de mail, qui met à jour la configuration en fonction de l'application appelante
     *
     * @param appName nom de l'application qui appelle le WS
     */
    private void setConf(String appName) {
        this.emailService.setConf(appName);
    }

    /**
     * Exception handler générique, en cas de problème serveur
     *
     * @param ex Exception levée
     * @return Message d'erreur / code 503
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        log.error(ex.getClass() + ": " + ex.getMessage() + ": " + ex.getCause(), ex);
        return new ResponseEntity<>("Une erreur est survenue. Merci de réessayer ultérieurement.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Exception handler si requête malformée (paramètres manquants ou mauvais type par ex)
     *
     * @param ex Exception levée
     * @return Message d'erreur / code 400
     */
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<String> handleException(IllegalArgumentException ex) {
        log.error(ex.getClass() + ": " + ex.getMessage() + ": " + ex.getCause(), ex);
        return new ResponseEntity<>("Erreur dans la requête : " + ex.toString(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Exception handler si requête malformée (PJ manquante)
     *
     * @param ex Exception levée
     * @return Message d'erreur / code 400
     */
    @ExceptionHandler({MissingServletRequestPartException.class})
    public ResponseEntity<String> handleException(MissingServletRequestPartException ex) {
        log.error(ex.getClass() + ": " + ex.getMessage() + ": " + ex.getCause(), ex);
        return new ResponseEntity<>("Erreur dans la requête : " + ex.toString(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Exception handler si adresse mail malformée
     *
     * @param ex Exception levée
     * @return Message d'erreur / code 400
     */
    @ExceptionHandler(AddressException.class)
    public ResponseEntity<String> handleException(AddressException ex) {
        log.error(ex.getClass() + ": " + ex.getMessage() + ": " + ex.getCause(), ex);
        return new ResponseEntity<>("Adresse mail malformée : " + ex.toString(), HttpStatus.BAD_REQUEST);
    }


    /**
     * Exception handler si pièce jointe trop volumineuse
     * Penser à bien configrer le tomcat (voir commentaire ci-dessous)
     *
     * @param ex Exception levée
     * @return Message d'erreur / code 400
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleException(MaxUploadSizeExceededException ex) {
    /* Conf Tomcat maxSwallowSize nécessaire pour gérer cette exception :
    <Connector port="8080" protocol="HTTP/1.1"
    connectionTimeout="20000"
    redirectPort="8443"
    maxSwallowSize = "-1"/>
     */
        log.error(ex.getClass() + ": " + ex.getMessage() + ": " + ex.getCause(), ex);
        return new ResponseEntity<>("Pièce jointe trop volumineuse. 10Mo maximum. : " + ex.toString(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Exception handler si un des arguments n'est pas au bon format dans le JSON, et donc qu'il est impossible de le déserializer
     * @param ex Exception levée
     * @return Message d'erreur / code 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleException(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>("Un des paramètres n'est pas au bon format (les to, cc et cci sont des tableaux) : " + ex.toString(), HttpStatus.BAD_REQUEST);
    }

    public void logErrorMail(MailDto mail) {
        ObjectMapper mapper = new ObjectMapper();
        FileWriter fileWriter;

        try {
            String mailIJSON = mapper.writeValueAsString(mail);
            fileWriter = new FileWriter(errorLog, true);

            PrintWriter printWriter = new PrintWriter(fileWriter);

            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            String mailNotSent = "\n" + dateFormat.format(date) + " :" + mailIJSON;

            printWriter.append(mailNotSent);
            printWriter.close();
        } catch (IOException e) {
            log.error("Impossible de logger le mail non envoyé : " + e);
        }
    }
}
