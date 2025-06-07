FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} ms-admin.jar
ENTRYPOINT ["java", "-jar","/ms-admin.jar"]