#!/bin/bash
# This script executes a DevTest against EuroDaT. It will upload a dataset, and download it again.
# It requires the tunnel server and will reboot it during the test. Hence, only one developer at
# the time should be using this test and it cannot be executed in parallel to the CI pipeline!
# Prerequisites:
# - Set all env variables according the DatalandInternal Wiki
# - Build the current version of the DataSpaceConnector (see Readme)

set -e

trap stopEDCServer EXIT

function stopEDCServer() {
  if [[ -n $edc_server_pid ]]; then
    echo "Shutting down EDC server running under: $edc_server_pid"
    kill "$edc_server_pid"
  fi
}

workdir=$(dirname "$0")
echo "Changing to working directory $workdir."
cd "$workdir"

envsubst < ./.env.template > .env
source ./.env

source ./test_utility.sh

is_eurodat_up_and_healthy

if is_edc_server_up_and_healthy; then
  echo "Local EDC Server already responding before test started. Make sure no conflicting process is running."
  exit 1
fi

restart_tunnel_server

acquire_ssh_tunnel "$HOSTNAME"

start_edc_server

execute_eurodat_test
