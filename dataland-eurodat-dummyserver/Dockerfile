FROM ghcr.io/d-fine/datalandedc/temurinbase:latest
COPY . /app/
EXPOSE 8080
WORKDIR /app
CMD ./gradlew dataland-eurodat-dummyserver:bootRun --no-daemon
