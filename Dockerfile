FROM maven:3.3-jdk-8 AS app

RUN mkdir -p /code
WORKDIR /code
COPY ./ /code

ARG GIT_COMMIT=
ENV GIT_COMMIT ${GIT_COMMIT}
ENV OSF_URL="jdbc:postgresql://postgres:5432/osf?targetServerType=master"
ENV OSF_USER="postgres"
ENV OSF_PASSWORD=""
ENV DB_HOST="postgres"
ENV DB_PORT="5432"
ENV DB_NAME="cas"
ENV DB_USER="postgres"
ENV DB_PASSWORD=""

# Artifact caching for Multi-stage Builds : https://github.com/carlossg/docker-maven/issues/36
ENV MAVEN_OPTS=-Dmaven.repo.local=/root/.m2repo/

RUN mvn clean package -P nocheck

### Dist
FROM jetty:9.3-jre8-alpine AS dist

USER root

COPY ./etc/cas.properties ./etc/institutions-auth.xsl ./etc/log4j2.xml /etc/cas/
COPY ./etc/services /etc/cas/services
RUN mkdir -p /home/jetty \
    && chown -R jetty:jetty /home/jetty /etc/cas

USER jetty

COPY --from=app /code/cas-server-webapp/target/cas.war /var/lib/jetty/webapps/

RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=http-forwarded \
    && sed -i \
        -e "s|^osf.api.institutions.auth.xslLocation=.*|osf.api.institutions.auth.xslLocation=file:/etc/cas/institutions-auth.xsl|" \
        -e "s|^log4j.config.location=.*|log4j.config.location=file:/etc/cas/log4j2.xml|" \
        -e "s|^service.registry.config.location=.*|service.registry.config.location=file:/etc/cas/services|" \
        /etc/cas/cas.properties

RUN echo \
        '<?xml version="1.0" encoding="UTF-8"?>\
        <!DOCTYPE Configure PUBLIC\
            "-//Mort Bay Consulting//DTD Configure//EN"\
            "http://www.eclipse.org/jetty/configure_9_0.dtd">\
        \
        <Configure class="org.eclipse.jetty.webapp.WebAppContext">\
            <Set name="allowDuplicateFragmentNames">false</Set>\
            <Set name="contextPath">/</Set>\
            <Set name="war">/var/lib/jetty/webapps/cas.war</Set>\
        \
            <Call name="setAttribute">\
                <Arg>org.eclipse.jetty.server.webapp.WebInfIncludeJarPattern</Arg>\
                <Arg>.*/spring-security[^/]*\.jar$|.*/spring-web[^/]*\.jar$|.*/classes/.*</Arg>\
            </Call>\
        </Configure>' >> /var/lib/jetty/webapps/cas.xml

CMD ["java","-jar","/usr/local/jetty/start.jar","-Dcas.properties.filepath=file:/etc/cas/cas.properties","-Dlog4j.configurationFile=file:/etc/cas/log4j2.xml"]

### Dev
FROM app AS dev

RUN mvn install -P nocheck

# WOKRAROUND: Force maven to install jetty/build dependencies
RUN mvn install jetty:help

ENTRYPOINT []

CMD ["/usr/bin/mvn", "-pl", "cas-server-webapp", "jetty:run", "-DOSF_DB_URL=${OSF_URL}", "-DOSF_DB_USER=${OSF_USER}", "-DOSF_DB_PASSWORD=${OSF_PASSWORD}", "-DDATABASE_HOST=${DB_HOST}", "-DDATABASE_PORT=${DB_PORT}", "-DDATABASE_NAME=${DB_NAME}", "-DDATABASE_USER=${DB_USER}", "-DDATABASE_PASSWORD=${DB_PASSWORD}"]