FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} ms-reservas.jar
ENTRYPOINT ["java", "-jar","/ms-reservas.jar"]