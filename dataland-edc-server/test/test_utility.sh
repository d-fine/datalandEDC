#!/bin/bash

is_edc_server_up () {
  server_uri="${1:-localhost}"
  health_response=$(curl -s -f -X GET "http://${server_uri}:${dataland_edc_server_web_http_port}/api/dataland/health" -H "accept: application/json")
  if [[ ! $health_response =~ "I am alive!" ]]; then
    return 1
  fi
}
export -f is_edc_server_up

is_tunnel_server_up () {
  if ! ssh -o ConnectTimeout=10 ubuntu@"$dataland_tunnel_uri" "echo Connected to tunnel server"; then
    curl "$dataland_tunnel_startup_link"
    return 1
  fi
}
export -f is_tunnel_server_up

export dataland_tunnel_uri=dataland-tunnel.duckdns.org
dataland_edc_server_uri=dataland-tunnel.duckdns.org
export dataland_tunnel_startup_link=$TUNNEL_STARTUP_LINK

eurodat_health_endpoint="${TRUSTEE_WEB_URI}/ids/description"

export dataland_edc_server_web_http_port=9191
dataland_edc_server_web_http_ids_port=9292
dataland_edc_server_web_http_data_port=9393

config_web_http_port=9191
config_web_http_ids_port=9292
config_web_http_data_port=9393

is_eurodat_up () {
  echo "Checking if EuroDaT is available."
  if ! curl -f -X 'GET' "$eurodat_health_endpoint" -H 'accept: application/json' >/dev/null 2>&1; then
    echo "EuroDaT is not available."
    exit 1
  fi
  echo "EuroDat is available."
}

start_edc_server () {
  echo "Starting Dataland EDC server."
  current_dir=$(pwd)
  cd ../../ || exit
  ./gradlew :dataland-edc-server:run --stacktrace >edc_server.log 2>&1 &
  edc_server_pid=$!
  cd "$current_dir" || exit
}

execute_eurodat_test () {
  echo "Checking health endpoint via tunnel server."
  if ! is_edc_server_up "$dataland_edc_server_uri"; then
    echo "Unable to reach EDC server via tunnel."
    exit 1
  fi

  test_data="Test Data from: "$(date "+%d.%m.%Y %H:%M:%S")
  start_time=$(date +%s)

  echo "Posting test data: $test_data."
  response=$(curl -X POST "http://${dataland_edc_server_uri}:${dataland_edc_server_web_http_port}/api/dataland/data" -H "accept: application/json" -H "Content-Type: application/json" -d "$test_data")
  regex="\"([a-f0-9\-]+:[a-f0-9\-]+)\""
  if [[ $response =~ $regex ]]; then
    dataId=${BASH_REMATCH[1]}
  else
    echo "Unable to extract data ID from response: $response"
    exit 1
  fi
  echo "Received response from post request with data ID: $dataId"

  echo "Retrieving test data."
  get_response=$(curl -X GET "http://${dataland_edc_server_uri}:${dataland_edc_server_web_http_port}/api/dataland/data/$dataId" -H "accept: application/json")
  if [[ ! $get_response =~ $test_data ]]; then
    echo "Response was unexpected: $get_response"
    echo "Expected was substring: $test_data"
    exit 1
  fi
  echo "Retrieved data is: $get_response"

  runtime=$(($(date +%s) - start_time))

  echo "Test successfully run. Up- and download took $runtime seconds."
}
