package bftserverproxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import bftsmart.demo.counter.CounterServer;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Implement BFT server class here
 * 
 * @author joe
 *
 */
public class BFTClientConnection extends DefaultSingleRecoverable {

	Socket serverConnection;
	BFTServerControl control;
	OpenFlowPacketParser parser;
	int port;

	public BFTClientConnection(int switchID, int bftID, BFTServerControl control, OpenFlowPacketParser parser, int port) {
		new ServiceReplica(bftID, this, this);
		this.parser = parser;
		this.control = control;
		this.port = port;

		// this.replicaContext.getSVController().getRemoteAddress(9999).toString();

	}

	@Override
	public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
		int sender = msgCtx.getSender();
		if (command == null) {
			System.out.println("Null message");
		} else {
		//	 System.out.println("Message length: " + command.length);
		}
		long start = System.currentTimeMillis();
		List<Packet> packets = parser.parseMessagesLooping(Unpooled.buffer().writeBytes(command), 10000);
		int xID = -1;
		for (Packet p : packets) {
			xID = (int) p.getMessage().getXid();
		}
		control.createBuffer(sender,xID);
		control.sendToServer(sender,command, command.length);
		control.floodOutput.get(sender).put(xID, Boolean.FALSE);

		ByteBuf response;
		// try {
		// Thread.sleep(200);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		while (!(parser.parseMessagesLooping(control.checkForResponse(msgCtx.getSender(),xID), 1000).size() > 1) && !control.floodOutput.get(sender).get(xID)) {
			control.checkForResponse(sender,xID).resetReaderIndex();
			// while((response = control.checkForResponse(xID)).readableBytes()==0 ) {

			if(System.currentTimeMillis()-start>1000) {
				System.out.println("Response timeout - breaking");
				//break;
				// System.out.println(port + ": Got response for " + xID);
				
				byte[] responseArray = new byte[0];
				
				// System.out.println(port + ": clearing fields");
				control.clearResponse(xID);
				control.setFloodOutputFalse(sender, xID);
				control.setwrittenBFTFalse();

				// control.createBuffer(xID);
				return responseArray;
			
			}
			Thread.yield();
		

		}
		response = control.checkForResponse(sender,xID);
		
		// packets = parser.parseMessagesLooping(response, 1000);
		// System.out.println(port + ": Have " + packets.size() + " packets");
		// if(packets.size()<2 && !control.floodOutput) {
		// control.setwrittenBFTFalse();
		// // while(!control.writtenBFTData) {
		// while(!(parser.parseMessagesLooping(control.checkForResponse(xID),
		// 1000).size()>1)) {
		// Thread.yield();
		//
		// }
		// System.out.println(port + " got second packet");
		// response = control.checkForResponse(xID);
		// }
		//

		// System.out.println(port + ": Got response for " + xID);
		response.resetReaderIndex();
		 System.out.println(port + ": " + response.readableBytes());

		byte[] responseArray = new byte[response.readableBytes()];
		response.getBytes(0, responseArray);
		// System.out.println(port + ": clearing fields");
		control.clearResponse(xID);
		control.setFloodOutputFalse(sender, xID);
		control.setwrittenBFTFalse();

		// control.createBuffer(xID);
		return responseArray;
	}

	
	
	@SuppressWarnings("unchecked")
	@Override
	public void installSnapshot(byte[] state) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(state);
			ObjectInput in = new ObjectInputStream(bis);
			// counter = in.readInt();
			in.close();
			bis.close();
		} catch (IOException e) {
			System.err.println("[ERROR] Error deserializing state: " + e.getMessage());
		}
	}

	@Override
	public byte[] getSnapshot() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeInt(0);
			out.flush();
			bos.flush();
			out.close();
			bos.close();
			return bos.toByteArray();
		} catch (IOException ioe) {
			System.err.println("[ERROR] Error serializing state: " + ioe.getMessage());
			return "ERROR".getBytes();
		}
	}

	@Override
	public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
		int sender = msgCtx.getSender();
		
		if (command == null) {
			System.out.println("Null message");
		} else {
			// System.out.println("Message length: " + command.length);
		}
		List<Packet> packets = parser.parseMessagesLooping(Unpooled.buffer().writeBytes(command), 1000);
		int xID = -1;
		for (Packet p : packets) {
			xID = (int) p.getMessage().getXid();
		}
		control.createBuffer(sender,xID);
		control.sendToServer(sender,command, command.length);
		control.floodOutput.get(sender).put(xID, Boolean.FALSE);

		ByteBuf response;
		// try {
		// Thread.sleep(200);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		while (!(parser.parseMessagesLooping(control.checkForResponse(msgCtx.getSender(),xID), 1000).size() > 1) && !control.floodOutput.get(sender).get(xID)) {
			control.checkForResponse(sender,xID).resetReaderIndex();
			// while((response = control.checkForResponse(xID)).readableBytes()==0 ) {
			Thread.yield();

		}
	//	System.out.println("Server proxy has response");
		response = control.checkForResponse(sender,xID);
		// System.out.println(port + ": " + response.readableBytes());
		// packets = parser.parseMessagesLooping(response, 1000);
		// System.out.println(port + ": Have " + packets.size() + " packets");
		// if(packets.size()<2 && !control.floodOutput) {
		// control.setwrittenBFTFalse();
		// // while(!control.writtenBFTData) {
		// while(!(parser.parseMessagesLooping(control.checkForResponse(xID),
		// 1000).size()>1)) {
		// Thread.yield();
		//
		// }
		// System.out.println(port + " got second packet");
		// response = control.checkForResponse(xID);
		// }
		//

		// System.out.println(port + ": Got response for " + xID);
		response.resetReaderIndex();
		byte[] responseArray = new byte[response.readableBytes()];
		response.getBytes(0, responseArray);
		// System.out.println(port + ": clearing fields");
		control.clearResponse(xID);
		control.setFloodOutputFalse(sender, xID);
		control.setwrittenBFTFalse();

		// control.createBuffer(xID);
		return responseArray;
	}

}
