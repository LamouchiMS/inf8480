#!/bin/sh

i=1

# Config file
configFile="config.txt"

# Operations file
operationsFile="operations-588"

# Server
malice="2"
capacity="2"
serverPort="5003"

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
    fi;

    i=$(( i + 1 ))
done < $configFile

echo
echo "Operations file\t\t"$operationsFile
echo
echo "Load balancer IP\t"$loadBalancerIP
echo "Load balancer Port\t"$loadBalancerPort
echo "Load balancer username\t"$loadBalancerUsername
echo "Load balancer password\t"$loadBalancerPassword
# echo "Load balancer Port\t"$loadBalancerPort
echo
echo "Name repository IP\t"$nameRepositoryIP
echo "Name repository Port\t"$nameRepositoryPort
echo

echo "[*]\tCompiling"
ant &
wait %1

echo "[*]\tKilling previous rmiregistry processes"
killall rmiregistry &
killall java &
wait %1 %2

# rmiregistry w/ ports
cd ./bin
echo "[*]\tExecuting rmiregistry for each module w/ ports"
rmiregistry $nameRepositoryPort &
rmiregistry $loadBalancerPort &
rmiregistry $serverPort &
sleep 3

cd ..
# Start name repository
echo "[*]\tStarting name repository"
bash nameRepository $nameRepositoryPort &
sleep 3

# Start a server
echo "[*]\tStarting a server"
bash server $malice $capacity $serverPort &
sleep 3

# Start load balancer
echo "[*]\tStarting load balancer"
bash loadBalancer $loadBalancerUsername $loadBalancerPassword $operationsFile $loadBalancerPort

wait %4 %5 %6