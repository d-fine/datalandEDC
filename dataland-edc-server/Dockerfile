FROM eclipse-temurin:11.0.15_10-jre-alpine
COPY ./build/libs/dataland-edc-server-*.jar /app/edc-server.jar
EXPOSE 8080
WORKDIR /app
CMD ["java","-jar","edc-server.jar"]
