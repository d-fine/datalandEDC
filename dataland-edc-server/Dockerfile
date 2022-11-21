FROM eclipse-temurin:17.0.5_8-jre-alpine
COPY ./build/libs/dataland-edc-server-*-all.jar /app/edc-server.jar
COPY ./vault.properties ./keystore.jks ./config.properties /app/
EXPOSE 8080
WORKDIR /app

RUN apk add --no-cache curl

HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:9191/api/dataland/health | grep -q "I am alive!"

CMD ["java", "-Dedc.fs.config=config.properties", "-Dedc.keystore=keystore.jks", "-Dedc.keystore.password=123456", "-Dedc.vault=vault.properties", "-jar", "edc-server.jar"]
