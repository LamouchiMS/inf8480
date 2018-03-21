#!/bin/sh

i=1

# Config file
configFile="config.txt"

# Operations file
operationsFile="operations-big"

# Servers
servers={}
serversLineStart=15

# Load balancer
loadBalancerLine=4
loadBalancerIP=""
loadBalancerPort=""
loadBalancerUsername=""
loadBalancerPassword=""

# Name repository
nameRepositoryLine=6
nameRepositoryIP=""
nameRepositoryPort=""

# Load information from config file
while IFS='' read -r line || [[ -n "$line" ]]; do
    if [ $i == $loadBalancerLine ]; then
        arrIN=(${line//:/ })
        loadBalancerIP=${arrIN[0]}
        loadBalancerPort=${arrIN[1]}
    elif [ $i == 10 ]; then
        arrIN=(${line//:/ })
        loadBalancerUsername=${arrIN[0]}
    elif [ $i == 12 ]; then
        arrIN=(${line//:/ })
        loadBalancerPassword=${arrIN[0]}
    elif [ $i == $nameRepositoryLine ]; then
        arrIN=(${line//:/ })
        nameRepositoryIP=${arrIN[0]}
        nameRepositoryPort=${arrIN[1]}
    elif [ $i -ge $serversLineStart ]; then
        idx=$((i - $serversLineStart))
        servers[$idx]=$line
    fi;

    i=$(( i + 1 ))
done < $configFile

echo "[*]\tCompiling"
ant > /dev/null &
wait %1

echo "[*]\tKilling previous rmiregistry processes"
killall rmiregistry &
killall java &
wait %1 %2

# Recreate servers list
echo "[*]\tRegenerating servers list"
rm "serversIpList.txt"
touch "serversIpList.txt"

# rmiregistry w/ ports
cd ./bin
rmiregistry $nameRepositoryPort &
rmiregistry $loadBalancerPort &
for index in ${!servers[@]}; do
    serverPort=$(( index + 5003 ))
    rmiregistry $serverPort &
done
sleep 3

cd ..
# Start name repository
echo "[*]\tStarting name repository"
bash nameRepository $nameRepositoryPort &
sleep 3

# Start servers
echo "[*]\tStarting servers"
for index in ${!servers[@]}; do
    arrIN=(${servers[index]//:/ })
    malice=${arrIN[1]}
    capacity=${arrIN[2]}
    serverPort=$(( index + 5003 ))
    bash server $malice $capacity $serverPort &
done
sleep 3

# Start load balancer
echo "[*]\tStarting load balancer"
bash loadBalancer $loadBalancerUsername $loadBalancerPassword $operationsFile $loadBalancerPort

wait