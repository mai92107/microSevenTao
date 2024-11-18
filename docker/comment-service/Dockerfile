FROM openjdk:21-jdk-slim

WORKDIR /app

COPY comment-service.jar comment-service.jar

EXPOSE 8084

ENV SPRING_DATASOURCE_URL=jdbc:mysql://comment-mysql:3306/commentData

ENTRYPOINT ["java", "-jar", "comment-service.jar"]
