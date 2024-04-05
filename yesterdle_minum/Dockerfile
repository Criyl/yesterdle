FROM maven:3.9.6-sapmachine-21

COPY . /opt/project
WORKDIR /opt/project

RUN mvn install
CMD ["java", "-Xms512m", "-Xmx2048m", "-jar", "./target/minum_server-1.0.0-jar-with-dependencies.jar", "codes.carroll.Main"]
