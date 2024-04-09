# WS ENVOI DE MAIL ABES

Ce WS vous permet, via un simple appel REST, d'envoyer des mails via les serveurs de l'ABES

Pour pouvoir l'utiliser avec vos application, il est recommandé de mettre à jour la configuration du WS pour ajouter un compte spécifique à votre application. Voir [Mettre à jour la configuration](#mise-à-jour-de-la-configuration-pour-ajouter-une-application-appelante)
Sinon, les identifiants par défaut seront utilisés.

## Exemples d'appels au WS

#### Attention : tous les paramètres (même s'ils doivent être vides) sont obligatoires dans le JSON

A noter : En JAVA, vous pouvez créer un objet (classe ou record) MailDto, qui sera ensuite transformé en JSON lors de l'appel au WS, ou bien dans les cas simple vous pouvez écrire directement le JSON dans une String (mais cela peut s'avérer moins lisible et plus difficile à maintenir). Voir les exemples avec et sans MailDto, ci dessous.

Des exemples sont disponibles dans les applications utilisant ce WS (Item, Licences Nationales, Cidemis; Convergence, ...)

### /simpleMail
* JS : 
JSON à utiliser en body d'une requête HTTP POST : 

```
{ 
	"app": "monApplication",
	"to": ["mail@abes.fr", "mail2@abes.fr"],
	"cc": ["mail3@abes.fr"],
	"cci": [],
	"subject": "Objet du mail",
	"text": "Contenu du mail, pas de  HTML possible."
}
```

* JAVA (sans objet MailDto) : 
```
        URL url = new URL("http://localhost:8080/htmlMail");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        String input = "{ \n" +
                "\t\"app\": \"monApplication\",\n" +
                "\t\"to\": [\"tcn@abes.fr\"],\n" +
                "\t\"cc\": [],\n" +
                "\t\"cci\": [],\n" +
                "\t\"subject\": \"Objet du mail\",\n" +
                "\t\"text\": \"Contenu du mail\"\n" +
                "}";

        OutputStream os = conn.getOutputStream();
        os.write(input.getBytes());
        os.flush();

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            //Gestion de l'erreur
        }
```
* JAVA avec Spring (et objet MailDto) :

```        
	@Getter
	@Setter
	public class MailDto {
	    private String app;
	    private String[] to;
	    private String[] cc;
	    private String[] cci;
	    private String subject;
	    private String text;
	}
```
  
```
       

	//Creation du JSON à partir de l'objet MailDto
        String json = "";
        ObjectMapper mapper = new ObjectMapper();
        MailDto mail = new MailDto();
        mail.setApp("monApp");
        mail.setTo(["test@mail.com"]);
        mail.setCc(new String[]{});
        mail.setCci(new String[]{});
        mail.setSubject("sujet");
        mail.setText("contenu");
        try {
            json = mapper.writeValueAsString(mail);
        } catch (JsonProcessingException e) {
            log.error("Erreur lors du la création du mail. " + e);
        }

	RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/simpleMail";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        HttpEntity<String> entity = new HttpEntity<String>(json,headers);
        String answer = restTemplate.postForObject(url, entity, String.class);
	// Gérer ensuite éventuellement les retours (200 ou erreur) etc.
```

Exemple avec cURL :

```
echo '{"app": "uptimerobot","to": ["gully@abes.fr"],"cc": [],"cci": [],"subject": "Test uptimerobot","text": "Test uptimerobot bis"}' | \
curl -v -d @- -H "Content-Type: application/json" -X POST https://apicom.sudoc.fr/wsmail/simpleMail
```

### /htmlMail
* JS : 
JSON à utiliser en body : 

```
{ 
	"app": "monApplication",
	"to": ["mail@abes.fr", "mail2@abes.fr"],
	"cc": ["mail3@abes.fr"],
	"cci": [],
	"subject": "Objet du mail",
	"text": "Contenu du mail, <p>HTML possible.</p>"
}
```

* JAVA sans objet MailDto : 
```
        URL url = new URL("http://localhost:8080/htmlMail");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        String input = "{ \n" +
                "\t\"app\": \"monApplication\",\n" +
                "\t\"to\": [\"tcn@abes.fr\"],\n" +
                "\t\"cc\": [],\n" +
                "\t\"cci\": [],\n" +
                "\t\"subject\": \"Objet du mail\",\n" +
                "\t\"text\": \"Contenu du mail, <p>HTML possible.</p>\"\n" +
                "}";

        OutputStream os = conn.getOutputStream();
        os.write(input.getBytes());
        os.flush();

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            //Gestion de l'erreur
        }
```
* JAVA avec Spring :
```        
	@Getter
	@Setter
	public class MailDto {
	    private String app;
	    private String[] to;
	    private String[] cc;
	    private String[] cci;
	    private String subject;
	    private String text;
	}
```

```
	String json = "";
        ObjectMapper mapper = new ObjectMapper();
        MailDto mail = new MailDto();
        mail.setApp("monApp");
        mail.setTo(["test@mail.com"]);
        mail.setCc(new String[]{});
        mail.setCci(new String[]{});
        mail.setSubject("sujet");
        mail.setText("contenu");
        try {
            json = mapper.writeValueAsString(mail);
        } catch (JsonProcessingException e) {
            log.error("Erreur lors du la création du mail. " + e);
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/htmlMail";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        HttpEntity<String> entity = new HttpEntity<String>(json,headers);
        String answer = restTemplate.postForObject(url, entity, String.class);
```

