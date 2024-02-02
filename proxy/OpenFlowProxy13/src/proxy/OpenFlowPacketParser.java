package proxy;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFEchoReply;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class OpenFlowPacketParser {
	Control control;

	public OpenFlowPacketParser(Control control) {
		this.control = control;
	}

	public Packet parseMessages(ByteBuf data, int limit) {
		List<OFMessage> results = new ArrayList<OFMessage>();
		byte[] bytes = new byte[data.readableBytes()];
		int readerIndex = data.readerIndex();
		data.getBytes(readerIndex, bytes);
		OFMessage m = null;
		OFFactory factory;
		try {
			m = OFFactories.getGenericReader().readFrom(data);
		} catch (OFParseError e) {
			// TODO Auto-generated catch block
			System.out.println("Parse error");
			e.printStackTrace();
		} catch (IllegalArgumentException ex) {
			System.out.println("Illegal argument!");
		}

		// System.out.println(m.getVersion());
//		factory = OFFactories.getFactory(m.getVersion());

	//	System.out.println(m.getType());
		// OFFactory
		Packet toReturn = null;

//		switch (m.getType()) {
//		case PACKET_IN:
//			System.out.println("Packet in!!");
//
//			OFPacketIn packetIn = (OFPacketIn) m;
//


//			System.out.println(packetIn);
//
//			Match.Builder builder = factory.buildMatch();
//			Match match = packetIn.getMatch();
//			// System.out.println(
//			// org.projectfloodlight.openflow.types.U32.t(packetIn.getXid()));
//			// System.out.println(match);
//			for (MatchField field : match.getMatchFields()) {
//				System.out.println(field.getName());
//
//			}
//
//			toReturn = new Packet(bytes, m.getType(), m);
//			// match.loadFromPacket(packetIn.getData(), packetIn.getInPort());
//			return toReturn;
//		// break;
//		case HELLO:
//			// System.err.println("GOT HELLO from " + sw);
//
//			control.setHelloMessage(bytes);
//			break;
//		case ECHO_REQUEST:
//
//			break;
//		case FLOW_MOD:
//
//			OFFlowMod packet = (OFFlowMod) m;
//			Match modMatch = packet.getMatch();
//
//			for (OFAction a : packet.getActions()) {
//				System.out.println(a);
//
//			}
//
//			for (MatchField a : packet.getMatch().getMatchFields()) {
//				System.out.println(a.getName());
//				System.out.println(packet.getMatch());
//				System.out.println(modMatch.get(a));
//			}
//			return packet;
//		// break;
//		default:
//			// System.err.println("Unhandled OF message: "
//			// + m.getType());
//		}

		toReturn = new Packet(bytes, m.getType(), m);
		return toReturn;
	}

	public ParsedPacket parseMessagesWithSignatures(ByteBuf data, int limit) {
		List<OFMessage> results = new ArrayList<OFMessage>();
		byte[] bytes = new byte[data.readableBytes()];
		int readerIndex = data.readerIndex();
		data.getBytes(readerIndex, bytes);
		ParsedPacket toReturn = new ParsedPacket(data.capacity());
		OFMessage m = null;
		OFFactory factory;
		try {
			ByteBuf sig = Unpooled.buffer(5);

			data.readBytes(sig, 5);
			System.out.println("FOUND SIGNATURE!!!!!!!!!!!!!!!!:");
			System.out.println(new String(sig.array()));
			m = OFFactories.getGenericReader().readFrom(data);

			m.writeTo(toReturn.data);
		} catch (OFParseError e) {
			// TODO Auto-generated catch block
			System.out.println("Parse error");
			e.printStackTrace();
		} catch (IllegalArgumentException ex) {
			System.out.println("Illegal argument!");
		}


		System.out.println(m.getType());



		ByteBuf t = Unpooled.buffer(4096);
		m.writeTo(t);
		Packet temp  = new Packet(t.array(), m.getType(), m);
		toReturn.add(temp);
		return toReturn;
	}



	public boolean verifyPacketOut(ArrayList<Packet> packetOuts){

		OFPacketOut first = (OFPacketOut)packetOuts.get(0).getMessage();
		for (int i = 1; i < packetOuts.size(); i++) {
			if(!first.equalsIgnoreXid((OFPacketOut)packetOuts.get(i).getMessage())){
				return false;
			}
		}

		return true;

	}


	public boolean verifyFlowMods(ArrayList<Packet> flowMods){
		OFFlowMod first = (OFFlowMod)flowMods.get(0).getMessage();
		for (int i = 1; i < flowMods.size(); i++) {

			if(!first.equalsIgnoreXid((OFFlowMod)flowMods.get(i).getMessage())){
				return false;
			}
		}

		return true;
	}
}
