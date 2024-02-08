Floodlight can be compiled using 
ant build


Note that on compiling, any floolightdefault.properties in the target/bin directory are overwritten, so ensure that any custom prpoerties files are renamed. 

Floodlight can be run with (from folder above project folder). The -cf options is used to specify a properties file
java -jar  -server -XX:+UseCompressedOops  floodlight/target/floodlight.jar -cf floodlight/target/bin/floodlightdefault.properties 

If running multiple floodlight instances on the same machine, you will need to change the properties file within each to adjust overlapping networko port 
The specific elements that cause issues are:
net.floodlightcontroller.core.internal.OFSwitchManager.openFlowPort=6653
net.floodlightcontroller.restserver.RestApiServer.httpsPort=8081
net.floodlightcontroller.restserver.RestApiServer.httpPort=8080
org.sdnplatform.sync.internal.SyncManager.port=6642


You will also need to update the syncmanager nodes list (org.sdnplatform.sync.internal.SyncManager.nodes) to all controllers in use if performing failover testing. 

If using signatures, then change net.floodlightcontroller.core.internal.OFSwitchManager.use-signatures to True. 
You will need to make sure that the signature  config below matches that in you proxy properties file. 