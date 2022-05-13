#!/bin/bash
set -eu

workdir=$(dirname "$0")
echo "Changing to working directory $workdir."
cd "$workdir"

echo "Present working directory:"
pwd
echo "Showing all contents in this folder"
ls

echo "A"
envsubst < "./.env.e2etest" > .env
echo "B"
sleep 30
source ./.env
echo "C"

source ./test_utility.sh

echo "D"
is_eurodat_up

echo "Enable runner to connect to ssh tunnel server."
mkdir -p ~/.ssh/
echo "$dataland_tunnel_uri ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIBzDFbotMpfoTdyvpA/W3sFQX4e+GxTDp3BQHaHxV19N" >  ~/.ssh/known_hosts
echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa

echo "Check connection to tunnel server."
if ssh ubuntu@"$dataland_tunnel_uri" "sudo shutdown now"; then
   echo "Tunnel server was running and has been stopped."
fi

start_edc_server

echo "Checking health endpoint of dataland edc server locally."
timeout 240 bash -c "while ! is_edc_server_up; do echo 'Dataland EDC server not yet there - retrying in 5s'; sleep 5; done; echo 'Dataland EDC server up!'"

echo "Starting tunnel server."
curl "$dataland_tunnel_startup_link"
sleep 10

echo "Checking availability of tunnel server."
timeout 240 bash -c "while ! is_tunnel_server_up; do echo 'Tunnel server not yet there - retrying in 5s'; sleep 5; done; echo 'Tunnel server up!'"

echo "Open all three SSH tunnels between tunnel server and the host system."
ssh -R \*:"$dataland_edc_server_web_http_port":localhost:"$config_web_http_port" -N -f ubuntu@"$dataland_tunnel_uri"
ssh -R \*:"$dataland_edc_server_web_http_ids_port":localhost:"$config_web_http_ids_port" -N -f ubuntu@"$dataland_tunnel_uri"
ssh -R \*:"$dataland_edc_server_web_http_data_port":localhost:"$config_web_http_data_port" -N -f ubuntu@"$dataland_tunnel_uri"

execute_eurodat_test

echo "Stopping Dataland EDC Server."
kill "$edc_server_pid"
