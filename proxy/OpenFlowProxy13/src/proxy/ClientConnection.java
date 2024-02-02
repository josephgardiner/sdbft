package proxy;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.projectfloodlight.openflow.protocol.OFEchoReply;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ClientConnection implements Runnable {
	Socket sock;
	Control control;
	InputStream streamFromClient;
	OutputStream streamToClient;
	byte[] temp;
	boolean waiting;

	public ClientConnection(Socket s, Control control) {
		this.sock = s;
		this.control = control;
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
			System.out.println("Sending to client");
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
		byte[] request = new byte[1024];
//		while (true) {
			int bytesRead;
			try {
				// Thread.sleep(1);

				// System.out.println("Waiting for client message");

				while ((bytesRead = streamFromClient.read(request)) != -1) {

					System.out.println("Message to server");
					// System.out.println(new String(reply, 0, bytesRead));
					// control.sendHelloMessage();
					System.out.println("Client parse:");
					control.sendToAllServers(request, bytesRead);
//					try {
//						Packet message = control.parseMessage(request.clone());
//
//						switch (message.getType()) {
//						case PACKET_IN:
//							control.sendToAllServers(request, bytesRead);
//							control.handlePacketIn(message);
//							break;
////						case ECHO_REQUEST:
////							OFEchoReply reply = OFFactories.getFactory(message.getMessage().getVersion())
////									.buildEchoReply().build();
////							OFFactories.getFactory(message.getMessage().getVersion()).
////							ByteBuf buf = Unpooled.buffer(4096);
////							reply.writeTo(buf);
////							byte[] bytes = new byte[buf.readableBytes()];
////							int readerIndex = buf.readerIndex();
////							buf.getBytes(readerIndex, bytes);
////
////							control.sendToClient(bytes, bytes.length);
////
////							break;
////						case PORT_STATUS:
////							temp = request;
////							waiting = true;
////							break;
////						case STATS_REPLY:
////							temp = request;
////							waiting = true;
////							break;
////						case ERROR:
////							System.out.println("Error message from switch");
////							break;
//						default:
//							if (waiting) {
//								control.sendToAllServers(temp, temp.length);
//								waiting = false;
//							}
//
//							control.sendToAllServers(request, bytesRead);
//							break;
//						}
//					} catch (NullPointerException n) {
//						System.out.println("NULL POINTER");
//						if (waiting) {
//							byte[] t = new byte[temp.length + request.length];
//
//							for (int i = 0; i < t.length; ++i) {
//								t[i] = i < temp.length ? temp[i] : request[i - temp.length];
//							}
//							Packet message = control.parseMessage(t);
//							System.out.println("Found multipart: " + message.getType());
//							control.sendToAllServers(t, t.length);
//							waiting = false;
//						}else{
//						control.sendToAllServers(request, bytesRead);
//
//						}
//					}
					Thread.sleep(1);
//					Thread.currentThread().sleep(1);
				//	break;

				}
				// System.out.println("done");
				// sock.close();
				// streamFromClient.close();
				// streamToClient.close();
			} catch (IOException e) {
				 System.out.println("Client IO Exception");
				 control.closeServerConnections();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// }
			// try {
			// Thread.sleep(5);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
//		}

	}

}
