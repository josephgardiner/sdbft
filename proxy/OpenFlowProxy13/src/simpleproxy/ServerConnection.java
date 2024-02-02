package simpleproxy;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.projectfloodlight.openflow.protocol.OFEchoReply;
import org.projectfloodlight.openflow.protocol.OFEchoRequest.Builder;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFHello;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ServerConnection implements Runnable {
	String IP;
	Socket sock;
	ControlSimple control;
	InputStream streamFromServer;
	OutputStream streamToServer;
	Controller controller;
	ByteBuf buffer;
	OpenFlowPacketParser parser;

	public void close() {
		try {
			sock.close();
			streamToServer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ServerConnection(String iP, Socket s, ControlSimple control, Controller controller, OpenFlowPacketParser parser) {
		this.IP = iP;
		this.sock = s;
		this.control = control;
		this.controller = controller;
		this.parser = parser;
		buffer = Unpooled.buffer();

		try {
			streamFromServer = sock.getInputStream();

			streamToServer = new BufferedOutputStream(sock.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void send(byte[] data, int bytesRead) {
		try {
			// System.out.println("sending to server");
			streamToServer.write(data, 0, bytesRead);
			streamToServer.flush();
		    streamToServer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// while (true) {
		byte[] reply = new byte[65534];
		// while (true) {
		int bytesRead;
		try {
			// Thread.sleep(1);

			// System.out.println("Waiting for server message");

			while ((bytesRead = streamFromServer.read(reply)) != -1) {
		//		ByteBuf m = Unpooled.wrappedBuffer(reply);
				ByteBuf m=Unpooled.copiedBuffer(reply, 0, bytesRead);

				control.sendToClient(reply, bytesRead);
//				
			//Thread.yield();
			}
			
			// System.out.println("Read from server");
		} catch (IOException e) {
			System.out.println("Server IO Exception");
		}
	}

	// }

}
