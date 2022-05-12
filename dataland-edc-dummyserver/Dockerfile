FROM eclipse-temurin:11.0.15_10-jre-alpine
COPY ./build/libs/dataland-edc-dummyserver-*.jar /app/edc-dummyserver.jar
EXPOSE 8080
WORKDIR /app
CMD ["java","-jar","edc-dummyserver.jar"]
