package net.floodlightcontroller.comms;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.DatapathId;
import org.sdnplatform.sync.IStoreClient;
import org.sdnplatform.sync.IStoreListener;
import org.sdnplatform.sync.ISyncService;
import org.sdnplatform.sync.ISyncService.Scope;
import org.sdnplatform.sync.error.SyncException;
import org.sdnplatform.sync.internal.SyncManager;
import org.sdnplatform.sync.internal.rpc.IRPCListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.FloodlightProvider;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.mactracker.MACTracker;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.simpleft.FT;

public class CommsService
		implements IOFMessageListener, IFloodlightModule, IStoreListener<String>, IOFSwitchListener, IRPCListener {

	private ISyncService syncService;
	private IStoreClient<String, ArrayList> storeComms;
	private String controllerID;
	protected IFloodlightProviderService floodlightProvider;
	protected static Logger logger = LoggerFactory.getLogger(CommsService.class);
	int messageCount;
    HashMap<Short, Integer> messageCounts;
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return CommsService.class.getSimpleName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void disconnectedNode(Short nodeId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectedNode(Short nodeId) {
		// TODO Auto-generated method stub
		logger.info("Connected: " + nodeId);

	}

	@Override
	public void switchAdded(DatapathId switchId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchRemoved(DatapathId switchId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchActivated(DatapathId switchId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchPortChanged(DatapathId switchId, OFPortDesc port, PortChangeType type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchChanged(DatapathId switchId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchDeactivated(DatapathId switchId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keysModified(Iterator<String> keys, org.sdnplatform.sync.IStoreListener.UpdateType type) {

		while (keys.hasNext()) {
			String k = keys.next();
			try {
				/*
				 * logger.debug("keysModified: Key:{}, Value:{}, Type: {}", new
				 * Object[] { k, storeClient.get(k).getValue().toString(),
				 * type.name()} );
				 */
				if (type.name().equals("REMOTE")) {
					ArrayList list  = storeComms.get(k).getValue();
					String message = (String) list.get(list.size()-1);
										logger.info("REMOTE: NodeId:{}, Messages:" + list.size() + ", message: {}", k, message);
				      JSONParser parser = new JSONParser();
					  JSONObject obj = (JSONObject) parser.parse(message);
					
					  long currMessage =   (long) obj.get("MessageID");
					  JSONObject obj2 = (JSONObject) parser.parse((String) storeComms.get(k).getValue().get(list.size()-1));
					  long newMessage = (long) obj2.get("MessageID");
					if(!(currMessage==newMessage)){
					this.storeComms.put(k, list);
					}
				}
			} catch (SyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

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
		// TODO Auto-generated method stub
	    Collection<Class<? extends IFloodlightService>> l =
	            new ArrayList<Class<? extends IFloodlightService>>();
	        l.add(IFloodlightProviderService.class);
	        return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		// TODO Auto-generated method stub
		this.syncService = context.getServiceImpl(ISyncService.class);
		Map<String, String> configParams = context.getConfigParams(SyncManager.class);
		controllerID = configParams.get("thisNodeId");
	    floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		syncService.addRPCListener(this);

		try {
			this.syncService.registerStore("Comms", Scope.GLOBAL);
			this.storeComms = this.syncService.getStoreClient("Comms", String.class, ArrayList.class);
			this.storeComms.addStoreListener(this);
		} catch (SyncException e) {
			throw new FloodlightModuleException("Error while setting up sync service", e);
		}

		try {
			ArrayList newList = new ArrayList();
			 JSONObject obj = new JSONObject();

		      obj.put("Node:", this.controllerID);
		      obj.put("Message", "Hi. I am alive!");
		      obj.put("MessageID", 1);
		//	newList.add("Hi I'm controller " + controllerID);
			newList.add(obj.toString());

			this.storeComms.put(controllerID, newList);
		} catch (SyncException e) {
			e.printStackTrace();
		}
		
	    floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg,
			FloodlightContext cntx) {
		// TODO Auto-generated method stub
		messageCount++;
		  Ethernet eth =
	                IFloodlightProviderService.bcStore.get(cntx,
	                                            IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
	      JSONObject obj = new JSONObject();
	      obj.put("Node:", this.controllerID);
	      obj.put("PacketNumber", messageCount);
	      obj.put("From",  eth.getSourceMACAddress().toString());
	      obj.put("To", eth.getDestinationMACAddress().toString());
		  	String message =  "Packet number: " + messageCount + "From: "+ eth.getSourceMACAddress().toString() + " To: " + eth.getDestinationMACAddress().toString();
		  	try {
		  		ArrayList prev = storeComms.getValue(controllerID);
			      obj.put("MessageID", prev.size()+1);

		  		prev.add(obj.toString());
		  		
				this.storeComms.put(controllerID, prev);
			
			} catch (SyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        return Command.CONTINUE;
	}

}
