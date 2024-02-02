package proxy;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Control {

	OpenFlowPacketParser parser;
	ArrayList<Controller> controllers;

	ClientConnection client;
	byte[] helloMessage;

	SwitchEvent event;
	int controllerCount;

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
			Control control = new Control();
			control.runServer(localport);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Control() {
		parser = new OpenFlowPacketParser(this);
		controllers = new ArrayList<Controller>();

	//	controllers.add(new Controller("192.168.137.129", 6633));
	//	 controllers.add(new Controller("10.32.120.40", 6633));
//		controllers.add(new Controller("148.88.227.205", 6633));
		controllers.add(new Controller("192.168.1.136", 6653));

		controllers.add(new Controller("192.168.137.129", 6633));
	//	 controllers.add(new Controller("10.32.120.40", 6633));
		controllers.add(new Controller("148.88.227.205", 6633));
		//ryu
	//	controllers.add(new Controller("192.168.137.129", 6633));
	//	 controllers.add(new Controller("148.88.227.201", 6633));
	//	controllers.add(new Controller("148.88.227.205", 6633));

		//floodlight
		controllers.add(new Controller("148.88.227.200", 6653));


		//floodlight local
	//	controllers.add(new Controller("148.88.224.127", 6653));


		controllerCount = controllers.size();

	}

	public void setUpServerConnections() {
		for (int i = 0; i < controllers.size(); i++) {
			controllers.get(i).setUpConnection(this);
		}

	}

	public void sendToAllServers(byte[] data, int size) {
		for (int i = 0; i < controllers.size(); i++) {
			controllers.get(i).send(data.clone(), size);
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
	public void runServer(int localport) throws IOException {
		// Create a ServerSocket to listen for connections with
		ServerSocket ss = new ServerSocket(localport);

		Socket clientSock = null;
		boolean second  =false;
		while (true) {
			try {
				// Wait for a connection on the local port
				clientSock = ss.accept();

				System.out.println("got connection from: " + clientSock.getRemoteSocketAddress());
				client = new ClientConnection(clientSock, this);
				// closeServerConnections();
				if(!second){
				setUpServerConnections();

				}
			//	second = true;
				// sendHelloMessage();
				Thread t = new Thread(client);
				t.start();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public Packet parseMessage(byte[] data) {
		ByteBuf buffer = Unpooled.wrappedBuffer(data);
		return parser.parseMessages(buffer, 100);

	}

	public ParsedPacket parseSigMessage(byte[] data) {
		ByteBuf buffer = Unpooled.wrappedBuffer(data);
		return parser.parseMessagesWithSignatures(buffer, 100);

	}

	public void setHelloMessage(byte[] message) {
		this.helloMessage = message;
	}

	public void closeServerConnections() {
		for (int i = 0; i < controllers.size(); i++) {
			controllers.get(i).stop();
		}
	}

	public void createEvent(OFPacketIn packetIn) {
		event = new SwitchEvent(packetIn);
	}

	public void handlePacket(Packet packet, byte[] raw, int rawSize) {
		if (event != null) {
			switch (packet.getType()) {
			case PACKET_OUT:
				event.addPacketOut(packet);
				System.out.println("Packet Out");
				System.out.println(packet.getMessage());

				if (event.getPacketOuts().size() == controllerCount) {
				//	if (parser.verifyPacketOut(event.getPacketOuts())) {
					System.out.println("Sending packet out");
						sendToClient(raw, rawSize);

				//	}
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

	public void handlePacketIn(Packet packet) {
	//	event = new SwitchEvent((OFPacketIn) packet.getMessage());

	}
}