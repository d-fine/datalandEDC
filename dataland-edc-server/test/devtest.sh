#!/bin/bash
set -e

trap stopEDCServer EXIT

function stopEDCServer() {
  kill "$edc_server_pid"
}

workdir=$(dirname "$0")
echo "Changing to working directory $workdir."
cd "$workdir"

envsubst < "./.env.devtest" > .env
source "./.env"

source ./test_utility.sh

is_eurodat_up

echo "Check connection to tunnel server."
if ! ssh ubuntu@"$dataland_tunnel_uri" "echo Successfully connected!"; then
  echo "Unable to connect to tunnel server. Trying to start server."
  curl "$dataland_tunnel_startup_link"
  sleep 60
fi

echo "Kill all locally running SSH tunnels"
for pid in $(ps | grep /usr/bin/ssh | awk '{ print $1 }')
do
  echo "Killing PID: $pid"
  kill "$pid"
done

echo "Open all three SSH tunnels from the Dataland-Tunnel-Server to your host system"
ssh -R \*:"$dataland_edc_server_web_http_port":$HOSTNAME:"$config_web_http_port" -N -f ubuntu@"$dataland_tunnel_uri"
ssh -R \*:"$dataland_edc_server_web_http_ids_port":$HOSTNAME:"$config_web_http_ids_port" -N -f ubuntu@"$dataland_tunnel_uri"
ssh -R \*:"$dataland_edc_server_web_http_data_port":$HOSTNAME:"$config_web_http_data_port" -N -f ubuntu@"$dataland_tunnel_uri"

start_edc_server

echo "Checking health endpoint of dataland edc server locally."
timeout 240 bash -c "while ! is_edc_server_up; do echo 'Dataland EDC server not yet there - retrying in 5s'; sleep 5; done; echo 'Dataland EDC server up!'"

execute_eurodat_test
