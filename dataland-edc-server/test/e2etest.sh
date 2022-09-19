#!/bin/bash
set -eu

workdir=$(dirname "$0")
echo "Changing to working directory $workdir."
cd "$workdir"

envsubst < ./.env.template > .env
source ./.env

source ./test_utility.sh

is_eurodat_up_and_healthy

echo "Enable runner to connect to ssh tunnel server."
mkdir -p ~/.ssh/
echo "$dataland_tunnel_uri ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIBzDFbotMpfoTdyvpA/W3sFQX4e+GxTDp3BQHaHxV19N" >  ~/.ssh/known_hosts
echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa

restart_tunnel_server

echo "Open SSH tunnels between tunnel server and the host system."
ssh -R \*:"$dataland_edc_server_web_http_port":localhost:"$config_web_http_port" -S /tmp/.ssh_tunnel_control_http_port -M -fN ubuntu@"$dataland_tunnel_uri"
ssh -R \*:"$dataland_edc_server_web_http_ids_port":localhost:"$config_web_http_ids_port" S /tmp/.ssh_tunnel_control_ids_port -M  -fN ubuntu@"$dataland_tunnel_uri"

start_edc_server

execute_eurodat_test

echo "Stopping Dataland EDC Server."
kill "$edc_server_pid"
