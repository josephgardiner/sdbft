package bftclientproxy;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.projectfloodlight.openflow.protocol.OFErrorMsg;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFHelloFailedCode;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.errormsg.OFHelloFailedErrorMsg.Builder;
import org.projectfloodlight.openflow.types.OFErrorCauseData;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bftsmart.tom.AsynchServiceProxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

public class BFTClientControl {

	OpenFlowPacketParser parser;
	List<Controller> controllers;

	ClientConnection client;
	byte[] helloMessage;
	ArrayList<Request> requestQueue;
	HashMap<Integer, SwitchEvent> switchEvents;
	HashMap<Integer, ServerConnection> mappedChannels;
	HashMap<OFType, OFType> replyRequestMapping;
	boolean useSigs = false;
	int localPort;

	int xidCounter = 999999;

	SwitchEvent event;
	int controllerCount;

	KeyPair keyPair;
	Signature signer;
	SendAcks ackSender;

	OFVersion oFVersion;
	boolean batchAcks;

	boolean setUpComplete;


	Thread bftThread;

	 int bftClientID;
	
	AsynchServiceProxy serverProxy;
	
	boolean serverConnections;

	public static void main(String[] args) throws IOException {
		try {

			// SimpleCLI cmd = parseArgs(args);
			// int port = Integer.valueOf(cmd.getOptionValue("p"));

			// String host = "192.168.137.129";
			String host = "148.88.227.205";
 
			int remoteport = 6633;
			int localport = 111;
			// Print a start-up message
			System.out.println("Starting proxy for " + host + ":" + remoteport + " on port " + localport);
			// And start running the server
			BFTClientControl control = new BFTClientControl(null);
			control.runServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public BFTClientControl(Properties props) {
		parser = new OpenFlowPacketParser();
		controllers = new ArrayList<Controller>();
		switchEvents = new HashMap<Integer, SwitchEvent>(); 
		requestQueue = new ArrayList<Request>();
		mappedChannels = new HashMap<Integer, ServerConnection>();

		ObjectMapper mapper = new ObjectMapper();
		try {
			controllers = mapper.readValue(props.getProperty("controllers"), new TypeReference<List<Controller>>() {
			});
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < controllers.size(); i++) {
			System.out.println(controllers.get(i));
		}

		this.localPort = Integer.parseInt(props.getProperty("localport"));
		// controllers.add(new Controller("192.168.137.129", 6633));
		// controllers.add(new Controller("10.32.120.40", 6633));
		// controllers.add(new Controller("148.88.227.205", 6633));
		// controllers.add(new Controller("192.168.1.156", 6653,1));
		// controllers.add(new Controller("192.168.1.163", 6653,2));
		// controllers.add(new Controller("127.0.0.1", 6653,1));
		// controllers.add(new Controller("148.88.227.205", 6633));
		// controllers.add(new Controller("192.168.1.156", 6653,1));
		// controllers.add(new Controller("192.168.1.163", 6653,2));

		// controllers.add(new Controller("148.88.227.205", 6653,1));
		// controllers.add(new Controller("127.0.0.1", 6653,2));
		// controllers.add(new Controller("192.168.80.136", 6653, 1));
		// controllers.add(new Controller("192.168.72.1", 6653, 1));

		// controllers.add(new Controller("192.168.1.156", 6653,1));
		// controllers.add(new Controller("192.168.1.163", 6653,2));

		// controllers.add(new Controller("192.168.137.129", 6633));
		// controllers.add(new Controller("10.32.120.40", 6633));
		// controllers.add(new Controller("148.88.227.205", 6633));
		// ryu
		// controllers.add(new Controller("192.168.137.129", 6633));
		// controllers.add(new Controller("148.88.227.201", 6633));
		// controllers.add(new Controller("148.88.227.205", 6633));

		// floodlight
		// controllers.add(new Controller("148.88.227.200", 6653));

		// floodlight local
		// controllers.add(new Controller("148.88.224.127", 6653));

		replyRequestMapping = new HashMap<OFType, OFType>();
		replyRequestMapping.put(OFType.HELLO, OFType.HELLO);
		replyRequestMapping.put(OFType.STATS_REPLY, OFType.STATS_REQUEST);
		replyRequestMapping.put(OFType.BARRIER_REPLY, OFType.BARRIER_REQUEST);
		replyRequestMapping.put(OFType.ROLE_REPLY, OFType.ROLE_REQUEST);
		replyRequestMapping.put(OFType.FEATURES_REPLY, OFType.FEATURES_REQUEST);
		replyRequestMapping.put(OFType.GET_CONFIG_REPLY, OFType.GET_CONFIG_REQUEST);

		controllerCount = controllers.size();

		String ofVersion = props.getProperty("ofversion");
		switch (ofVersion) {
		case "1.0":
			this.oFVersion = OFVersion.OF_10;
			break;
		case "1.1":
			this.oFVersion = OFVersion.OF_11;
			break;
		case "1.2":
			this.oFVersion = OFVersion.OF_12;
			break;
		case "1.3":
			this.oFVersion = OFVersion.OF_13;
			break;
		case "1.4":
			this.oFVersion = OFVersion.OF_14;
			break;
		case "1.5":
			this.oFVersion = OFVersion.OF_15;
			break;
		default:
			this.oFVersion = OFVersion.OF_13;
			break;

		}
		System.out.println("using openflow version: " + oFVersion);

		this.batchAcks = Boolean.parseBoolean(props.getProperty("batchacks"));


	//	serverProxy = new AsynchServiceProxy(Integer.parseInt(props.getProperty("bftid")));
		bftClientID =  Integer.parseInt(props.getProperty("bftclientid"));
		if(props.getProperty("bftclientid")==null) {
			bftClientID=1001;
		}
		serverProxy = new AsynchServiceProxy(bftClientID);

//		bftConnection = new BFTServerConnection(this, Integer.parseInt(props.getProperty("btfid")));

	}
 
	public void setUpServerConnections() {
		// closeServerConnections();
		for (int i = 0; i < controllers.size(); i++) {
			controllers.get(i).setUpConnection(this, parser, oFVersion, bftClientID);
			mappedChannels.put(controllers.get(i).id, controllers.get(i).getConnection());
			byte[] clientID = intToBytes(bftClientID);
			controllers.get(i).send(clientID, clientID.length);
			
		}
		serverConnections=true;

	}

	public void sendToAllServers(byte[] data, int size) {
		// if(!setUpComplete) {
		for (int i = 0; i < controllers.size(); i++) {
			controllers.get(i).send(data.clone(), size);
		}
//		}else {
//			bftConnection.updateMessage(data);
//			bftThread = new Thread(bftConnection);
//			bftThread.start();
//		}
	}

	public void sendToClient(byte[] data, int size) {
		if (client != null) {
			client.send(data, size);
		} else {
			System.out.println("Client not connected");
		}
	}

	public void sendHelloMessage() {
		if (client != null) {
			client.send(helloMessage, helloMessage.length);
		} else {
			System.out.println("Client not connected");
		}
	}

	/**
	 * runs a single-threaded proxy server on the specified local port. It never
	 * returns.
	 */
	public void runServer() throws IOException {
		// Create a ServerSocket to listen for connections with
		ServerSocket ss = new ServerSocket(localPort);

		Socket clientSock = null;
		boolean second = false;
		while (true) {
			try {
				// Wait for a connection on the local port
				clientSock = ss.accept();
				clientSock.setTcpNoDelay(true);
				System.out.println("got connection from: " + clientSock.getRemoteSocketAddress());
				client = new ClientConnection(clientSock, this, parser, oFVersion, serverProxy);
		//		sendHelloMessage();
				// closeServerConnections();
				if (!second) {
					
					setUpServerConnections();

				}
				second = true;
				// sendHelloMessage();
				Thread t = new Thread(client);
				t.start();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void setHelloMessage(byte[] message) {
		this.helloMessage = message;
	}

	public void closeServerConnections() {
		for (int i = 0; i < controllers.size(); i++) {

			controllers.get(i).stop();
		}
	}

	public void addSwitchEvent(Integer key, SwitchEvent value) {
		switchEvents.put(key, value);
	}

	public void deleteSwitchEvent(Integer key) {
		this.switchEvents.remove(key);
	}

	public SwitchEvent getSwitchEvent(Integer key) {
		return switchEvents.get(key);
	}

	public int getNextXid() {
		xidCounter = xidCounter + 1;
		return xidCounter;
	}

	public void handlePacket(Packet packet, byte[] raw, int rawSize) {
		if (event != null) {
			switch (packet.getType()) {
			case PACKET_OUT:
				event.addPacketOut(packet);
				System.out.println("Packet Out");
				System.out.println(packet.getMessage());

				if (event.getPacketOuts().size() == controllerCount) {
					// if (parser.verifyPacketOut(event.getPacketOuts())) {
					System.out.println("Sending packet out");
					sendToClient(raw, rawSize);

					// }
				}

				break;
			case FLOW_MOD:
				event.addFlowMod(packet);
				System.out.println("Flow Mod");
				System.out.println(packet.getMessage());
				if (event.getFlowMods().size() == controllerCount) {
					if (parser.verifyFlowMods(event.getFlowMods())) {
						System.out.println("sending flow mod");
						sendToClient(raw, rawSize);

					}
					// event=null;
				}

				break;
			}
			// if(event.getPacketOuts().size()==controllerCount && )
		} else {
			sendToClient(raw, rawSize);
		}
	}

	public int getControllerCount() {
		return this.controllers.size();
	}

	public void handlePacketIn(Packet packet) {
		// event = new SwitchEvent((OFPacketIn) packet.getMessage());

	}

	public void queueRequest(Request r) {
		requestQueue.add(r);
	}

	/**
	 * COde for processing fowmod/packetout messages If not received one from each
	 * controller, adds to matching SwitchEvent (according to xid) Otherwise,
	 * verifies all match and send to switch If no switchEvent exists, sends to
	 * client anyway
	 * 
	 * @param packet
	 * @param ctx
	 */
	public synchronized void processResponseList(List<Packet> packets, int id) {
		ByteBuf toWrite = Unpooled.buffer();
		String ackMessage = null;
	//	OFVersion ackVersion = null;
		for (Packet packet : packets) {
				ByteBuf t = Unpooled.buffer();
				// System.out.println("Found event");
				SwitchEvent event = getSwitchEvent((int) packet.getMessage().getXid());
				switch (packet.getType()) {
				case PACKET_OUT:
				System.out.println("server packet out");
							packet.getMessage().writeTo(toWrite);

					break;
				case FLOW_MOD:

							packet.getMessage().writeTo(toWrite);

					break;
				case FEATURES_REQUEST:
					queueRequest(new Request(id, packet.getMessage().getType(), packet.getMessage().getXid(),
							packet.getMessage()));
					packet.getMessage().writeTo(t);
					t.setInt(4, id);
					toWrite.writeBytes(t);
					// packet.getMessage().writeTo(toWrite);
					break;
				case STATS_REQUEST:
					queueRequest(new Request(id, packet.getMessage().getType(), packet.getMessage().getXid(),
							packet.getMessage()));
					packet.getMessage().writeTo(t);
					t.setInt(4, id);
					toWrite.writeBytes(t);
					// packet.getMessage().writeTo(toWrite);
					break;
				case BARRIER_REQUEST:
					queueRequest(new Request(id, packet.getMessage().getType(), packet.getMessage().getXid(),
							packet.getMessage()));
					packet.getMessage().writeTo(t);
					t.setInt(4, id);
					toWrite.writeBytes(t);
					// packet.getMessage().writeTo(toWrite);
					break;
				case ROLE_REQUEST:
					queueRequest(new Request(id, packet.getMessage().getType(), packet.getMessage().getXid(),
							packet.getMessage()));
					packet.getMessage().writeTo(t);
					t.setInt(4, id);
					toWrite.writeBytes(t);
					// packet.getMessage().writeTo(toWrite);
					break;
				case GET_CONFIG_REQUEST:
					queueRequest(new Request(id, packet.getMessage().getType(), packet.getMessage().getXid(),
							packet.getMessage()));
					packet.getMessage().writeTo(t);
					t.setInt(4, id);
					toWrite.writeBytes(t);
					ByteBuf temp = Unpooled.buffer();
					
					// packet.getMessage().writeTo(toWrite);
					break;
				case ECHO_REQUEST:
					break;
				case HELLO:
					// control.queueRequest(new Request(this.id,
					// packet.getMessage().getType(),
					// packet.getMessage().getXid(), packet.getMessage()));
					// packet.getMessage().writeTo(t);
					// t.setInt(4, this.id);
					// toWrite.writeBytes(t);
					// packet.getMessage().writeTo(toWrite);
					break;
				default:
					packet.getMessage().writeTo(toWrite);

					break;
				}
			}

		

		// write buffer to channel
		byte[] bytes = new byte[toWrite.readableBytes()];
		int readerIndex = toWrite.readerIndex();
		toWrite.getBytes(readerIndex, bytes);
		byte[] copyTo = new byte[65535];
		System.arraycopy(bytes, 0, copyTo, 0, bytes.length);
		sendToClient(copyTo, bytes.length);
		// long startTime = System.nanoTime();
		if (ackMessage != null) {
			sendAckBroadcast(ackMessage, oFVersion);
		}
		// long endTime = System.nanoTime();
		// System.out.println("Ack: " + (endTime - startTime));
	}

	public void sendAckBroadcast(String message, OFVersion version) {
		ByteBuf ackBuf = Unpooled.buffer();
		Builder replyBuilder = OFFactories.getFactory(version).errorMsgs().buildHelloFailedErrorMsg();
		OFErrorCauseData data = new OFErrorCauseData((message).getBytes(), version);
		// System.out.println(new String(data.getData()));
		replyBuilder.setData(data);
		replyBuilder.setCode(OFHelloFailedCode.EPERM);
		OFErrorMsg ack = replyBuilder.build();
		ack.writeTo(ackBuf);
		byte[] bytes = new byte[ackBuf.readableBytes()];
		int readerIndex = ackBuf.readerIndex();
		ackBuf.getBytes(readerIndex, bytes);
		// sendToAllServers(bytes, bytes.length);
		byte[] copyTo = new byte[65535];
		System.arraycopy(bytes, 0, copyTo, 0, bytes.length);

		if (batchAcks) {
			ackSender.addMessage(bytes);
		} else {
			sendToAllServers(copyTo, bytes.length);

		}
		// ackSender.addMessage(bytes);
		// control.controllerChannelGroup.writeAndFlush((Object)ackBuf);
	}

	public void processResponse(Packet p) {
		OFType expected = replyRequestMapping.get(p.getType());
		// System.out.println("Expected: " + expected);
		boolean found = false;
		for (int i = 0; i < requestQueue.size(); i++) {
			// System.out.println("Checking: " + requestQueue.get(i).getType());
			if (requestQueue.get(i).getType().equals(expected)
					&& p.getMessage().getXid() == requestQueue.get(i).getController()) {
				// System.out.println("Found request");
				ByteBuf toWrite = Unpooled.buffer();
				p.getMessage().writeTo(toWrite);
				toWrite.setInt(4, (int) requestQueue.get(i).getXid());
				toWrite.writeBytes(toWrite);
				// packet.getMessage().writeTo(toWrite);
				byte[] bytes = new byte[toWrite.readableBytes()];
				int readerIndex = toWrite.readerIndex();
				toWrite.getBytes(readerIndex, bytes);

				mappedChannels.get(requestQueue.get(i).getController()).send(bytes, bytes.length);

				if (p.getType() != OFType.STATS_REPLY) {
					requestQueue.remove(i);
				}
				found = true;
				break;
			}
		}
		if (p.getType() == OFType.ERROR) {
			for (int i = 0; i < requestQueue.size(); i++) {
				// System.out.println("Checking: " +
				// requestQueue.get(i).getType());
				if (p.getMessage().getXid() == requestQueue.get(i).getController()) {
					// System.out.println("Found request");
					ByteBuf toWrite = Unpooled.buffer();
					p.getMessage().writeTo(toWrite);
					toWrite.setInt(4, (int) requestQueue.get(i).getXid());
					toWrite.writeBytes(toWrite);
					// p.getMessage().writeTo(toWrite);
					byte[] bytes = new byte[toWrite.readableBytes()];
					int readerIndex = toWrite.readerIndex();
					toWrite.getBytes(readerIndex, bytes);

					mappedChannels.get(requestQueue.get(i).getController()).send(bytes, bytes.length);
					if (p.getType() != OFType.STATS_REPLY) {
						requestQueue.remove(i);
					}
					found = true;
					break;
				}
			}
		}
		if (!found) {
			System.out.println("not found: " + p.getType());
			ByteBuf toWrite = Unpooled.buffer();
			p.getMessage().writeTo(toWrite);
			byte[] bytes = new byte[toWrite.readableBytes()];
			int readerIndex = toWrite.readerIndex();
			toWrite.getBytes(readerIndex, bytes);

			sendToAllServers(bytes, bytes.length);
		}

	}

	public OFVersion getoFVersion() {
		return oFVersion;
	}

	public void setoFVersion(OFVersion oFVersion) {
		this.oFVersion = oFVersion;
	}

	public void pickNewQuorum() {

	}

	public void handleControllerFault() {

	}
	
	public static byte[] intToBytes( int data) {
	    return new byte[] {
	        (byte)((data >> 24) & 0xff),
	        (byte)((data >> 16) & 0xff),
	        (byte)((data >> 8) & 0xff),
	        (byte)((data >> 0) & 0xff),
	    };
	}
	
	public int convertByteArrayToInt(byte[] data) {
	    if (data == null || data.length != 4) return 0x0;
	    // ----------
	    return (int)( // NOTE: type cast not necessary for int
	            (0xff & data[0]) << 24  |
	            (0xff & data[1]) << 16  |
	            (0xff & data[2]) << 8   |
	            (0xff & data[3]) << 0
	            );
	}
}