package bftclientproxy;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;

import bftsmart.tom.core.messages.TOMMessage;
import io.netty.buffer.ByteBuf;

public class Packet {

	Controller controller;
	OFType type;
	OFMessage message;
	byte[] signature;
	TOMMessage tomMessage;

	public Packet(Controller controller, OFMessage message) {
		super();
		this.controller = controller;
		this.message = message;
		this.type = message.getType();
	}

	public Packet(Controller controller, OFType type, OFMessage message, byte[] signature) {
		super();
		this.controller = controller;
		this.type = type;
		this.message = message;
		this.signature = signature;
	}

	public Packet( OFType type, OFMessage message) {
		super();
		this.type = type;
		this.message = message;
	}

	public Packet(OFType type, OFMessage message, TOMMessage tomMessage) {
		super(); 
		this.type = type;
		this.message = message;
		this.tomMessage = tomMessage;
	}
 
	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}

//	public ByteBuf getData() {
//		return data;
//	}
//
//	public void setData(ByteBuf data) {
//		this.data = data;
//	}

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

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	public TOMMessage getTomMessage() {
		return tomMessage;
	}

	public void setTomMessage(TOMMessage tomMessage) {
		this.tomMessage = tomMessage;
	}
	
	
	

}
