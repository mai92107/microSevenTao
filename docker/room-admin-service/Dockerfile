FROM openjdk:21-jdk-slim

WORKDIR /app

COPY room-admin-service.jar room-admin-service.jar

EXPOSE 8086

ENV SPRING_DATASOURCE_URL=jdbc:mysql://room-admin-mysql:3306/roomData

ENV REDIS_DATASOURCE_URL=redis-sentinel1:26379,redis-sentinel2:26380

ENTRYPOINT ["java", "-jar", "room-admin-service.jar"]