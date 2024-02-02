This example setup generates a 10-switch, 2 network in mininet, launches 10 instances of the proxy and will send a ping from switch 1 to switch 10. The network layout is 
H1-S1-S2-S3-S4-S5-S6-S7-S8-S9-S10-H2

You can test over smaller networks by modifying the 10hop.py mininet config to remove switches, or by attaching host 2 to a different switch by modifiying the line
net.addLink( s10, h2 )

You will also need to run 4 instances of the controller. DUplicate the floodlight controller 4 times, and copy one of the provided properties files into each. 

The 10hop.sh bash script can be used to run the test automatically, including launchign controllers. You will need to update the path for the controllers. 