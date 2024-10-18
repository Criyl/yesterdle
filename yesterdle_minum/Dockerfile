FROM maven:3.9.6-sapmachine-21

COPY . /opt/project
WORKDIR /opt/project

RUN mvn package && mv ./target/yesterdle-jar-with-dependencies.jar yesterdle.jar 

ENTRYPOINT ["java", "-Xms512m", "-Xmx2048m", "-cp", "./yesterdle.jar"]
CMD ["codes.carroll.Main"]
