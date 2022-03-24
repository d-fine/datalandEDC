FROM ghcr.io/d-fine/datalandedc/temurinbase:latest

COPY . /app
EXPOSE 8080
WORKDIR /app
RUN ./gradlew dataland-eurodat-dummy:assemble --no-daemon
CMD ./gradlew dataland-eurodat-dummy:bootRun
