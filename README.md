# WS ENVOI DE MAIL ABES

Ce WS vous permet, via un simple appel REST, d'envoyer des mails via les serveurs de l'ABES

Pour pouvoir l'utiliser avec vos application, il est nécessaire de mettre à jour la configuration du WS

Contactez TCN ou bien mettez à jour le "application.properties" de ce projet en suivant l'exemple (c'est facile).

Pour les appels en Java, l'utilisation de dépendances peut permettre de simplifier les appels. FluentAPI par exemple rend l'écriture de ces appels POST plus court et simple.

## Exemples d'appels au WS

#### Attention : tous les paramètres (même s'ils doivent être vides) sont obligatoires dans le JSON

A noter : Tous les exemples JAVA utilisent le JSON "brut", généré directement sous forme de String.
Il peut être intéressant, pour faire du code plus élégant et pour faciliter la réutilisation, de mettre en place en Java un objet MailDTO (avec les membres App, To, Cc, etc.), qui sera ensuite converti en JSON lors de l'envoi au WS. Cela permet de travailler avec des objets plutôt que de manipuler des String potentiellement longues, et s'avère donc plus simple.

Des exemples sont disponibles dans les applications utilisant ce WS (Item, Licences Nationales, Cidemis, ...)

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

* JAVA : 
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
* JAVA avec Spring :
```
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/simpleMail";
        String requestJson = "{\n" +
                "\t\"to\": [\"tcn@abes.fr\", \"chambon@abes.fr\"],\n" +
                "\t\"cc\": [\"chambon@abes.fr\", \"tcn@abes.fr\"],\n" +
                "\t\t\"cci\": [\"chambon@abes.fr\"],\n" +
                "\t\"subject\": \"Test\",\n" +
                "\t\"text\": \"Ceci est un test.\"\n" +
                "}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        HttpEntity<String> entity = new HttpEntity<String>(requestJson,headers);
        String answer = restTemplate.postForObject(url, entity, String.class);
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

* JAVA : 
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
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/htmlMail";
        String requestJson = "{\n" +
                "\t\"to\": [\"tcn@abes.fr\", \"chambon@abes.fr\"],\n" +
                "\t\"cc\": [\"chambon@abes.fr\", \"tcn@abes.fr\"],\n" +
                "\t\t\"cci\": [\"chambon@abes.fr\"],\n" +
                "\t\"subject\": \"Test\",\n" +
                "\t\"text\": \"<b>Ceci est un test</b>.\"\n" +
                "}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        HttpEntity<String> entity = new HttpEntity<String>(requestJson,headers);
        String answer = restTemplate.postForObject(url, entity, String.class);
```

### /htmlMailAttachment

* JAVA : 
```
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost uploadFile = new HttpPost("http://localhost:8080/htmlMailAttachment");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("mail", "{\n" +
                "\t\"to\": [\"tcn@abes.fr\", \"chambon@abes.fr\"],\n" +
                "\t\"cc\": [\"chambon@abes.fr\"],\n" +
                "\t\t\"cci\": [\"chambon@abes.fr\"],\n" +
                "\t\"subject\": \"Objet du mail\",\n" +
                "\t\"text\": \"Exemple de contenu, avec <b>HTML</b>.\"\n" +
                "}", ContentType.TEXT_PLAIN);

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
```
### /v2/htmlMailAttachment

Même fonctionnement que la v1 au dessus, mais supporte les pièces jointes multiples (max : 10Mo)

### /test-default /test-eole

Cette route permet de tester que l'API arrive à envoyer un email sans erreurs.
Les URL sont https://apicom.sudoc.fr/wsmail/test-default et https://apicom.sudoc.fr/wsmail/test-eole (accessible uniquement en interne à la date du 10/04/2020).
Ces URLs sont utilisées par une sonde uptimerobot pour surveiller que le service d'envoi de mail est opérationnel.

La route test-eole permet de continuer à surveiller le serveur de mail eole le temps que la migration de tous les envoi de mail sur lotus soit effectuée, et devrait être supprimée par la suite.


## Mémo pour compiler et exécuter l'application en local

Pour compiler le war avec maven :
```
mvn package
```

Pour lancer les tests avec maven :
```
mvn test
```

Pour lancer un serveur tomcat vierge via docker (CTRL+C pour le stopper) :
```
docker run \
    --rm \
    -e TZ="Europe/Paris" \
    -p 8080:8080 \
    --name my-docker-tomcat \
    tomcat:9-jdk11
```

Ensuite dans un autre terminal copier le WAR pour qu'il se déploie dans le tomcat (répéter l'opération après chaque `mvn package` et attendez quelques secondes pour que tomcat prenne en compte le nouveau WAR) :
```
docker cp target/wsmail-1.0.0-SNAPSHOT.war my-docker-tomcat:/usr/local/tomcat/webapps/ROOT.war
```

Ensuite on peut appeler par exemple la route /test-default, en se rendant sur http://localhost:8080/test-default
