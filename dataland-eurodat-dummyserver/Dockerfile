FROM ghcr.io/d-fine/datalandedc/temurinbase:latest

COPY . /app
EXPOSE 8080
WORKDIR /app
RUN ./gradlew dataland-eurodat-dummyserver:assemble --no-daemon
CMD ./gradlew dataland-eurodat-dummyserver:bootRun
