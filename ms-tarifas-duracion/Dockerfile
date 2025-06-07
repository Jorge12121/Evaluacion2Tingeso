FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} ms-tarifas-duracion.jar
ENTRYPOINT ["java", "-jar","/ms-tarifas-duracion.jar"]