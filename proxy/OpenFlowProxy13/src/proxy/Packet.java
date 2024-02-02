package proxy;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;

public class Packet {

	Controller controller;
	byte[] data;
	OFType type;
	OFMessage message;
	public Packet(Controller controller, byte[] data, OFMessage message) {
		super();
		this.controller = controller;
		this.data = data;
		this.message = message;
		this.type = message.getType();
	}


	public Packet(byte[] data, OFType type, OFMessage message) {
		super();
		this.data = data;
		this.type = type;
		this.message = message;
	}


	public Controller getController() {
		return controller;
	}
	public void setController(Controller controller) {
		this.controller = controller;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public OFType getType() {
		return type;
	}
	public void setType(OFType type) {
		this.type = type;
	}
	public OFMessage getMessage() {
		return message;
	}
	public void setMessage(OFMessage message) {
		this.message = message;
	}





}
