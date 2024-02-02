package hybridproxy;

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
import java.util.Iterator;
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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

public class Control {

	OpenFlowPacketParser parser;
	List<Controller> controllers;
	List<Controller> backupControllers;
	int totalControllers;
	ClientConnection client;
	byte[] helloMessage;
	ArrayList<Request> requestQueue;
	HashMap<Integer, SwitchEvent> switchEvents;
	HashMap<Integer, ServerConnection> mappedChannels;
	HashMap<OFType, OFType> replyRequestMapping;
	boolean useSigs = false;
	int localPort;

	int xidCounter;

	SwitchEvent event;
	int controllerCount;

	KeyPair keyPair;
	Signature signer;
	SendAcks ackSender;

	OFVersion oFVersion;
	boolean batchAcks;

	boolean failureMode;
	boolean failureModeStarted;
	
	int majority;
	boolean loadBackups;
	boolean handshakeDone;
	long startTime; 

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
			Control control = new Control(null);
			control.runServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Control(Properties props) {
		parser = new OpenFlowPacketParser();
		controllers = new ArrayList<Controller>();
		switchEvents = new HashMap<Integer, SwitchEvent>();
		requestQueue = new ArrayList<Request>();
		mappedChannels = new HashMap<Integer, ServerConnection>();

		ObjectMapper mapper = new ObjectMapper();
		 loadBackups = false;
		try {
			controllers = mapper.readValue(props.getProperty("controllers"), new TypeReference<List<Controller>>() {
			});
			 loadBackups = Boolean.parseBoolean(props.getProperty("loadbackups"));
			if(loadBackups) {
			backupControllers = mapper.readValue(props.getProperty("backupcontrollers"),
					new TypeReference<List<Controller>>() {
			});
			}
			if(loadBackups) {
			totalControllers = controllers.size() + backupControllers.size();
			}else {
				totalControllers = controllers.size();
			}
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
		System.out.println("Controllers");
		for (int i = 0; i < controllers.size(); i++) {
			System.out.println(controllers.get(i));
		}
		if(loadBackups) {
		System.out.println("Backups");
		for (int i = 0; i < backupControllers.size(); i++) {
			System.out.println(backupControllers.get(i));
		}
		}
		this.localPort = Integer.parseInt(props.getProperty("localport"));
		this.xidCounter = Integer.parseInt(props.getProperty("startxid"));
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
		System.out.println("Batch sending acks: " + batchAcks);
		if (batchAcks) {
			ackSender = new SendAcks(this);
			Thread t = new Thread(ackSender);
			t.start();
		}

	}

	public void setUpServerConnections() {
		// closeServerConnections();
		startTime = System.currentTimeMillis();
		for (int i = 0; i < controllers.size(); i++) {
			controllers.get(i).setUpConnection(this, parser, oFVersion);
			mappedChannels.put(controllers.get(i).id, controllers.get(i).getConnection());
		}

	}

	public void sendToAllServers(byte[] data, int size) {
		for (int i = 0; i < controllers.size(); i++) {
			controllers.get(i).send(data.clone(), size);
		}
		if(failureMode) {
			sendToAllBackupServers(data, size);
		}
	}
	public void sendToAllBackupServers(byte[] data, int size) {
		for (int i = 0; i < backupControllers.size(); i++) {
			backupControllers.get(i).send(data.clone(), size);
		}
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
				client = new ClientConnection(clientSock, this, parser, oFVersion);
				// closeServerConnections();
				if (second) {
					setUpServerConnections();
					if(loadBackups) {
					setUpBackupServerConnections();
					}
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
	
	public int getCountrollerCountFailureMode() {
		return this.controllers.size() + this.backupControllers.size();
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
		if (failureMode) {
			System.out.println("using failure mode");
			processResponseListFailureMode(packets, id);
		} else {
			ByteBuf toWrite = Unpooled.buffer();
			String ackMessage = null;
			// OFVersion ackVersion = null;
			for (Packet packet : packets) {
	//			System.out.println(packet.getType());
				if (getSwitchEvent((int) packet.getMessage().getXid()) != null) {
					// System.out.println("Found event");
					SwitchEvent event = getSwitchEvent((int) packet.getMessage().getXid());
					switch (packet.getType()) {
					case PACKET_OUT:
						event.addPacketOut(packet);
						// System.out.println("Packet Out " +
						// packet.getMessage().getXid());
						// System.out.println(packet.getMessage());
					//	System.out.println(
					//			"Packet outs: " + event.packetOuts.size() + " control count: " + getControllerCount());
						if (event.getPacketOuts().size() == getControllerCount()) {
							if (parser.verifyPacketOut(event.getPacketOuts())) {
				//				System.out.println("Got packet out group: " + event.packetOuts.size());

								packet.getMessage().writeTo(toWrite);
								event.setPacketOutSent(true);

							} else {
								System.out.println("FAULT DETECTED ON PACKET OUT!! Controller: " + id);
								
								for(Packet p :event.getPacketOuts()) {
									System.out.println(p);
								}
								handleControllerFault(event);
								event.getData().resetReaderIndex();

								byte[] bytes = new byte[event.getData().readableBytes()];

								int readerIndex = event.getData().readerIndex();
								event.getData().getBytes(readerIndex, bytes);
								sendToAllBackupServers(bytes, bytes.length);							//	this.failureMode = true;
							}
							// if (event.flowMods.size() == 0) {
							// control.deleteSwitchEvent(event.getXid());
							// }

							ackMessage = "##ACK##PACKETOUT##";
							// ackVersion = packet.getMessage().getVersion();
							// sendAckBroadcast("##PACKETOUT##",
							// packet.getMessage().getVersion());
						}

						break;
					case FLOW_MOD:
						event.addFlowMod(packet);
						// System.out.println("Flow Mod "+
						// packet.getMessage().getXid());

			//			 System.out.println(packet.getMessage());
			//			System.out.println(
			//					"Flow mods: " + event.flowMods.size() + " control count: " + getControllerCount());

						if (event.getFlowMods().size() == getControllerCount()) {
							
							
							if (parser.verifyFlowMods(event.getFlowMods())) {
					//			 System.out.println("Got flow mod group: "+ event.packetOuts.size());
								packet.getMessage().writeTo(toWrite);
								event.setFlowModSent(true);

							} else {
								System.out.println("FAULT DETECTED ON FLOW MOD!!");
								handleControllerFault(event);
								event.getData().resetReaderIndex();

								byte[] bytes = new byte[event.getData().readableBytes()];
								int readerIndex = event.getData().readerIndex();
								event.getData().getBytes(readerIndex, bytes);
								sendToAllBackupServers(bytes, bytes.length);	
							}
							// event=null;
							// if (event.flowMods.size() == 0) {
							// control.deleteSwitchEvent(event.getXid());
							// }

							ackMessage = "##ACK##FLOWMOD##";
							// ackVersion = packet.getMessage().getVersion();

							// sendAckBroadcast("##FLOWMOD##",
							// packet.getMessage().getVersion());
					//		switchEvents.remove(packet.getMessage().getXid());
						}
						break;

					}
					// if(event.getPacketOuts().size()==controllerCount && )
				} else {
					ByteBuf t = Unpooled.buffer();

					switch (packet.getType()) {
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
						// packet.getMessage().writeTo(toWrite);
						break;
					case ECHO_REQUEST:
						break;
					case PACKET_OUT:
	//					System.out.println("PO: " + packet.getMessage().getXid());
						packet.getMessage().writeTo(t);
						toWrite.writeBytes(t);
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
	
	public void sendAckBroadcastFailure(String message, OFVersion version) {
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
			sendToAllBackupServers(copyTo, bytes.length);

		}
		// ackSender.addMessage(bytes);
		// control.controllerChannelGroup.writeAndFlush((Object)ackBuf);
	}

	
	public synchronized void processResponseListFailureMode(List<Packet> packets, int id) {


			ByteBuf toWrite = Unpooled.buffer();
			String ackMessage = null;
			// OFVersion ackVersion = null;
			for (Packet packet : packets) {
				System.out.println(packet.getType());
				if (getSwitchEvent((int) packet.getMessage().getXid()) != null) {
					// System.out.println("Found event");
					SwitchEvent event = getSwitchEvent((int) packet.getMessage().getXid());
					switch (packet.getType()) {
					case PACKET_OUT:
					//	event.addPacketOut(packet);
						// System.out.println("Packet Out " +
						// packet.getMessage().getXid());
						// System.out.println(packet.getMessage());
						System.out.println(
								"Packet outs: " + event.packetOuts.size() + " control count: " + getCountrollerCountFailureMode() + " agreement: " + parser.verifyPacketOutFailure(packet,event.getPacketOuts()) + " majority: " + majority);
							if (parser.verifyPacketOutFailure(packet,event.getPacketOuts())>=majority && !event.packetOutSent) {
								 System.out.println("Got packet out  group");

								packet.getMessage().writeTo(toWrite);
								event.setPacketOutSent(true);

							} else {
								System.out.println("FAULT DETECTED ON PACKET OUT!!");
								this.failureMode = true;
							}
							event.addPacketOut(packet);
							// if (event.flowMods.size() == 0) {
							// control.deleteSwitchEvent(event.getXid());
							// }

							ackMessage = "##ACK##PACKETOUT##";
							// ackVersion = packet.getMessage().getVersion();
							// sendAckBroadcast("##PACKETOUT##",
							// packet.getMessage().getVersion());
						

						break;
					case FLOW_MOD:
					//	event.addFlowMod(packet);
						// System.out.println("Flow Mod "+
						// packet.getMessage().getXid());

						// System.out.println(packet.getMessage());
						System.out.println(
								"Flow mods: " + event.flowMods.size() + " control count: " + getCountrollerCountFailureMode() + " agreement: " +parser.verifyFlowModsFailure(packet,event.getFlowMods()) + " majority: " + majority);

						
							if (parser.verifyFlowModsFailure(packet,event.getFlowMods())>=majority && !event.flowModSent) {
								 System.out.println("Got flow mod group");
								packet.getMessage().writeTo(toWrite);
								event.setFlowModSent(true);

							} else {
								System.out.println("FAULT DETECTED ON FLOW MOD!!");
								this.failureMode = true;
							}
							event.addFlowMod(packet);
							// event=null;
							// if (event.flowMods.size() == 0) {
							// control.deleteSwitchEvent(event.getXid());
							// }

							ackMessage = "##ACK##FLOWMOD##";
							// ackVersion = packet.getMessage().getVersion();

							// sendAckBroadcast("##FLOWMOD##",
							// packet.getMessage().getVersion());

						
						break;

					}
					// if(event.getPacketOuts().size()==controllerCount && )
				} else {
					ByteBuf t = Unpooled.buffer();

					switch (packet.getType()) {
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



	public void processResponse(Packet p) {
		OFType expected = replyRequestMapping.get(p.getType());
		// System.out.println("Expected: " + expected);
		boolean found = false;
		for (int i = 0; i < requestQueue.size(); i++) {
			// System.out.println("Checking: " + requestQueue.get(i).getType());
			if (requestQueue.get(i).getType().equals(expected)
					&& p.getMessage().getXid() == requestQueue.get(i).getController()) {
				// System.out.println("Found request");
				System.out.println("Matched reply from switch:" + p.getType());
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
			//		requestQueue.remove(i);
				}else {
					for(Controller c : controllers) {
						if(c.getId()==requestQueue.get(i).getController()){
							c.setHandshakeDone(true);
						}
					}
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
		if (!failureModeStarted) {
			failureModeStarted = true;
			
			setUpBackupServerConnections();
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void setUpBackupServerConnections() {
		// closeServerConnections();
	    System.out.println("Settign up backups");
		for (int i = 0; i < backupControllers.size(); i++) {
			backupControllers.get(i).setUpConnection(this, parser, oFVersion);
			mappedChannels.put(backupControllers.get(i).id, backupControllers.get(i).getConnection());
			this.majority = (int)(mappedChannels.size()/2)+1;
		}

	}

	public void handleControllerFault(SwitchEvent event) {
		
		System.out.println("Failover protocol initialised!!!");
		failureMode = true;
		sendAckBroadcastFailure("##ACK##FAILURE##", oFVersion);
		
	//	pickNewQuorum();
		
		

	}

	public boolean isFailureMode() {
		return failureMode;
	}

	
	public boolean handshakesDone() {
		for(Controller c : controllers){
			if(!c.isHandshakeDone()) {
				return false;
			}
			
		}
		return true;
	}
	
	public boolean timeSinceStart(long amount)
	{
		long time = System.currentTimeMillis() - startTime;
		if(time > amount) {
			return true;
		}
		return false;
	}
}