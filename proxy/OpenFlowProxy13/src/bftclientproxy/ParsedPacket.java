package bftclientproxy;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ParsedPacket {

	ArrayList<Packet> packets;
	ByteBuf data;

	public ParsedPacket(ArrayList<Packet> packets, ByteBuf data) {
		super();
		this.packets = packets;
		this.data = data;
	}

	public ParsedPacket(int bufferSize) {
		super();
		this.packets = new ArrayList<Packet>();
		data = Unpooled.buffer(4096);
	}

	public ArrayList<Packet> getPackets() {
		return packets;
	}

	public void setPackets(ArrayList<Packet> packets) {
		this.packets = packets;
	}

	public ByteBuf getData() {
		return data;
	}

	public void setData(ByteBuf data) {
		this.data = data;
	}

	public void writeBytes(byte[] arg0) {
		data.writeBytes(arg0);
	}

	public void add(Packet e) {
		packets.add(e);
	}



}
