#!/bin/bash

is_edc_server_up_and_healthy () {
  server_uri="${1:-localhost}"
  health_response=$(curl -s -f -X GET "http://${server_uri}:${dataland_edc_server_web_http_port}/api/dataland/health" -H "accept: application/json")
  if [[ ! $health_response =~ "I am alive!" ]]; then
    return 1
  fi
}
export -f is_edc_server_up_and_healthy

is_tunnel_server_up () {
  if ! ssh -o ConnectTimeout=10 ubuntu@"$dataland_tunnel_uri" "echo Connected to tunnel server"; then
    curl "$dataland_tunnel_startup_link"
    return 1
  fi
}
export -f is_tunnel_server_up

export dataland_edc_server_web_http_ids_port=9292
export config_web_http_port=9191
export config_web_http_ids_port=9292

export dataland_tunnel_uri=dataland-tunnel.duckdns.org
export dataland_tunnel_startup_link=$TUNNEL_STARTUP_LINK
export dataland_edc_server_web_http_port=9191

dataland_edc_server_uri=dataland-tunnel.duckdns.org
eurodat_health_endpoint="${TRUSTEE_BASE_URL}/${TRUSTEE_ENVIRONMENT_NAME}/api/check/health"

restart_tunnel_server () {
  echo "Stop tunnel server if it is currently running."
  if ssh ubuntu@"$dataland_tunnel_uri" "sudo shutdown now"; then
    echo "Tunnel server was running and has been stopped."
    sleep 5
  fi

  echo "Starting tunnel server."
  curl "$dataland_tunnel_startup_link"
  sleep 10

  echo "Checking availability of tunnel server."
  timeout 240 bash -c "while ! is_tunnel_server_up; do echo 'Tunnel server not yet there - retrying in 10s'; sleep 10; done; echo 'Tunnel server up!'"
}

is_eurodat_up_and_healthy () {
  echo "Checking if EuroDaT is available. Curling $(echo $eurodat_health_endpoint | base64)"
  if ! curl -f -X 'GET' "$eurodat_health_endpoint" -H 'accept: application/json' 2>/dev/null | grep -q '"isHealthy":true}],"isSystemHealthy":true}'; then
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
  export edc_server_pid=$!
  cd "$current_dir" || exit

  echo "Checking health endpoint of dataland edc server locally."
  timeout 240 bash -c "while ! is_edc_server_up_and_healthy; do echo 'Dataland EDC server not yet there - retrying in 10s'; sleep 10; done; echo 'Dataland EDC server up!'"
}

execute_eurodat_test () {
  echo "Checking health endpoint via tunnel server."
  timeout 60 bash -c "is_edc_server_up_and_healthy \"$dataland_edc_server_uri\"" || exit 1

  data_url="http://${dataland_edc_server_uri}:${dataland_edc_server_web_http_port}/api/dataland/data"
  echo "Using $data_url for requests."

  test_data="Test Data from: "$(date "+%d.%m.%Y %H:%M:%S")
  start_time=$(date +%s)

  echo "Posting test data: $test_data."
  response=$(curl --max-time 780 -X POST "$data_url" -H "accept: application/json" -H "Content-Type: application/json" -d "$test_data")
  regex="\"dataId\":\"([0-9a-f:\-]+_[0-9a-f\-]+)\""
  if [[ $response =~ $regex ]]; then
    dataId=${BASH_REMATCH[1]}
  else
    echo "Unable to extract data ID from response: $response"
    exit 1
  fi
  echo "Received response from post request with data ID: $dataId"

  echo "Retrieving test data."
  get_response=$(curl --max-time 780 -X GET "$data_url/$dataId" -H "accept: application/json")
  if [[ ! $get_response =~ $test_data ]]; then
    echo "Response was unexpected: $get_response"
    echo "Expected was substring: $test_data"
    exit 1
  fi
  echo "Retrieved data is: $get_response"

  runtime=$(($(date +%s) - start_time))

  echo "Test successfully run. Up- and download took $runtime seconds."

  echo "Testing wrong data id response"
  test_broken_data="47t67dgxesy"
  curl --max-time 780 -X GET "$data_url/$test_broken_data"
  if ! grep -q "Error getting Asset with data ID $test_broken_data from EuroDat." ../../edc_server.log; then
    echo "Unexpected response"
    exit 1
  else
    echo "Test of invalid data id was successfull"
  fi

  echo "Testing get request to eurodat with wrong data id"
    test_broken_data="trze648fksaasy"
    get_response=$(curl -X 'GET' "http://${dataland_edc_server_uri}:${dataland_edc_server_web_http_port}/api/dataland/eurodat/asset/$test_broken_data" -H "eurodat-asset-id: af8baf3c-" -H "eurodat-contract-definition-id: fe8a7ad6")
    echo $get_response
    if [[ ! $get_response == "" ]]; then
      echo "Unexpected response"
      exit 1
    else
      echo "Test of invalid data request to eurodat was successfull"
    fi

  echo "Testing 400 error on unexpected asset transmission"
    if ! curl -X 'POST' "http://${dataland_edc_server_uri}:${dataland_edc_server_web_http_port}/api/dataland/eurodat/asset/non-existent" | grep -q '400'; then
      echo "ERROR: Did not receive 400 Response"
      exit 1
    fi

  echo "Testing metaawait timeout"

    echo "Posting test data: $test_data."
      response=$(curl --max-time 780 -X POST "$data_url" -H "accept: application/json" -H "Content-Type: application/json" -d "$test_data")
      regex="\"dataId\":\"([0-9a-f:\-]+_[0-9a-f\-]+)\""
      if [[ $response =~ $regex ]]; then
        dataId=${BASH_REMATCH[1]}
      else
        echo "Unable to extract data ID from response: $response"
        exit 1
      fi
    echo "Received response from post request with data ID: $dataId"

    echo "Killing tunnel"
    ps -ef
    ps -ef | grep ssh
    ps -ef | grep :9191:localhost:9191
    ps -ef | grep :9292:localhost:9292

    echo "Retrieving test data."
    curl --max-time 780 -X GET "http://${server_uri}:${dataland_edc_server_web_http_port}/api/dataland/data/$dataId" -H "accept: application/json"
    if ! grep -q "Errormessage: Condition with org.dataland.edc.server.utils.AwaitUtils was not fulfilled within 1 minutes." ../../edc_server.log; then
      echo "Response was unexpected: $get_response"
      exit 1
    fi
    echo "Timeout Test was successfull"

  echo "Test complete"
}
