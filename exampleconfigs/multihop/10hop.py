from mininet.net import Mininet
from mininet.node import Controller, OVSSwitch, OVSKernelSwitch, RemoteController
from mininet.link import TCLink
from mininet.cli import CLI
from mininet.log import setLogLevel
import sys
import time

def multiControllerNet(num):
    "Create a network from semi-scratch with multiple controllers."

    net = Mininet( controller=RemoteController, switch=OVSKernelSwitch, build=False, autoSetMacs = True, autoStaticArp = True)

    print "*** Creating (reference) controllers"
    c1 = net.addController('c1', controller=RemoteController,
ip="127.0.0.1", port=55413)
    c2 = net.addController('c2', controller=RemoteController,
ip="127.0.0.1", port=55414)
    c3 = net.addController('c3', controller=RemoteController,
ip="127.0.0.1", port=55415)
    c4 = net.addController('c4', controller=RemoteController,
ip="127.0.0.1", port=55416)
    c5 = net.addController('c5', controller=RemoteController,
ip="127.0.0.1", port=55417)
    c6 = net.addController('c6', controller=RemoteController,
ip="127.0.0.1", port=55418)
    c7 = net.addController('c7', controller=RemoteController,
ip="127.0.0.1", port=55419)
    c8 = net.addController('c8', controller=RemoteController,
ip="127.0.0.1", port=55420)
    c9 = net.addController('c9', controller=RemoteController,
ip="127.0.0.1", port=55421)
    c10 = net.addController('c10', controller=RemoteController,
ip="127.0.0.1", port=55422)


    print "*** Creating switches"
    s1 = net.addSwitch( 's1' , protocols=["OpenFlow13"])
    s2 = net.addSwitch( 's2' , protocols=["OpenFlow13"])
    s3 = net.addSwitch( 's3' , protocols=["OpenFlow13"])
    s4 = net.addSwitch( 's4' , protocols=["OpenFlow13"])
    s5 = net.addSwitch( 's5' , protocols=["OpenFlow13"])
    s6 = net.addSwitch( 's6' , protocols=["OpenFlow13"])
    s7 = net.addSwitch( 's7' , protocols=["OpenFlow13"])
    s8 = net.addSwitch( 's8' , protocols=["OpenFlow13"])
    s9 = net.addSwitch( 's9' , protocols=["OpenFlow13"])
    s10 = net.addSwitch( 's10' , protocols=["OpenFlow13"])



    s1.cmd("sysctl -w net.ipv6.conf.all.disable_ipv6=1")
    s1.cmd("sysctl -w net.ipv6.conf.default.disable_ipv6=1")
    s1.cmd("sysctl -w net.ipv6.conf.lo.disable_ipv6=1")
    s2.cmd("sysctl -w net.ipv6.conf.all.disable_ipv6=1")
    s2.cmd("sysctl -w net.ipv6.conf.default.disable_ipv6=1")
    s2.cmd("sysctl -w net.ipv6.conf.lo.disable_ipv6=1")
    s3.cmd("sysctl -w net.ipv6.conf.all.disable_ipv6=1")
    s3.cmd("sysctl -w net.ipv6.conf.default.disable_ipv6=1")
    s3.cmd("sysctl -w net.ipv6.conf.lo.disable_ipv6=1")
    s4.cmd("sysctl -w net.ipv6.conf.all.disable_ipv6=1")
    s4.cmd("sysctl -w net.ipv6.conf.default.disable_ipv6=1")
    s4.cmd("sysctl -w net.ipv6.conf.lo.disable_ipv6=1")
    s5.cmd("sysctl -w net.ipv6.conf.all.disable_ipv6=1")
    s5.cmd("sysctl -w net.ipv6.conf.default.disable_ipv6=1")
    s5.cmd("sysctl -w net.ipv6.conf.lo.disable_ipv6=1")
    s6.cmd("sysctl -w net.ipv6.conf.all.disable_ipv6=1")
    s6.cmd("sysctl -w net.ipv6.conf.default.disable_ipv6=1")
    s6.cmd("sysctl -w net.ipv6.conf.lo.disable_ipv6=1")
    s7.cmd("sysctl -w net.ipv6.conf.all.disable_ipv6=1")
    s7.cmd("sysctl -w net.ipv6.conf.default.disable_ipv6=1")
    s7.cmd("sysctl -w net.ipv6.conf.lo.disable_ipv6=1")
    s8.cmd("sysctl -w net.ipv6.conf.all.disable_ipv6=1")
    s8.cmd("sysctl -w net.ipv6.conf.default.disable_ipv6=1")
    s8.cmd("sysctl -w net.ipv6.conf.lo.disable_ipv6=1")
    s9.cmd("sysctl -w net.ipv6.conf.all.disable_ipv6=1")
    s9.cmd("sysctl -w net.ipv6.conf.default.disable_ipv6=1")
    s9.cmd("sysctl -w net.ipv6.conf.lo.disable_ipv6=1")
    s10.cmd("sysctl -w net.ipv6.conf.all.disable_ipv6=1")
    s10.cmd("sysctl -w net.ipv6.conf.default.disable_ipv6=1")
    s10.cmd("sysctl -w net.ipv6.conf.lo.disable_ipv6=1")

    print "*** Creating hosts"

    h1 = net.addHost( 'h1' )
    h2 = net.addHost( 'h2' )

    net.addLink( s1, h1 )
    net.addLink( s10, h2 )

    net.addLink( s1, s2 )
    net.addLink( s2, s3 )
    net.addLink( s3, s4 )
    net.addLink( s4, s5 )
    net.addLink( s5, s6 )
    net.addLink( s6, s7 )
    net.addLink( s7, s8 )
    net.addLink( s8, s9 )
    net.addLink( s9, s10 )

    print "*** Starting network"
    net.build()


    c1.start()
    c2.start()
    c3.start()
    c4.start()
    c5.start()
    c6.start()
    c7.start()
    c8.start()
    c9.start()
    c10.start()
    s1.start( [ c1 ] )
    s2.start( [ c2 ] )
    s3.start( [ c3 ] )
    s4.start( [ c4 ] )
    s5.start( [ c5 ] )
    s6.start( [ c6 ] )
    s7.start( [ c7 ] )
    s8.start( [ c8 ] )
    s9.start( [ c9 ] )
    s10.start( [c10] )
    print "***Setup Done"
    time.sleep(20)
    print "*** Testing network"
  #  net.pingAll()
    f = open("results/10/test" + num + ".txt", "a")


    res1=h1.cmd('ping -c 4 10.0.0.2')
    print(res1)
    f.write(res1)
    time.sleep(6)
    res2= h1.cmd('ping -c 4 10.0.0.2')
    print(res2)
    f.write(res2)
    f.close()
    print "*** Running CLI"
 #   CLI( net )

    print "*** Stopping network"
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )  # for CLI
    multiControllerNet(sys.argv[1])
