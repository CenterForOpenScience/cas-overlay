FROM maven:3-jdk-8 AS app

RUN mkdir -p /code
WORKDIR /code
COPY ./ /code

ARG GIT_COMMIT=
ENV GIT_COMMIT ${GIT_COMMIT}

# Artifact caching for Multi-stage Builds : https://github.com/carlossg/docker-maven/issues/36
ENV MAVEN_OPTS=-Dmaven.repo.local=/root/.m2repo/
RUN mvn clean package -P nocheck

### Dist
FROM jetty:9.3-jre8-alpine AS dist

USER root

COPY ./etc/cas.properties /etc/cas/
COPY ./etc/institutions-auth.xsl /etc/cas/
COPY ./etc/log4j2.xml /etc/cas/
COPY ./etc/services/ /etc/cas/services/
RUN chown -R jetty:jetty /etc/cas \
    && rm -f /usr/local/jetty/etc/jetty-jdbc-sessions.xml \
    && echo \
        '<?xml version="1.0"?>\
        <!DOCTYPE Configure PUBLIC "-//Jetty//Configure//EN" "http://www.eclipse.org/jetty/configure_9_3.dtd">\
        \
        <Configure id="Server" class="org.eclipse.jetty.server.Server">\
            <Set name="sessionIdManager">\
                <New id="idMgr" class="org.eclipse.jetty.server.session.JDBCSessionIdManager">\
                    <Arg>\
                        <Ref refid="Server"/>\
                    </Arg>\
                    <Set name="workerName"><Property name="jetty.jdbcSession.workerName" default="cas"/></Set>\
                    <Set name="scavengeInterval"><Property name="jetty.jdbcSession.scavenge" default="1800"/></Set>\
                    <Call name="setDriverInfo">\
                        <Arg>org.postgresql.Driver</Arg>\
                        <Arg>jdbc:postgresql://<Env name="DATABASE_HOST" default="localhost"/>:<Env name="DATABASE_PORT" default="5432"/>/<Env name="DATABASE_NAME" default="cas"/>?targetServerType=master&amp;user=<Env name="DATABASE_USER" default="postgres"/>&amp;password=<Env name="DATABASE_PASSWORD" default=""/></Arg>\
                    </Call>\
                </New>\
            </Set>\
        </Configure>' >> /usr/local/jetty/etc/jetty-jdbc-sessions.xml

USER jetty

COPY --from=app /code/cas-server-webapp/target/cas.war /var/lib/jetty/webapps/
COPY --from=app /code/cas-server-webapp/target/dependency/ /var/lib/jetty/lib/ext/

RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=http-forwarded \
    && java -jar "$JETTY_HOME/start.jar" --add-to-startd=jdbc-sessions \
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
        \
            <Ref id="Server">\
                <Call id="idMgr" name="getSessionIdManager"/>\
            </Ref>\
            <Set name="sessionHandler">\
                <New class="org.eclipse.jetty.server.session.SessionHandler">\
                    <Arg>\
                        <New id="jdbcmgr" class="org.eclipse.jetty.server.session.JDBCSessionManager">\
                            <Set name="sessionIdManager">\
                                <Ref id="idMgr"/>\
                            </Set>\
                        </New>\
                    </Arg>\
                </New>\
            </Set>\
        \
            <Get name="sessionHandler">\
                  <Get name="sessionManager">\
                      <Get name="sessionCookieConfig">\
                          <Set name="secure" type="boolean"><Env name="SESSION_SECURE_COOKIES" default="false"/></Set>\
                          <Set name="httpOnly">true</Set>\
                      </Get>\
                  </Get>\
            </Get>\
        </Configure>' >> /var/lib/jetty/webapps/cas.xml

CMD ["java","-jar","/usr/local/jetty/start.jar","-Dcas.properties.filepath=file:/etc/cas/cas.properties","-Dlog4j.configurationFile=file:/etc/cas/log4j2.xml"]

### Dev
FROM app AS dev

RUN mvn install -P nocheck

# WOKRAROUND: Force maven to install jetty/build dependencies
RUN mvn jetty:help

ENTRYPOINT []

CMD ["/usr/bin/mvn", "-pl", "cas-server-webapp", "jetty:run"]
