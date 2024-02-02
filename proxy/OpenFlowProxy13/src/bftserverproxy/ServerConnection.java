package bftserverproxy;

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
	int clientID;
	String IP;
	Socket sock;
	BFTServerControl control;
	InputStream streamFromServer;
	OutputStream streamToServer;
	Controller controller;
	ByteBuf buffer;
	OpenFlowPacketParser parser;
	byte[] response;
	boolean hasResponse;
	boolean setUpComplete;

	OFVersion ofVersion;

	public void close() {
		try {
			sock.close();
			streamToServer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ServerConnection(int clientID, String iP, Socket s, BFTServerControl control, Controller controller,
			OpenFlowPacketParser parser, OFVersion ofVersion) {
		this.clientID = clientID;
		this.IP = iP;
		this.sock = s;
		this.control = control;
		this.controller = controller;
		this.parser = parser;
		buffer = Unpooled.buffer();
		this.ofVersion = ofVersion;

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
			// long start = System.nanoTime();
			// System.out.println(bytesRead);
			streamToServer.write(data, 0, bytesRead);
			streamToServer.flush();

			// Thread.yield();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			control.handleControllerFault();
			e.printStackTrace();
		}

	}

	public byte[] getResponse() {
		if (hasResponse) {
			byte[] toReturn = response;
			hasResponse = false;
			return toReturn;
		} else {
			return null;
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// while (true) {
		byte[] reply = new byte[65535];
		// while (true) {
		int bytesRead;
		try {
			// Thread.sleep(1);

			// System.out.println("Waiting for server message");

			while ((bytesRead = streamFromServer.read(reply)) != -1) {
				// ByteBuf m = Unpooled.wrappedBuffer(reply);
				// long start = System.currentTimeMillis();
				ByteBuf m = Unpooled.copiedBuffer(reply, 0, bytesRead);

				List<Packet> packets = null;

				try {
					// System.out.println("Readable: " + m.readableBytes());

					ByteBuf messageBuf = Unpooled.copiedBuffer(buffer, m);
					m = Unpooled.copiedBuffer(messageBuf);
					packets = parser.parseMessagesLooping(m, 100);

					// m.setInt(4, 123456);
					// System.out.println("Controller " + controller.getId() + ":");
			//		 for (Packet p : packets) {
					// if (p != null) {
//					 System.out.println("Controller: " + p.getType() + " xid: " +
//					 p.getMessage().getXid() + " full: " + p.getMessage().toString());
					// }
				// }
					// System.out.println("Remaining bytes: "+m.readableBytes());
					buffer = Unpooled.copiedBuffer(m);

					// System.out.println("Controller: " + p.getType());
				} catch (NullPointerException ex) {
					System.out.println("Controller: NullPointer");
					buffer = Unpooled.copiedBuffer(m);
					// waiting = true;
				}
				// Must reset reader index after parsing otherwise message will not be
				// sent.
				m.resetReaderIndex();
				ByteBuf toWrite = Unpooled.buffer();
				ByteBuf ackBuf = Unpooled.buffer();

				if (packets != null) {
					for (Packet p : packets) {
						// getMessageData
						// p.getMessage().writeTo(toWrite);
			//			System.out.println("Controller: " + p.getType());
						// getAck
						if (p.getType() == OFType.HELLO) {
							ByteBuf helloBuf = Unpooled.buffer();
							org.projectfloodlight.openflow.protocol.OFHello.Builder helloBuilder = OFFactories
									.getFactory(ofVersion).buildHello();
							OFHello hello = helloBuilder.build();
							hello.writeTo(helloBuf);
							// ctx.channel().writeAndFlush((Object)helloBuf);
							byte[] bytes = new byte[helloBuf.readableBytes()];
							int readerIndex = helloBuf.readerIndex();
							helloBuf.getBytes(readerIndex, bytes);

							streamToServer.write(bytes);
							streamToServer.flush();

						} else if (p.getType() == OFType.ECHO_REQUEST) {
							ByteBuf echoBuf = Unpooled.buffer();
							org.projectfloodlight.openflow.protocol.OFEchoReply.Builder echoBuilder = OFFactories
									.getFactory(ofVersion).buildEchoReply();
							OFEchoReply echo = echoBuilder.build();
							echo.writeTo(echoBuf);
							byte[] bytes = new byte[echoBuf.readableBytes()];
							int readerIndex = echoBuf.readerIndex();
							echoBuf.getBytes(readerIndex, bytes);

							streamToServer.write(bytes);
							streamToServer.flush();

						}
					}
				}
				if (packets.size() > 0 && packets != null) {

					// ByteBuf replyBuf = Unpooled.buffer();
					// Builder replyBuilder =
					// OFFactories.getFactory(OFVersion.OF_13).errorMsgs().buildHelloFailedErrorMsg();
					// OFErrorCauseData data = new
					// OFErrorCauseData("ACK123".getBytes(),packets.get(0).getMessage().getVersion());
					// System.out.println(new String(data.getData()));
					// replyBuilder.setData(data);
					// replyBuilder.setCode(OFHelloFailedCode.EPERM);
					// OFErrorMsg ack = replyBuilder.build();
					// org.projectfloodlight.openflow.protocol.OFEchoReply.Builder b =
					// OFFactories.getFactory(OFVersion.OF_13).buildEchoReply();
					// b.setData("ACK123".getBytes());
					// OFEchoReply ack = b.build();

					control.processResponseList( clientID, packets, controller.getId());
				}
				//
				// Thread.sleep(1);
				// long end = System.currentTimeMillis();
				// System.out.println("Server process time: " + (end-start));
				Thread.yield();
				// Thread.sleep(1);
			}
			// System.out.println("Read from server");
		} catch (IOException e) {
			System.out.println("Server IO Exception");
		}
	}

	// }

}
