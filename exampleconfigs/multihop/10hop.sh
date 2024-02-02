#!/bin/bash
mn -c
java -jar  -server -XX:+UseCompressedOops  /home/joe/singlehopcontrollers/floodlight1/target/floodlight.jar -cf /home/joe/singlehopcontrollers/floodlight1/target/bin/floodlightdefault.properties &
java -jar  -server -XX:+UseCompressedOops  /home/joe/singlehopcontrollers/floodlight2/target/floodlight.jar -cf /home/joe/singlehopcontrollers/floodlight2/target/bin/floodlightdefault.properties &
java -jar  -server -XX:+UseCompressedOops  /home/joe/singlehopcontrollers/floodlight3/target/floodlight.jar -cf /home/joe/singlehopcontrollers/floodlight3/target/bin/floodlightdefault.properties &
java -jar  -server -XX:+UseCompressedOops  /home/joe/singlehopcontrollers/floodlight4/target/floodlight.jar -cf /home/joe/singlehopcontrollers/floodlight4/target/bin/floodlightdefault.properties &
sleep 5
java -jar -XX:+UseCompressedOops  -XX:+UseNUMA  proxy.jar proxy.properties &
java -jar -XX:+UseCompressedOops  -XX:+UseNUMA  proxy.jar proxy2.properties &
java -jar -XX:+UseCompressedOops  -XX:+UseNUMA  proxy.jar proxy3.properties &
java -jar -XX:+UseCompressedOops  -XX:+UseNUMA  proxy.jar proxy4.properties &
java -jar -XX:+UseCompressedOops  -XX:+UseNUMA  proxy.jar proxy5.properties &
java -jar -XX:+UseCompressedOops  -XX:+UseNUMA  proxy.jar proxy6.properties &
java -jar -XX:+UseCompressedOops  -XX:+UseNUMA  proxy.jar proxy7.properties &
java -jar -XX:+UseCompressedOops  -XX:+UseNUMA  proxy.jar proxy8.properties &
java -jar -XX:+UseCompressedOops  -XX:+UseNUMA  proxy.jar proxy9.properties &
java -jar -XX:+UseCompressedOops  -XX:+UseNUMA  proxy.jar proxy10.properties &

sleep 5
python 10hop.py $1
pkill -9 java
sleep 2
