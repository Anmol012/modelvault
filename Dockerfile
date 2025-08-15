FROM openjdk:21-jre-slim

WORKDIR /app

COPY target/modelvault.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]