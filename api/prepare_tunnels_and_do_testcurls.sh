#!/bin/sh
set -ex

dataland_tunnel_public_ip='3.67.203.82'
dataland_tunnel_startup_link='https://try7eflmqd.execute-api.eu-central-1.amazonaws.com/default?instance=i-0f2429445a5fbdcc4'

config_web_http_port=9191
config_web_http_ids_ports_port=9292
config_web_http_data_port=9393

dataland_edc_server_url='http://dataland-tunnel.duckdns.org'
dataland_edc_server_web_http_port=9191
dataland_edc_server_web_http_ids_port=9292
dataland_edc_server_web_http_data_port=9393


    private val datalandConnectorURL = "http://dataland-tunnel.duckdns.org:9191"


#Check if the Dataland-Tunnel-Server is running
ssh ubuntu@${dataland_tunnel_public_ip}
 => if not running, startup the server via: $(dataland_tunnel_startup_link)

#Kill all running SSH tunnels (ssh processes)
for pid in $(ps | grep /usr/bin/ssh | awk '{ print $1 }')
do
  kill $pid
done

#Open all three SSH tunnels from the Dataland-Tunnel-Server to your host system
ssh -R \*:${dataland_edc_server_web_http_port}:${HOSTNAME}:${config_web_http_port} -N -f ubuntu@3.67.203.82
ssh -R \*:${dataland_edc_server_web_http_ids_port}:${HOSTNAME}:${config_web_http_ids_port} -N -f ubuntu@3.67.203.82
ssh -R \*:${dataland_edc_server_web_http_data_port}:${HOSTNAME}:${config_web_http_data_port} -N -f ubuntu@3.67.203.82

#Check if everything works as expected
curl -X 'GET'   'http://${dataland_edc_server_url}:${dataland_edc_server_web_http_port}/api/health'   -H 'accept: application/json'
=> sollte Im Alive zurückgeben

test_text='Just a test input'

curl -X 'POST'  'http://${dataland_edc_server_url}:${dataland_edc_server_web_http_port}/api/dataland/data'   -H 'accept: application/json'   -H 'Content-Type: application/json'   -d ${test_text}
=> sollte assetId:contractDefinitionId zurückgeben und das sollte auch gepseichert werden
dataId = assetId:contractDefinitionId

curl -X 'GET'   'http://${dataland_edc_server_url}:${dataland_edc_server_web_http_port}/api/dataland/data/${dataId}'   -H 'accept: application/json'
=> Ergebnis sollte == ${test_text} sein

#done
