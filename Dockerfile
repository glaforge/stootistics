FROM openjdk:17-slim
WORKDIR /app
COPY ./micronaut-backend/build/libs/micronaut-backend-0.1-all.jar /app/
EXPOSE 8080
CMD ["java", "-jar", "/app/micronaut-backend-0.1-all.jar"]
