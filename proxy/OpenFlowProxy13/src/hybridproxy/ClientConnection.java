package hybridproxy;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteOrder;
import java.util.List;

import org.projectfloodlight.openflow.protocol.OFEchoReply;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFHello;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFVersion;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ClientConnection implements Runnable {
	Socket sock;
	Control control;
	InputStream streamFromClient;
	OutputStream streamToClient;
	byte[] temp;
	boolean waiting;
	ByteBuf buffer;
	OpenFlowPacketParser parser;
	OFVersion ofVersion;

	public ClientConnection(Socket s, Control control, OpenFlowPacketParser parser, OFVersion version) {
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
	//		System.out.println("Sending to client");
			// System.out.println(data.length);
			// System.out.println(bytesRead);
			
			streamToClient.write(data, 0, bytesRead);
			streamToClient.flush();
			Thread.yield();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Client senc exception: " + this.control.localPort);
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
	//			System.out.println("--Switch Start--");
			//	ByteBuf m = Unpooled.copiedBuffer(request);
		//		long start = System.currentTimeMillis();
				ByteBuf m=Unpooled.copiedBuffer(request, 0, bytesRead);
				

				ByteBuf toWrite = Unpooled.buffer();
				List<Packet> packets = null;
				try {

					if(buffer.readableBytes()>0){
					ByteBuf messageBuf = Unpooled.copiedBuffer(buffer, m);
					m = Unpooled.copiedBuffer(messageBuf);
					}
					if(buffer.readableBytes()>50000){
					System.out.println("Buffer overflow: " + buffer.readableBytes());
					}
		//			m.order(ByteOrder.BIG_ENDIAN);
					packets = parser.parseMessagesLooping(m, 10000);

					for (Packet p : packets) {

						switch (p.getType()) {
						case PACKET_IN:
							/*
							 * On receiving a packetin, fetches the next xid
							 * from the Proxy object, inserts it into the packet
							 * and sends Creates a new switchEvent for the
							 * packet.
							 */
//							OFPacketIn packet = (OFPacketIn) p.getMessage();
//							if(packet.getTotalLen()==60) {
//								break;
//							}
						//	System.out.println(p.getMessage());
				//			System.out.println("Packet In: " + p.getMessage().getXid());
							int nextXID = control.getNextXid();
							ByteBuf temp = Unpooled.buffer();
	//						System.out.println("Set xid: " + nextXID);
							p.getMessage().writeTo(temp);

			    			temp.setInt(4, nextXID);
			    			
			    //			System.out.println("Switch packet in xid: " + nextXID);
			    		//	System.out.println("Handshakes done: " + control.handshakesDone());
			    		//	if(control.handshakesDone()) {
							control.addSwitchEvent(nextXID, new SwitchEvent(nextXID, (OFPacketIn) p.getMessage(),temp));
							
							toWrite.writeBytes(temp);
						
			    	//		}
							break;
						case STATS_REPLY:
							control.processResponse(p);
							break;
						case ROLE_REPLY:
							control.processResponse(p);
							break;
						case FEATURES_REPLY:
							System.out.println("got features reply");
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
							OFHello hello = helloBuilder.build();
							ByteBuf helloBuf = Unpooled.buffer();
							hello.writeTo(helloBuf);
							byte[] bytes = new byte[helloBuf.readableBytes()];
							int readerIndex = helloBuf.readerIndex();
							helloBuf.getBytes(readerIndex, bytes);
							streamToClient.write(bytes);
							streamToClient.flush();
							break;
						case ERROR:
							control.processResponse(p);
							break;
						case ECHO_REQUEST:
							ByteBuf echoBuf = Unpooled.buffer();
							
							org.projectfloodlight.openflow.protocol.OFEchoReply.Builder echoBuilder = OFFactories
									.getFactory(ofVersion).buildEchoReply();
							OFEchoReply echo = echoBuilder.build();
							echo.writeTo(echoBuf);
							byte[] echobytes = new byte[echoBuf.readableBytes()];
							int echoReaderIndex = echoBuf.readerIndex();
							echoBuf.getBytes(echoReaderIndex, echobytes);
							
							streamToClient.write(echobytes);
							streamToClient.flush();
							break;
						case PORT_STATUS:
							System.out.println("Port status received");
							p.getMessage().writeTo(toWrite);
							break;
						default:
							System.out.println("Direct:" + p.getType());
						
							p.getMessage().writeTo(toWrite);
							break;
						}
//						System.out.println("Switch: " + p.getType() + " xid: " + p.getMessage().getXid() + " full: "
//								+ p.getMessage().toString());

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
		//		System.out.println("--Switch End--");
		//		byte[] bytes = new byte[toWrite.readableBytes()];
				byte[] bytes = new byte[toWrite.readableBytes()];

				int readerIndex = toWrite.readerIndex();
				toWrite.getBytes(readerIndex, bytes);
				
				control.sendToAllServers(bytes, bytes.length);
		//		Thread.sleep(1);
//				long end = System.currentTimeMillis();
//				System.out.println("Client process time: " + (end-start));
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
			e.printStackTrace();
			control.closeServerConnections();
		} 
//		catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// }
		// try {
		// Thread.sleep(5);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

	}

}
