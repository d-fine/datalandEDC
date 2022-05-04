#!/bin/bash
set -eu

start_time=$(date +%s)

dataland_tunnel_public_ip=
dataland_tunnel_startup_link=

config_web_http_port=9191
config_web_http_ids_port=9292
config_web_http_data_port=9393

dataland_edc_server_url=
dataland_edc_server_web_http_port=9191
dataland_edc_server_web_http_ids_port=9292
dataland_edc_server_web_http_data_port=9393

echo "Check connection to tunnel server."
if ! ssh ubuntu@"$dataland_tunnel_public_ip" "echo Successfully connected!"; then
  echo "Unable to connect to tunnel server. Trying to start server."
  curl "$dataland_tunnel_startup_link"
  sleep 30
fi

echo "Kill all locally running SSH tunnels"
for pid in $(ps | grep /usr/bin/ssh | awk '{ print $1 }')
do
  echo "Killing PID: $pid"
  kill "$pid"
done

echo "Open all three SSH tunnels from the Dataland-Tunnel-Server to your host system"
ssh -R \*:"$dataland_edc_server_web_http_port":"$HOSTNAME":"$config_web_http_port" -N -f ubuntu@"$dataland_tunnel_public_ip"
ssh -R \*:"$dataland_edc_server_web_http_ids_port":"$HOSTNAME":"$config_web_http_ids_port" -N -f ubuntu@"$dataland_tunnel_public_ip"
ssh -R \*:"$dataland_edc_server_web_http_data_port":"$HOSTNAME":"$config_web_http_data_port" -N -f ubuntu@"$dataland_tunnel_public_ip"

echo "Checking health endpoint"
health_response=$(curl -X GET "${dataland_edc_server_url}:${dataland_edc_server_web_http_port}/api/health" -H "accept: application/json")
if [[ ! $health_response =~ "I am alive!" ]]; then
  echo "Response was unexpected: $health_response"
  exit 1
fi

test_data="Test Data from: "$(date "+%d.%m.%Y %H:%M:%S")

echo "Posting test data: $test_data."
dataId=$(curl -X POST "${dataland_edc_server_url}:${dataland_edc_server_web_http_port}/api/dataland/data" -H "accept: application/json" -H "Content-Type: application/json" -d "$test_data")
echo "Received response from post request: $dataId"

echo "Retrieving test data."
get_response=$(curl -X GET "${dataland_edc_server_url}:${dataland_edc_server_web_http_port}/api/dataland/data/$dataId" -H "accept: application/json")
if [[ ! $get_response =~ $test_data ]]; then
  echo "Response was unexpected: $get_response"
  echo "Expected was substring: $test_data"
  exit 1
fi
echo "Retrieved data is: $get_response"

runtime=$(($(date +%s) - start_time))

echo "Test successfully run in $runtime seconds."
