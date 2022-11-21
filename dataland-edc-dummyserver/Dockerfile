FROM eclipse-temurin:17.0.5_8-jre-alpine
COPY ./build/libs/dataland-edc-dummyserver-*.jar /app/edc-dummyserver.jar
EXPOSE 8080
WORKDIR /app

RUN apk add --no-cache curl

HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:9191/api/dataland/health | grep -q "I am alive!"

CMD ["java","-jar","edc-dummyserver.jar"]
