package bftclientproxy;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import org.projectfloodlight.openflow.protocol.OFEchoReply;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFHello;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFVersion;

import bftsmart.demo.counter.CounterReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.core.messages.TOMMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ClientConnection implements Runnable {
	Socket sock;
	BFTClientControl control;
	InputStream streamFromClient;
	OutputStream streamToClient;
	byte[] temp;
	boolean waiting;
	ByteBuf buffer;
	OpenFlowPacketParser parser;
	OFVersion ofVersion;

	AsynchServiceProxy switchProxy;

	public ClientConnection(Socket s, BFTClientControl control, OpenFlowPacketParser parser, OFVersion version,
			AsynchServiceProxy switchProxy) {
		this.sock = s;
		this.control = control;
		this.parser = parser;
		buffer = Unpooled.buffer();
		this.ofVersion = version;

		try {
			streamFromClient = sock.getInputStream();

			streamToClient = new BufferedOutputStream(sock.getOutputStream());
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.switchProxy = switchProxy;

	}

	public void close() {
		try {
			sock.close();
			streamToClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void send(byte[] data, int bytesRead) {
		try {
			//

	//		System.out.println("Sending to client: " + bytesRead);
			// System.out.println(data.length);
			// System.out.println(bytesRead);

			streamToClient.write(data, 0, bytesRead);
			streamToClient.flush();
			Thread.yield();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// while (true) {
		byte[] request = new byte[65535];
		// while (true) {
		int bytesRead;
		try {
			// Thread.sleep(1);

			// System.out.println("Waiting for client message");

			while ((bytesRead = streamFromClient.read(request)) != -1) {
				// System.out.println("--Switch Start--");
				// ByteBuf m = Unpooled.copiedBuffer(request);
				// long start = System.currentTimeMillis();
				ByteBuf m = Unpooled.copiedBuffer(request, 0, bytesRead);

				ByteBuf toWrite = Unpooled.buffer();
				ByteBuf bftWrite = Unpooled.buffer();
				List<Packet> packets = null;
				try {

					if (buffer.readableBytes() > 0) {
		//				System.out.println("BUFFER SIZE: " + buffer.readableBytes());
						ByteBuf messageBuf = Unpooled.copiedBuffer(buffer, m);
						m = Unpooled.copiedBuffer(messageBuf);
					}
					packets = parser.parseMessagesLooping(m, 10000);

					for (Packet p : packets) {
						System.out.println("Client Proxy From Switch: " + p.getType());
						switch (p.getType()) {
						case PACKET_IN:
							/*
							 * On receiving a packetin, fetches the next xid from the Proxy object, inserts
							 * it into the packet and sends Creates a new switchEvent for the packet.
							 */
							
							int nextXID = control.getNextXid();
		//					System.out.println("Switch: " + p.getType() + " XiD: " + nextXID);

							ByteBuf temp = Unpooled.buffer();
							// System.out.println("Set xid: " + nextXID);
							p.getMessage().writeTo(temp);
							temp.setInt(4, nextXID);
							// control.addSwitchEvent(nextXID, new SwitchEvent(nextXID, (OFPacketIn)
							// p.getMessage()));
							OFPacketIn packet = (OFPacketIn) p.getMessage();
				//			System.out.println("Packet length: " + packet.getTotalLen());
							if(packet.getTotalLen()==60 ||packet.getTotalLen()==75||packet.getTotalLen()==83) {
								System.out.println("diverting");
								toWrite.writeBytes(temp);
							}else {
						//	bftWrite.writeBytes(temp);
								byte[] bytes = new byte[temp.readableBytes()];

								int readerIndex = temp.readerIndex();
								temp.getBytes(readerIndex, bytes);
								switchProxy.invokeAsynchRequest(bytes, new BFTReplyListener(switchProxy, control, parser),
									TOMMessageType.ORDERED_REQUEST);
							
							}// counterProxy.invokeAsynchRequest(out.toByteArray(), new
							// CounterReplyListener(counterProxy),TOMMessageType.UNORDERED_REQUEST);

							break;
						case STATS_REPLY:
							control.processResponse(p);
							break;
						case ROLE_REPLY:
							control.processResponse(p);
							break;
						case FEATURES_REPLY:
							control.processResponse(p);
							break;
						case GET_CONFIG_REPLY:
							control.processResponse(p);
							break;
						case BARRIER_REPLY:
							control.processResponse(p);
							break;
						case HELLO:
							org.projectfloodlight.openflow.protocol.OFHello.Builder helloBuilder = OFFactories
									.getFactory(ofVersion).buildHello();
							helloBuilder.setXid(p.getMessage().getXid());
							OFHello hello = helloBuilder.build();
							
							ByteBuf helloBuf = Unpooled.buffer();
							hello.writeTo(helloBuf);
							byte[] bytes = new byte[helloBuf.readableBytes()];
							int readerIndex = helloBuf.readerIndex();
							helloBuf.getBytes(readerIndex, bytes);
							
							streamToClient.write(bytes);
							streamToClient.flush();
//							if(!control.serverConnections) {
//								control.setUpServerConnections();
//							}
							break;
						case ECHO_REQUEST:
							System.out.println("Got echo request");
							ByteBuf echoBuf = Unpooled.buffer();
							org.projectfloodlight.openflow.protocol.OFEchoReply.Builder echoBuilder = OFFactories
									.getFactory(ofVersion).buildEchoReply();
							echoBuilder.setXid(p.getMessage().getXid());
							OFEchoReply echo = echoBuilder.build();
							echo.writeTo(echoBuf);
							byte[] bytes1 = new byte[echoBuf.readableBytes()];
							int readerIndex1 = echoBuf.readerIndex();
							echoBuf.getBytes(readerIndex1, bytes1);

							streamToClient.write(bytes1);
							streamToClient.flush();
							break;
						case ERROR:
							control.processResponse(p);
							break;
						default:

							p.getMessage().writeTo(toWrite);
							break;
						}
						// System.out.println("Switch: " + p.getType() + " xid: " +
						// p.getMessage().getXid() + " full: "
						// + p.getMessage().toString());

					}
					// System.out.println("Controller: " + p.getType());
					// System.out.println(ByteBufUtil.prettyHexDump(m));
					// System.out.println("Remianing bytes:
					// "+m.readableBytes());
					buffer = Unpooled.copiedBuffer(m);
				} catch (NullPointerException ex) {
					System.out.println("Switch: NullPointer");
					ex.printStackTrace();
					// System.out.println(ByteBufUtil.prettyHexDump(m));
					// need to wait for remaider of packet (likely multipart).
					// buffer = Unpooled.copiedBuffer(m);
					// waiting = true;
				}
				m.resetReaderIndex();
				// System.out.println("--Switch End--");
				// byte[] bytes = new byte[toWrite.readableBytes()];
				if (toWrite.readableBytes() > 0) {
					byte[] bytes = new byte[toWrite.readableBytes()];

					int readerIndex = toWrite.readerIndex();
					toWrite.getBytes(readerIndex, bytes);

					control.sendToAllServers(bytes, bytes.length);
				}

				if (bftWrite.readableBytes() > 0) {
					byte[] bytes = new byte[bftWrite.readableBytes()];

					int readerIndex = bftWrite.readerIndex();
					bftWrite.getBytes(readerIndex, bytes);
		//			System.out.println("BFTClient Sending " + bytes.length + " bytes");
					// control.sendToAllServers(bytes, bytes.length);
					switchProxy.invokeAsynchRequest(bytes, new BFTReplyListener(switchProxy, control, parser),
							TOMMessageType.ORDERED_REQUEST);
					
					// switchProxy.invokeAsynchRequest("tester".getBytes(), new
					// BFTReplyListener(switchProxy, control,parser),
					// TOMMessageType.ORDERED_REQUEST);

				}
				// Thread.sleep(1);
				// long end = System.currentTimeMillis();
				// System.out.println("Client process time: " + (end-start));
				Thread.yield();
				// Thread.currentThread().sleep(1);
				// break;
			}
			// System.out.println("done");
			// sock.close();
			// streamFromClient.close();
			// streamToClient.close();
		} catch (IOException e) {
			System.out.println("Client IO Exception");
			control.closeServerConnections();
		}
		// catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// }
		// try {
		// Thread.sleep(5);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

	}

	public void setSocket(Socket s) {
		this.sock = s;
		try {
			streamFromClient = this.sock.getInputStream();
			streamToClient = new BufferedOutputStream(this.sock.getOutputStream());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
