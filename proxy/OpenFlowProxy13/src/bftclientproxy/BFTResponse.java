package bftclientproxy;

import java.util.ArrayList;

import bftsmart.tom.core.messages.TOMMessage;

public class BFTResponse {

	ArrayList<Packet> packets;
	TOMMessage response;
	public BFTResponse(ArrayList<Packet> packets, TOMMessage response) {
		super();
		this.packets = packets;
		this.response = response;
	}
	public ArrayList<Packet> getPackets() {
		return packets;
	}
	public void setPackets(ArrayList<Packet> packets) {
		this.packets = packets;
	}
	public TOMMessage getResponse() {
		return response;
	}
	public void setResponse(TOMMessage response) {
		this.response = response;
	}
	
	
}
 