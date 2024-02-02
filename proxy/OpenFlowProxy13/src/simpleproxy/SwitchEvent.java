package simpleproxy;


import java.util.ArrayList;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;

public class SwitchEvent {

	public int xid;

	public OFPacketIn inPacket;

	public ArrayList<Packet> packetOuts;

	public ArrayList<Packet> flowMods;

	public SwitchEvent(int xid, OFPacketIn inPacket) {
		super();
		this.xid = xid;
		this.inPacket = inPacket;
		packetOuts = new ArrayList<Packet>();
		flowMods = new ArrayList<Packet>();
	}

	public int getXid() {
		return xid;
	}

	public void setXid(int xid) {
		this.xid = xid;
	}

	public OFPacketIn getInPacket() {
		return inPacket;
	}

	public void setInPacket(OFPacketIn inPacket) {
		this.inPacket = inPacket;
	}

	public ArrayList<Packet> getPacketOuts() {
		return packetOuts;
	}

	public void setPacketOuts(ArrayList<Packet> packetOuts) {
		this.packetOuts = packetOuts;
	}

	public synchronized ArrayList<Packet> getFlowMods() {
		return flowMods;
	}

	public synchronized void setFlowMods(ArrayList<Packet> flowMods) {
		this.flowMods = flowMods;
	}

	public boolean addPacketOut(Packet arg0) {
		return packetOuts.add(arg0);
	}

	public boolean addFlowMod(Packet arg0) {
		return flowMods.add(arg0);
	}

}
