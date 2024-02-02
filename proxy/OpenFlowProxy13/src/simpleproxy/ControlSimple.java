package simpleproxy;

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

public class ControlSimple {

	OpenFlowPacketParser parser;
	List<Controller> controllers;

	ClientConnection client;
	int localPort;

	HashMap<Integer, ServerConnection> mappedChannels;
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
			ControlSimple control = new ControlSimple(null);
			control.runServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ControlSimple(Properties props) {
		parser = new OpenFlowPacketParser();
		controllers = new ArrayList<Controller>();
		mappedChannels = new HashMap<Integer, ServerConnection>();
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			controllers = mapper.readValue(props.getProperty("controllers"), new TypeReference<List<Controller>>(){});
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
	//	controllers.add(new Controller("192.168.80.135", 6653, 1));

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

		controllerCount = controllers.size();

	}

	public void setUpServerConnections() {
		// closeServerConnections();
		for (int i = 0; i < controllers.size(); i++) {
			controllers.get(i).setUpConnection(this, parser);
			mappedChannels.put(controllers.get(i).id, controllers.get(i).getConnection());
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
				client = new ClientConnection(clientSock, this, parser);
				// close!ServerConnections();
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

	public Packet parseMessage(byte[] data) {
		ByteBuf buffer = Unpooled.wrappedBuffer(data);
		return parser.parseMessages(buffer, 100);

	}

	public List<Packet> parseSigMessage(byte[] data) {
		ByteBuf buffer = Unpooled.wrappedBuffer(data);
		return parser.parseMessagesLoopingWithSig(buffer, 100);

	}



	public void closeServerConnections() {
		for (int i = 0; i < controllers.size(); i++) {

			controllers.get(i).stop();
		}
	}






	public int getControllerCount() {
		return this.controllers.size();
	}

	

	
}