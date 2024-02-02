This folder contains the mining topology used within the thesis to demonstrate the attacks in a non-ics setting. To use, load as a mininet topology and specify the malicious floodlight in this folder controller as the controller for all switches. 
This floodlight is for demonstrating the attacks only, and has not been configured for use with the SDBFT proxy. The malicious applications can be ported to the SDBFT supported Floodlight if required. 



To load a malicious application, you need to add it to the lists of modules at the top of the flodlight/target/bin/floodlightdefault.properties file
e.g. 
net.floodlightcontroller.malicious.AmplifiedDOS,\

Note that only a single malicious application has been tested at one time. If more than one are loaded then there may be unexpected effects. 
 
You can then set a single IP to be targeted by using the options at the bottom of the config file. There is one option per malicious application. 