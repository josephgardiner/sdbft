package proxy;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.projectfloodlight.openflow.protocol.OFEchoReply;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ServerConnection implements Runnable {
	String IP;
	Socket sock;
	Control control;
	InputStream streamFromServer;
	OutputStream streamToServer;

	public void close() {
		try {
			sock.close();
			streamToServer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ServerConnection(String iP, Socket s, Control control) {
		this.IP = iP;
		this.sock = s;
		this.control = control;
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// while (true) {
		byte[] reply = new byte[4096];
		// while (true) {
		int bytesRead;
		try {
			// Thread.sleep(1);

			// System.out.println("Waiting for server message");

			while ((bytesRead = streamFromServer.read(reply)) != -1) {
				System.out.println("Message to client from " + IP);
				// System.out.println(new String(reply, 0, bytesRead));
				System.out.println("Server parse");

				byte[] temp = new byte[reply.length];
				for (int i = 0; i < reply.length; i++) {
					temp[i] = reply[i];
				}
				control.sendToClient(reply, bytesRead);

				 Packet response = control.parseMessage(temp);
				 System.out.println(response.getType());

				// switch (response.getType()) {
				//
				//// case ECHO_REQUEST:
				//// OFEchoReply echoReply =
				// OFFactories.getFactory(response.getMessage().getVersion()).buildEchoReply()
				//// .build();
				////
				//// ByteBuf buf = Unpooled.buffer(4096);
				//// echoReply.writeTo(buf);
				//// byte[] bytes = new byte[buf.readableBytes()];
				//// int readerIndex = buf.readerIndex();
				//// buf.getBytes(readerIndex, bytes);
				//// //send to server instead???
				//// // control.sendToClient(bytes, bytes.length);
				//// send(bytes, bytes.length);
				////
				//// break;
				// case ERROR:
				// System.out.println("Error message from controller");
				// break;
				// default:
				// control.handlePacket(response, reply, bytesRead);
				// break;
				// }
				//

				 Thread.sleep(1);
				// System.out.println("Done");
				// break;
			}
			// System.out.println("Read from server");
		} catch (IOException e) {
			System.out.println("Server IO Exception");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// }

}
