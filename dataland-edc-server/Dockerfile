FROM eclipse-temurin:17.0.4_8-jre-alpine
COPY ./build/libs/dataland-edc-server-*-all.jar /app/edc-server.jar
COPY ./vault.properties ./keystore.jks ./config.properties /app/
EXPOSE 8080
WORKDIR /app
CMD ["java", "-Dedc.fs.config=config.properties", "-Dedc.keystore=keystore.jks", "-Dedc.keystore.password=123456", "-Dedc.vault=vault.properties", "-jar", "edc-server.jar"]
