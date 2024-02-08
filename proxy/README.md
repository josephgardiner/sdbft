

The proxy is currently configured for use with mininet. To use with other controllers, you will need to modify the Control.java class within each version of the proxy. Specifically, within the runServer() method, change the line
if(second)
to 
if(!second)

This is required as mininet makes an initial connection to a controller to test for liveness before the switch makes a connection, which needs to be ignored by the proxy. 
This will be incorporated into the proxy config in later versions. 


To run the proxy with a properties file, then in the terminal do the following;
java -jar -XX:+UseCompressedOops  -XX:+UseNUMA  proxy.jar proxy.properties 

An example proxy.properties is supplied. The following options are required:
version: the type of proxy to run. This can be simple (simple tcp proxy), unsigned (unsigned sdbft proxy) or signed (signed sdbft proxy). 
localport: the port to run the proxy. This is also the port that should be configured as the controller on the openflow switch. 
controllers: the list of primary controllers
loadbackups: if performing failover testing, set to true
backupcontrollers: the list of backup controllers
batchacks: If true, acknowledgements will only be sent every 1/2 second
startxid: if running multiple proxies, this value should be different on each, with sufficient gap between proxy start values. 
ofversion: the supported openflow version. Currently tested only on 1.3, though should work with other versions up to 1.5. 
signaturetype, signaturekey, keysize, siglength: configuration for Java signatures. siglength must equal generated signature length from other options. 

For running tests with BFT-SMaRt, an example proxy config is included under bft/
A BFTClient.properties must be run for each switch, whilst a server.properties must be run for each controller. The contained config folder must also be updated per the instructions provided on the original project page
https://github.com/bft-smart/library


To compile the proxy, use the eclipse Export-> Runnable jar file function. Make sure you select the "Package required libraries into runnable jar" options. 
