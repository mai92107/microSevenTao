FROM openjdk:21-jdk-slim

WORKDIR /app

COPY room-user-service.jar room-user-service.jar

EXPOSE 8085

ENV SPRING_DATASOURCE_URL=jdbc:mysql://room-user-mysql:3306/roomData

ENV REDIS_DATASOURCE_URL=redis-sentinel1:26379,redis-sentinel2:26380

ENTRYPOINT ["java", "-jar", "room-user-service.jar"]