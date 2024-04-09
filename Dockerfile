###
# Image pour la compilation
FROM maven:3-eclipse-temurin-21 as build-image
WORKDIR /build/
# Installation et configuration de la locale FR
RUN apt update && DEBIAN_FRONTEND=noninteractive apt -y install locales
RUN sed -i '/fr_FR.UTF-8/s/^# //g' /etc/locale.gen && \
    locale-gen
ENV LANG fr_FR.UTF-8
ENV LANGUAGE fr_FR:fr
ENV LC_ALL fr_FR.UTF-8


# On lance la compilation Java
# On débute par une mise en cache docker des dépendances Java
# cf https://www.baeldung.com/ops/docker-cache-maven-dependencies
COPY ./pom.xml /build/pom.xml
RUN mvn verify --fail-never
# et la compilation du code Java
COPY ./src/   /build/src/
RUN mvn --batch-mode -e \
        -Dmaven.test.skip=false \
        -Duser.timezone=Europe/Paris \
        -Duser.language=fr \
        package


###
# Image pour le module API
#FROM tomcat:9-jdk11 as api-image
#COPY --from=build-image /build/web/target/*.war /usr/local/tomcat/webapps/ROOT.war
#CMD [ "catalina.sh", "run" ]
FROM eclipse-temurin:21-jre as api-recherche-image
WORKDIR /app/
COPY --from=build-image /build/target/*.jar /app/ws-mail.jar
ENTRYPOINT ["java","-jar","/app/theses-api-recherche.jar"]