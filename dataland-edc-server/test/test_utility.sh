#!/bin/bash

is_edc_server_up_and_healthy () {
  local server_uri="${1:-localhost}"
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

export dataland_tunnel_uri=tunnel.dataland.com
export dataland_tunnel_startup_link=$TUNNEL_STARTUP_LINK
export dataland_edc_server_web_http_port=9191

dataland_edc_server_uri=tunnel.dataland.com
eurodat_health_endpoint="${TRUSTEE_BASE_URL}/${TRUSTEE_ENVIRONMENT_NAME}/api/check/health"

ssh_http_control_path=/tmp/.ssh_tunnel_control_http_port
ssh_ids_control_path=/tmp/.ssh_tunnel_control_ids_port

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

acquire_ssh_tunnel () {
  local host=$1
  echo "Open SSH tunnels between tunnel server and the host system."
  ssh -R \*:"$dataland_edc_server_web_http_port":"$host":"$config_web_http_port" -S $ssh_http_control_path -M -fN ubuntu@"$dataland_tunnel_uri"
  ssh -R \*:"$dataland_edc_server_web_http_ids_port":"$host":"$config_web_http_ids_port" -S $ssh_ids_control_path -M -fN ubuntu@"$dataland_tunnel_uri"
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

checkTestCondition () {
  local maxNumberOfRetries=10
  local inputErrorMessage=$1
  local i=0
  echo "Checking log file $edc_log_file for message $inputErrorMessage"
  while [[ $(grep -q "$inputErrorMessage" "$edc_log_file"; echo $?) -eq 1 && $i -lt $maxNumberOfRetries ]]
  do
    echo "No result yet (iteration $i), waiting."
    sleep 1
    i=$((i+1))
  done

  if [[ $i -eq $maxNumberOfRetries ]]; then
    echo "Test timed out."
    exit 1
  fi

  echo "Test was successful"
}

execute_eurodat_test () {
  echo "Checking health endpoint via tunnel server."
  timeout 60 bash -c "is_edc_server_up_and_healthy \"$dataland_edc_server_uri\"" || exit 1

  data_url="http://${dataland_edc_server_uri}:${dataland_edc_server_web_http_port}/api/dataland/data"
  echo "Using $data_url for requests."

  test_data="Test Data from: "$(date "+%d.%m.%Y %H:%M:%S")
  start_time=$(date +%s)
  edc_log_file="../../edc_server.log"

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
  curl -s --max-time 780 -X GET "$data_url/$test_broken_data"
  checkTestCondition "Error getting Asset with data ID $test_broken_data from EuroDat."

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

  echo "Shutting down tunnel"
  ssh -S $ssh_http_control_path -O exit ubuntu@"$dataland_tunnel_uri"
  ssh -S $ssh_ids_control_path -O exit ubuntu@"$dataland_tunnel_uri"

  echo "Retrieving test data."
  curl --max-time 780 -X GET "http://localhost:${dataland_edc_server_web_http_port}/api/dataland/data/$dataId" -H "accept: application/json"
  checkTestCondition "Errormessage: Condition with org.dataland.edc.server.utils.AwaitUtils was not fulfilled within 1 minutes"

  echo "Test complete"
}
