package bftserverproxy;

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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ClientConnection implements Runnable {
	int id;
	Socket sock;
	BFTServerControl control;
	InputStream streamFromClient;
	OutputStream streamToClient;
	byte[] temp;
	boolean waiting;
	ByteBuf buffer;
	OpenFlowPacketParser parser;

	public ClientConnection(int id, Socket s, BFTServerControl control) {
		this.id = id;
		this.sock = s;
		this.control = control;
	//	this.parser = parser;
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
//				System.out.println("Got data from client " + id + " on server ");
			//	ByteBuf m = Unpooled.copiedBuffer(request);
	//			ByteBuf m=Unpooled.copiedBuffer(request, 0, bytesRead);
				

				//ByteBuf toWrite = Unpooled.buffer(); 
			//	List<Packet> packets = null;
			    
	//			control.sendToAllServers(request, bytesRead);
				control.sendToServer(id, request, bytesRead);
				//Thread.sleep(1);
				// Thread.currentThread().sleep(1);
				// break;
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
		// Thread.sleep(5);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

	}
	
	public void setClientID(int clientID) {
		this.id = clientID;
	}

}
