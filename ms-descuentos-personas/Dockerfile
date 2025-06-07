FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} ms-descuentos-personas.jar
ENTRYPOINT ["java", "-jar","/ms-descuentos-personas.jar"]