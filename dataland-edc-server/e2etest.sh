#!/bin/bash
set -eu

export dataland_tunnel_uri=$EDC_SERVER_URI
dataland_tunnel_startup_link=$TUNNEL_STARTUP_LINK
dataland_edc_server_uri=$EDC_SERVER_URI

config_web_http_port=9191
config_web_http_ids_port=9292
config_web_http_data_port=9393

export dataland_edc_server_web_http_port=9191
dataland_edc_server_web_http_ids_port=9292
dataland_edc_server_web_http_data_port=9393

workdir=$(dirname "$0")
echo "Changing to working directory $workdir"
cd "$workdir"

echo "Checking if EuroDaT is available."
if ! curl -f -X 'GET' "${TRUSTEE_URI}/ids/description" -H 'accept: application/json' >/dev/null 2>&1; then
 echo "EuroDaT is not available."
 exit 1
fi
echo "EuroDat is available."

echo "Enable runner to connect to ssh tunnel server."
mkdir -p ~/.ssh/
echo "$dataland_tunnel_uri ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIBzDFbotMpfoTdyvpA/W3sFQX4e+GxTDp3BQHaHxV19N" >  ~/.ssh/known_hosts
echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa

echo "Check connection to tunnel server."
if ssh ubuntu@"$dataland_tunnel_uri" "sudo shutdown now"; then
   echo "Tunnel server was running and has been stopped."
fi
echo "Starting tunnel server."
curl "$dataland_tunnel_startup_link"

echo "Starting Dataland EDC server"
./../gradlew :dataland-edc-server:run >test.log 2>test.err &

is_infrastructure_up () {
  health_response=$(curl -f -X GET "http://localhost:${dataland_edc_server_web_http_port}/api/dataland/health" -H "accept: application/json")
  if [[ ! $health_response =~ "I am alive!" ]]; then
    echo "Response was unexpected: $health_response"
    return 1
  fi
  if ! ssh ubuntu@"$dataland_tunnel_uri" "echo Connected to tunnel server"; then
    return 1
  fi
}

echo "Checking health endpoint of dataland edc server locally and tunnel server status"
export -f is_infrastructure_up
timeout 240 bash -c "while ! is_infrastructure_up; do echo 'dataland edc server or tunnel server not yet there - retrying in 5s'; sleep 5; done; echo 'dataland edc and tunnel server up!'"

echo "Open all three SSH tunnels from the Dataland-Tunnel-Server to your host system"
ssh -R \*:"$dataland_edc_server_web_http_port":localhost:"$config_web_http_port" -N -f ubuntu@"$dataland_tunnel_uri"
ssh -R \*:"$dataland_edc_server_web_http_ids_port":localhost:"$config_web_http_ids_port" -N -f ubuntu@"$dataland_tunnel_uri"
ssh -R \*:"$dataland_edc_server_web_http_data_port":localhost:"$config_web_http_data_port" -N -f ubuntu@"$dataland_tunnel_uri"

echo "Checking health endpoint via internet"
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
