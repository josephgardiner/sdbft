package simpleproxy;

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
	ControlSimple control;
	InputStream streamFromClient;
	OutputStream streamToClient;
	byte[] temp;
	boolean waiting;
	ByteBuf buffer;
	OpenFlowPacketParser parser;

	public ClientConnection(Socket s, ControlSimple control, OpenFlowPacketParser parser) {
		this.sock = s;
		this.control = control;
		this.parser = parser;
		buffer = Unpooled.buffer();

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
			streamToClient.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void send(byte[] data, int bytesRead) {
		try {
		//	System.out.println("Sending to client");
			// System.out.println(data.length);
			// System.out.println(bytesRead);
			streamToClient.write(data, 0, bytesRead);
			streamToClient.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// while (true) {
		byte[] request = new byte[65534];
		// while (true) {
		int bytesRead;
		try {
			// Thread.sleep(1);

			// System.out.println("Waiting for client message");

			while ((bytesRead = streamFromClient.read(request)) != -1) {
		//		if(bytesRead>8) {
		//		System.out.println("Bytes read from switch: " + bytesRead);
				
	//			System.out.println("--Switch Start--");
//				long start = System.currentTimeMillis();
				ByteBuf m = Unpooled.copiedBuffer(request, 0, bytesRead);
				m.order(ByteOrder.LITTLE_ENDIAN);
//			//	ByteBuf m=Unpooled.copiedBuffer(request, 0, bytesRead);
				byte[] bytes = new byte[m.readableBytes()];
//
				int readerIndex = m.readerIndex();
				m.getBytes(readerIndex, bytes);
//				long end = System.currentTimeMillis();
//				System.out.println("Time for buffer: " + (end-start));
				//ByteBuf toWrite = Unpooled.buffer();
			//	List<Packet> packets = null;
			    
	//			control.sendToAllServers(request, bytesRead);
				control.sendToAllServers(bytes, bytes.length);

				//Thread.sleep(1);
				// Thread.currentThread().sleep(1);
				// break;
				request = new byte[65534];
//				}else{
//					ByteBuf echoBuf = Unpooled.buffer();
//					org.projectfloodlight.openflow.protocol.OFEchoReply.Builder echoBuilder = OFFactories
//							.getFactory(OFVersion.OF_10).buildEchoReply();
//					OFEchoReply echo = echoBuilder.build();
//					echo.writeTo(echoBuf);
//					byte[] echobytes = new byte[echoBuf.readableBytes()];
//					int echoReaderIndex = echoBuf.readerIndex();
//					echoBuf.getBytes(echoReaderIndex, echobytes);
//					
//					streamToClient.write(echobytes);
//					streamToClient.flush();
//				}
					Thread.yield();
			}
			// System.out.println("done");
			// sock.close();
			// streamFromClient.close();
			// streamToClient.close();
		} catch (IOException e) {
			System.out.println("Client IO Exception");
			control.closeServerConnections();
		} 

		// }
		// try {
		// Thread.sleep(5)
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

	}

}
