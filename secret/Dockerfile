FROM openjdk:21

WORKDIR /app
COPY ./build/libs/secret-1.0.0.jar /app

EXPOSE 6789

CMD ["java", "-jar", "secret-1.0.0.jar"]