### /htmlMailAttachment

* JAVA avec MailDto : 
```
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost uploadFile = new HttpPost("http://localhost:8080/htmlMailAttachment");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

	//Creation du JSON à partir de l'objet MailDto
        String json = "";
        ObjectMapper mapper = new ObjectMapper();
        MailDto mail = new MailDto();
        mail.setApp("monApp");
        mail.setTo(["test@mail.com"]);
        mail.setCc(new String[]{});
        mail.setCci(new String[]{});
        mail.setSubject("sujet");
        mail.setText("contenu");
        try {
            json = mapper.writeValueAsString(mail);
        } catch (JsonProcessingException e) {
            log.error("Erreur lors du la création du mail. " + e);
        }

        builder.addTextBody("mail", json, ContentType.TEXT_PLAIN);

        // This attaches the file to the POST:
        File f = new File("test.txt");
        builder.addBinaryBody(
                "attachment",
                new FileInputStream(f),
                ContentType.APPLICATION_OCTET_STREAM,
                f.getName()
        );

        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        CloseableHttpResponse response = httpClient.execute(uploadFile);
        HttpEntity responseEntity = response.getEntity();
	// Gérer ensuite les différents retours possible (code erreur, etc)
```

* JAVA avec Spring et MailDto :
  
 ```        
	@Getter
	@Setter
	public class MailDto {
	    private String app;
	    private String[] to;
	    private String[] cc;
	    private String[] cci;
	    private String subject;
	    private String text;
	}
```
```
	//Creation du JSON à partir de l'objet MailDto
        String json = "";
        ObjectMapper mapper = new ObjectMapper();
        MailDto mail = new MailDto();
        mail.setApp("monApp");
        mail.setTo(["test@mail.com"]);
        mail.setCc(new String[]{});
        mail.setCci(new String[]{});
        mail.setSubject("sujet");
        mail.setText("contenu");
        try {
            json = mapper.writeValueAsString(mail);
        } catch (JsonProcessingException e) {
            log.error("Erreur lors du la création du mail. " + e);
        }

	//Creation de l'objet MultiPart
	MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
	//Ecrire une méthode getAttachmentFile() qui doit retourner un objet FileSystemResource
	body.add("attachment", getAttachmentFile());
	body.add("mail", json);

        builder.addTextBody("mail", json, ContentType.TEXT_PLAIN);

        String serverUrl = "http://localhost:8080/htmlMail";

	RestTemplate restTemplate = new RestTemplate();
	ResponseEntity<String> response = restTemplate.postForEntity(serverUrl, requestEntity, String.class);
	// Gérer ensuite les différents retours possible (code erreur, etc)
```

### /v2/htmlMailAttachment

Même fonctionnement que la v1 au dessus, mais supporte les pièces jointes multiples (max : 10Mo)

### /test-default /test-eole

Cette route permet de tester que l'API arrive à envoyer un email sans erreurs.
Les URL sont https://apicom.sudoc.fr/wsmail/test-default et https://apicom.sudoc.fr/wsmail/test-eole (accessible uniquement en interne à la date du 10/04/2020).
Ces URLs sont utilisées par une sonde uptimerobot pour surveiller que le service d'envoi de mail est opérationnel.

La route test-eole permet de continuer à surveiller le serveur de mail eole le temps que la migration de tous les envoi de mail sur lotus soit effectuée, et devrait être supprimée par la suite.

## Code retours et documentation OpenAPI

Une documentation OpenAPI (swagger) est disponible à l'url suivante : https://apicom.sudoc.fr/wsmail/swagger-ui/index.html

Les retours possible de l'API sont : 

* 200 : Mail envoyé
* 400 / 40X : Mauvaise requête. L'erreur sera précisée dans le corps du retour de l'API (paramètre manquant, adresse mail incorrecte, PJ trop volumineuse, etc)
* 503 / 50X : Service indisponible. L'API est indisponible, consultez les logs si vous y avez accès, ou bien rapprochez vous du responsable de l'application.

## Mise à jour de la configuration pour ajouter une application appelante

Si vous souhaitez envoyer des mails avec un compte propre à votre application, et personnaliser l'adresse d'expédition, il faut ajouter votre application dans la configuration du WS.
Cela se fait en ajoutant dans les fichier .env (sur diplotaxis2, dev test prod) les informations suivantes : 
```
monApp.mail.host=serveurmail.fr
monApp.mail.username=exemple@abes.fr
monApp.mail.password=motDePasse
monApp.mail.sender=exemple@abes.fr
```

Ensuite, il faut mettre à jour le fichier docker-compose.yml (disponible ici : https://github.com/abes-esr/ws-mail-docker ) pour y ajouter ces paramètres : 
```
#MONAPP
MONAPP_MAIL_HOST: ${WSMAIL_MONAPP_MAIL_HOST}
MONAPP_MAIL_USERNAME: ${WSMAIL_MONAPP_MAIL_USERNAME}
MONAPP_MAIL_PASSWORD: ${WSMAIL_MONAPP_MAIL_PASSWORD}
MONAPP_MAIL_SENDER: ${WSMAIL_MONAPP_MAIL_SENDER}
```

Vous pourrez ensuite simplement, dans le JSON à envoyer au WS, utiliser le nom de votre app (monApp dans l'exemple) dans le champ "app" afin d'utiliser ces paramètres personnalisés.
