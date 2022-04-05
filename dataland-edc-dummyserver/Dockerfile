FROM eclipse-temurin:17.0.2_8-jre-alpine
COPY ./build/libs/dataland-edc-dummyserver-*.jar /app/edc-dummyserver.jar
EXPOSE 8080
WORKDIR /app
CMD ["java","-jar","edc-dummyserver.jar"]
