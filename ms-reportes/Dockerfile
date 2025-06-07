FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} ms-reportes.jar
ENTRYPOINT ["java", "-jar","/ms-reportes.jar"]