#!/bin/bash

is_edc_server_up () {
  health_response=$(curl -s -f -X GET "http://localhost:${dataland_edc_server_web_http_port}/api/dataland/health" -H "accept: application/json")
  if [[ ! $health_response =~ "I am alive!" ]]; then
    return 1
  fi
}
export -f is_edc_server_up

is_tunnel_server_up () {
  if ! ssh ubuntu@"$dataland_tunnel_uri" "echo Connected to tunnel server"; then
    return 1
  fi
}
export -f is_tunnel_server_up

export dataland_tunnel_uri=$EDC_SERVER_URI
export dataland_edc_server_uri=$EDC_SERVER_URI
export dataland_tunnel_startup_link=$TUNNEL_STARTUP_LINK

export dataland_edc_server_web_http_port=9191
export dataland_edc_server_web_http_ids_port=9292
export dataland_edc_server_web_http_data_port=9393

export config_web_http_port=9191
export config_web_http_ids_port=9292
export config_web_http_data_port=9393

is_eurodat_up () {
  echo "Checking if EuroDaT is available."
  if ! curl -f -X 'GET' "${TRUSTEE_URI}/ids/description" -H 'accept: application/json' >/dev/null 2>&1; then
    echo "EuroDaT is not available."
    exit 1
  fi
  echo "EuroDat is available."
}

start_edc_server () {
  echo "Starting Dataland EDC server."
  ./../../gradlew :dataland-edc-server:run --stacktrace >edc_server.log 2>&1 &
  export edc_server_pid=$!
}

execute_eurodat_test () {
  echo "Checking health endpoint via tunnel server."
  health_response=$(curl -X GET "http://${dataland_edc_server_uri}:${dataland_edc_server_web_http_port}/api/dataland/health" -H "accept: application/json")
  if [[ ! $health_response =~ "I am alive!" ]]; then
    echo "Response was unexpected: $health_response"
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
