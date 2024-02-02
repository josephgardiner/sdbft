package bftserverproxy;

import java.nio.ByteBuffer;
import java.security.Signature;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
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

	
	public OpenFlowPacketParser() {
		
		
	}

	public List<Packet> parseMessagesLooping(ByteBuf data, int limit) {
		List<Packet> results = new ArrayList<Packet>();
		byte[] bytes = new byte[data.readableBytes()];
		int readerIndex = data.readerIndex();
		data.getBytes(readerIndex, bytes);
		OFMessage m = null;
		OFFactory factory;
		int prev = 0;
		try {
			int count = 0;
			
			while (data.readableBytes() >= 8 && count < limit ) {
				ByteBuf sig = Unpooled.buffer();
				prev = data.readerIndex();
				
			
			

				if (data.getShort(data.readerIndex() + 2) > data.readableBytes() ||data.getShort(data.readerIndex() + 2)==0) {
					break;
				}
				
				//data.getUnsignedShort(data.readerIndex() + 2);
				// System.out.println(data.readableBytes());
				m = OFFactories.getGenericReader().readFrom(data);

				// System.out.println(m);
				// System.out.println(data.readableBytes());
				if (m != null) {
					ByteBuf mData = Unpooled.buffer();
					if (m == null) {
						System.out.println("Message is null");
					}
					// verify signature

					
				//	System.out.println("Verified: " + verified);

					Packet p = new Packet(m.getType(), m);
			//		p.setSignature(null);
					results.add(p);
				}
				count++;
			}
		} catch (OFParseError e) {
			// TODO Auto-generated catch block
			System.out.println("Parse error");
			e.printStackTrace();
		} catch (IllegalArgumentException ex) {
			System.out.println("Illegal argument!");
			data.readerIndex(prev);
		} catch (NullPointerException ex) {
			System.out.println("parser threw null pointer!!!!");
			ex.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Packet toReturn = null;

		// data.resetReaderIndex();
		// toReturn = new Packet(data, m.getType(), m);
		return results;
	}

	public boolean verifyPacketOut(ArrayList<Packet> packetOuts) {

		OFPacketOut first = (OFPacketOut) packetOuts.get(0).getMessage();
		for (int i = 1; i < packetOuts.size(); i++) {
			if (!first.equalsIgnoreXid((OFPacketOut) packetOuts.get(i).getMessage())) {
				return false;
			}
		}

		return true;

	}

	public boolean verifyFlowMods(ArrayList<Packet> flowMods) {
		OFFlowMod first = (OFFlowMod) flowMods.get(0).getMessage();
		for (int i = 1; i < flowMods.size(); i++) {

			if (!first.equalsIgnoreXid((OFFlowMod) flowMods.get(i).getMessage())) {
				return false;
			}
		}

		return true;
	}


	
}
