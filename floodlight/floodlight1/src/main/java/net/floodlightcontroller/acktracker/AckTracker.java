package net.floodlightcontroller.acktracker;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFEchoReply;
import org.projectfloodlight.openflow.protocol.OFErrorMsg;
import net.floodlightcontroller.core.IListener.Command;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.HARole;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;
import net.floodlightcontroller.packet.Ethernet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AckTracker implements IOFMessageListener, IFloodlightModule {

    protected IFloodlightProviderService floodlightProvider;
    protected static Logger logger;
    protected static final Logger log = LoggerFactory.getLogger(AckTracker.class);

    @Override
    public String getName() {
      return AckTracker.class.getSimpleName();
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        // TODO Auto-generated method stub
    	return (type.equals(OFType.ERROR) && 
                (name.equals("channelhandler") || 
                 name.equals("devicemanager") ||
                 name.equals("virtualizer") ||
                 name.equals("forwarding")));
      //  return false;
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        // TODO Auto-generated method stub
    	log.info("Received ACK");
    	OFErrorMsg error = (OFErrorMsg) msg;
    	String message = new String(error.getData().getData());
		log.info(message);
	//	logger.info(message.substring(2, 5));
			if(message.contains("FAILURE")){
			sw.setAttribute("master", Boolean.TRUE);	    	
			
			}
		if(message.substring(2, 5).equals("ACK")){
			return Command.STOP;
		}
    	
    	return Command.CONTINUE;
    	
// 	OFEchoReply ack = (OFEchoReply) msg;
//    	
//    	String message = new String(ack.getData());
//		logger.info(message);
//		logger.info(message.substring(0, 3));
//		if(message.substring(0, 3).equals("ACK")){
//			return Command.STOP;
//		}
//    	
//    	return Command.CONTINUE;
        }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<>();
        l.add(IFloodlightProviderService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
      //  macAddresses = new ConcurrentSkipListSet<>();
        logger = LoggerFactory.getLogger(AckTracker.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider.addOFMessageListener(OFType.ERROR, this);
    }
    

}
