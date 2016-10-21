FROM java:8-jdk

RUN apt-get update \
    && apt-get install -y \
      maven \
    && apt-get clean \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /code
WORKDIR /code

COPY ./ /code

ARG GIT_COMMIT=
ENV GIT_COMMIT ${GIT_COMMIT}

RUN mvn clean install -DskipTests=true
# Forces all jetty dependancies to install
RUN mvn jetty:help

# ENV MAVEN_OPTS=# "-Xms256m -Xmx512m"
CMD ["/usr/bin/mvn", "-pl", "cas-server-webapp", "jetty:run"]
