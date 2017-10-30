FROM java:8-jdk

RUN mkdir -p /var/www && chown www-data:www-data /var/www

RUN apt-get update \
    && apt-get install -y \
      maven \
    && apt-get clean \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/*

# grab gosu for easy step-down from root
ENV GOSU_VERSION 1.9
RUN apt-get update \
    && apt-get install -y \
        curl \
    && gpg --keyserver pool.sks-keyservers.net --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4 \
    && curl -o /usr/local/bin/gosu -SL "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$(dpkg --print-architecture)" \
    && curl -o /usr/local/bin/gosu.asc -SL "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$(dpkg --print-architecture).asc" \
    && gpg --verify /usr/local/bin/gosu.asc \
    && rm /usr/local/bin/gosu.asc \
    && chmod +x /usr/local/bin/gosu \
    && apt-get clean \
    && apt-get autoremove -y \
        curl \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /code
WORKDIR /code
COPY ./ /code
RUN chown -R www-data:www-data /code

ARG GIT_COMMIT=
ENV GIT_COMMIT ${GIT_COMMIT}

RUN gosu www-data mvn clean install -DskipTests=true
# Forces all jetty dependancies to install
RUN gosu www-data mvn jetty:help

# ENV MAVEN_OPTS=# "-Xms256m -Xmx512m"
CMD ["gosu", "www-data", "/usr/bin/mvn", "-pl", "cas-server-webapp", "jetty:run"]
