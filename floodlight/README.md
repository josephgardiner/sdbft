Floodlight can be compiled using 
ant build


Note that on compiling, any floolightdefault.properties in the target/bin directory are overwritten, so ensure that any custom prpoerties files are renamed. 

Floodlight can be run with (from folder above project folder). The -cf options is used to specify a properties file
java -jar  -server -XX:+UseCompressedOops  floodlight/target/floodlight.jar -cf floodlight/target/bin/floodlightdefault.properties 
