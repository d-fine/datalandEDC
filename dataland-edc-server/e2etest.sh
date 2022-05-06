#!/bin/bash
set -eu

#trap stopEDCServer EXIT

#function stopEDCServer() {
#  kill "$edc_server_pid"
#}

#dataland_tunnel_uri=$EDC_SERVER_URI
#dataland_tunnel_startup_link=$TUNNEL_STARTUP_LINK

#config_web_http_port=9191
#config_web_http_ids_port=9292
#config_web_http_data_port=9393

#dataland_edc_server_uri=$EDC_SERVER_URI
#dataland_edc_server_web_http_port=9191
#dataland_edc_server_web_http_ids_port=9292
#dataland_edc_server_web_http_data_port=9393

#echo "Check connection to tunnel server."
#if ! ssh ubuntu@"$dataland_tunnel_uri" "echo Successfully connected!"; then
#  echo "Unable to connect to tunnel server. Trying to start server."
#  curl "$dataland_tunnel_startup_link"
#  sleep 60
#fi

#echo "Kill all locally running SSH tunnels"
#for pid in $(ps | grep /usr/bin/ssh | awk '{ print $1 }')
#do
#  echo "Killing PID: $pid"
#  kill "$pid"
#done

#echo "Open all three SSH tunnels from the Dataland-Tunnel-Server to your host system"
#ssh -R \*:"$dataland_edc_server_web_http_port":"$HOSTNAME":"$config_web_http_port" -N -f ubuntu@"$dataland_tunnel_uri"
#ssh -R \*:"$dataland_edc_server_web_http_ids_port":"$HOSTNAME":"$config_web_http_ids_port" -N -f ubuntu@"$dataland_tunnel_uri"
#ssh -R \*:"$dataland_edc_server_web_http_data_port":"$HOSTNAME":"$config_web_http_data_port" -N -f ubuntu@"$dataland_tunnel_uri"

#echo "Starting Dataland EDC server"
#.././gradlew :dataland-edc-server:run >test.log 2>test.err &
#edc_server_pid=$!

#sleep 20
#echo "Done waiting. Continue with test."

#echo "http://${dataland_edc_server_uri}:${dataland_edc_server_web_http_port}/api/dataland/health"

echo "Checking if EuroDaT is available."
TODO test if EuroDaT is there



echo "Checking health endpoint"
health_response=$(curl -X GET "http://localhost/api/dataland/health" -H "accept: application/json")
if [[ ! $health_response =~ "I am alive!" ]]; then
  echo "Response was unexpected: $health_response"
  exit 1
fi

test_data="Test Data from: "$(date "+%d.%m.%Y %H:%M:%S")

start_time=$(date +%s)

echo "Posting test data: $test_data."
response=$(curl -X POST "http://localhost/api/dataland/data" -H "accept: application/json" -H "Content-Type: application/json" -d "$test_data")
regex="\"([a-f0-9\-]+:[a-f0-9\-]+)\""
if [[ $response =~ $regex ]]; then
  dataId=${BASH_REMATCH[1]}
else
  echo "Unable to extract data ID from response: $response"
fi
echo "Received response from post request with data ID: $dataId"

echo "Retrieving test data."
get_response=$(curl -X GET "http://localhost/api/dataland/data/$dataId" -H "accept: application/json")
if [[ ! $get_response =~ $test_data ]]; then
  echo "Response was unexpected: $get_response"
  echo "Expected was substring: $test_data"
  exit 1
fi
echo "Retrieved data is: $get_response"

runtime=$(($(date +%s) - start_time))

echo "Test successfully run. Up- and download took $runtime seconds."
