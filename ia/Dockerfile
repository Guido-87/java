FROM czy21/jdk-23
ARG JAR_FILE=target/ia-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app_ia.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app_ia.jar"]