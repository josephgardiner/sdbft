

The proxy is currently configured for use with mininet. To use with other controllers, you will need to modify the Control.java class within each version of the proxy. Specifically, within the runServer() method, change the line
if(second)
to 
if(!second)

This is required as mininet makes an initial connection to a controller to test for liveness before the switch makes a connection, which needs to be ignored by the proxy. 
This will be incorporated into the proxy config in later versions. 


To run the proxy with a properties file, then in the terminal do the following;
java -jar -XX:+UseCompressedOops  -XX:+UseNUMA  proxy.jar proxy.properties 


To compile the proxy, use the eclipse Export-> Runnable jar file function. Make sure you select the "Package required libraries into runnable jar" options. 
