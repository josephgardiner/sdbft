package simpleproxy;

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

	KeyPair keyPair;
	Signature signer ; 
	public OpenFlowPacketParser() {
		
		try {
		this.keyPair = generateKeyPair(999);
		
		signer = Signature.getInstance("SHA512withRSA");
			signer.initSign(keyPair.getPrivate());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Packet parseMessages(ByteBuf data, int limit) {
		List<OFMessage> results = new ArrayList<OFMessage>();
		byte[] bytes = new byte[data.readableBytes()];
		int readerIndex = data.readerIndex();
		data.getBytes(readerIndex, bytes);
		OFMessage m = null;
		OFFactory factory;
		try {
			System.out.println("OFLength: " + data.getUnsignedShort(2));
			// System.out.println(data.readableBytes());
			m = OFFactories.getGenericReader().readFrom(data);
			// System.out.println(data.readableBytes());
			if (data.readableBytes() > 0) {
				System.out.println("Supplementary bytes : " + data.readableBytes());
				OFMessage m2 = OFFactories.getGenericReader().readFrom(data);
				System.out.println(m2.getType());
			}
			
		} catch (OFParseError e) {
			// TODO Auto-generated catch block
			System.out.println("Parse error");
			e.printStackTrace();
		} catch (IllegalArgumentException ex) {
			System.out.println("Illegal argument!");
		}

		// System.out.println(m.getVersion());
		// factory = OFFactories.getFactory(m.getVersion());

		// System.out.println(m.getType());
		// OFFactory
		Packet toReturn = null;

		// switch (m.getType()) {
		// case PACKET_IN:
		// System.out.println("Packet in!!");
		//
		// OFPacketIn packetIn = (OFPacketIn) m;
		//

		// System.out.println(packetIn);
		//
		// Match.Builder builder = factory.buildMatch();
		// Match match = packetIn.getMatch();
		// // System.out.println(
		// // org.projectfloodlight.openflow.types.U32.t(packetIn.getXid()));
		// // System.out.println(match);
		// for (MatchField field : match.getMatchFields()) {
		// System.out.println(field.getName());
		//
		// }
		//
		// toReturn = new Packet(bytes, m.getType(), m);
		// // match.loadFromPacket(packetIn.getData(), packetIn.getInPort());
		// return toReturn;
		// // break;
		// case HELLO:
		// // System.err.println("GOT HELLO from " + sw);
		//
		// control.setHelloMessage(bytes);
		// break;
		// case ECHO_REQUEST:
		//
		// break;
		// case FLOW_MOD:
		//
		// OFFlowMod packet = (OFFlowMod) m;
		// Match modMatch = packet.getMatch();
		//
		// for (OFAction a : packet.getActions()) {
		// System.out.println(a);
		//
		// }
		//
		// for (MatchField a : packet.getMatch().getMatchFields()) {
		// System.out.println(a.getName());
		// System.out.println(packet.getMatch());
		// System.out.println(modMatch.get(a));
		// }
		// return packet;
		// // break;
		// default:
		// // System.err.println("Unhandled OF message: "
		// // + m.getType());
		// }
		data.resetReaderIndex();
		toReturn = new Packet( m.getType(), m);
		return toReturn;
	}

	public List<Packet> parseMessagesLooping(ByteBuf data, int limit) {
		List<Packet> results = new ArrayList<Packet>();
		byte[] bytes = new byte[data.readableBytes()];
		int readerIndex = data.readerIndex();
		data.getBytes(readerIndex, bytes);
		OFMessage m = null;
		OFFactory factory;
		try {
			int count = 0;
			while (data.readableBytes() > 0 && count < limit) {
				 System.out.println("OFLength: " + data.getUnsignedShort(2));
				if (data.getUnsignedShort(2) > data.readableBytes()) {
					break;
				}
				// System.out.println(data.readableBytes());
				m = OFFactories.getGenericReader().readFrom(data);
				// System.out.println(m);
				// System.out.println(data.readableBytes());
				if (m != null) {
					ByteBuf mData = Unpooled.buffer();
					if (m == null) {
						System.out.println("Message is null");
					}
					m.writeTo(mData);

					Packet p = new Packet( m.getType(), m);
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
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			System.out.println("parser threw null pointer!!!!");
			ex.printStackTrace();
		}

		// Packet toReturn = null;

		// data.resetReaderIndex();
		// toReturn = new Packet(data, m.getType(), m);
		return results;
	}

	public List<Packet> parseMessagesLoopingWithSig(ByteBuf data, int limit) {
		List<Packet> results = new ArrayList<Packet>();
		byte[] bytes = new byte[data.readableBytes()];
		int readerIndex = data.readerIndex();
		data.getBytes(readerIndex, bytes);
		OFMessage m = null;
		OFFactory factory;
		int prev = 0;
		try {
			int count = 0;
			
			while (data.readableBytes() > 0 && count < limit && data.readableBytes()>128) {
				ByteBuf sig = Unpooled.buffer();
				prev = data.readerIndex();
				
				data.readBytes(sig, 128);
				// System.out.println("FOUND SIGNATURE!!!!!!!!!!!!!!!!:");
				// System.out.println(new String(sig.array()));
				byte[] sigBytes = new byte[sig.readableBytes()];
				sig.readBytes(sigBytes);
				
			//	System.out.println("Post sig readable: " + data.readableBytes());
		//		System.out.println("OFLength: " + data.getShort(data.readerIndex() + 2));
				//int size = data.getUnsignedShort(data.readerIndex() + 2);

				if (data.getShort(data.readerIndex() + 2) > data.readableBytes() ||data.getShort(data.readerIndex() + 2)==0) {
					data.readerIndex(data.readerIndex() - 128);
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

					m.writeTo(mData);
					byte[] mBytes = new byte[mData.readableBytes()];
					int mReader = mData.readerIndex();
					mData.getBytes(mReader, mBytes);
					boolean verified;

					verified = verifySig(mBytes, sigBytes);
				//	System.out.println("Verified: " + verified);

					Packet p = new Packet(m.getType(), m);
					p.setSignature(sigBytes);
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

	public Packet parseMessagesSig(ByteBuf data, int limit) {
		List<OFMessage> results = new ArrayList<OFMessage>();
		byte[] bytes = new byte[data.readableBytes()];
		int readerIndex = data.readerIndex();
		data.getBytes(readerIndex, bytes);
		OFMessage m = null;
		OFFactory factory;
		try {
			ByteBuf sig = Unpooled.buffer(5);

			data.readBytes(sig, 5);
//			System.out.println("FOUND SIGNATURE!!!!!!!!!!!!!!!!:");
	//		System.out.println(new String(sig.array()));
			m = OFFactories.getGenericReader().readFrom(data);
		} catch (OFParseError e) {
			// TODO Auto-generated catch block
			System.out.println("Parse error");
			e.printStackTrace();
		} catch (IllegalArgumentException ex) {
			System.out.println("Illegal argument!");
		}

		Packet toReturn = null;

		data.resetReaderIndex();
		toReturn = new Packet( m.getType(), m);
		return toReturn;
	}

	public boolean verifySig(byte[] data, byte[] sig) throws Exception {
	//	KeyPair keyPair = generateKeyPair(999);

		Signature signer = Signature.getInstance("SHA512withRSA");
		signer.initVerify(keyPair.getPublic());
		signer.update(data);
		return (signer.verify(sig));
	}

	public static KeyPair generateKeyPair(long seed) throws Exception {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
		SecureRandom rng = SecureRandom.getInstance("SHA1PRNG", "SUN");
		rng.setSeed(seed);
		keyGenerator.initialize(1024, rng);

		return (keyGenerator.generateKeyPair());
	}
}
