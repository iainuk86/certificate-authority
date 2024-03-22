FROM openjdk:21

WORKDIR /app
COPY ./build/libs/secret-*.jar /app/secret.jar

EXPOSE 6789

CMD ["java", "-jar", "secret.jar"]