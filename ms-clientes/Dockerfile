FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} ms-cliente.jar
ENTRYPOINT ["java", "-jar","/ms-cliente.jar"]