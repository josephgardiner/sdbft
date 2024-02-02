package bftclientproxy;

import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFMessage;


public class Request {

	int controller;
	OFType type;
	long xid;
	OFMessage message;
	public Request(int controller, OFType type, long xid, OFMessage message) {
		super();
		this.controller = controller;
		this.type = type;
		this.xid = xid;
		this.message = message;
	}
	public int getController() {
		return controller;
	}
	public void setController(int controller) {
		this.controller = controller;
	}
	public OFType getType() {
		return type;
	}
	public void setType(OFType type) {
		this.type = type;
	}
	public long getXid() {
		return xid;
	}
	public void setXid(long xid) {
		this.xid = xid;
	}
	public OFMessage getMessage() {
		return message;
	}
	public void setMessage(OFMessage message) {
		this.message = message;
	}
	
	
}
