FROM ghcr.io/d-fine/datalandedc/temurinbase:latest
COPY . /app/
EXPOSE 8080
WORKDIR /app
RUN ./gradlew dataland-edc-dummyserver:assemble --no-daemon
#CMD tail -f /dev/null