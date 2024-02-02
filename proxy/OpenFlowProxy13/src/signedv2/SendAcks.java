package signedv2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.projectfloodlight.openflow.protocol.OFMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SendAcks implements Runnable {

	Control control;
	Queue<byte[]> toSend;

	public SendAcks(Control control) {
		super();
		this.control = control;
		toSend = new LinkedList<byte[]>();
	}

	public void addMessage(byte[] message) {
		toSend.add(message);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			ByteBuf output = Unpooled.buffer();
			boolean send = false;
			while (toSend.size() > 0) {
				send = true;
				byte[] message = toSend.remove();
				output.writeBytes(message);
			}
			if(send){
			byte[] write = new byte[output.readableBytes()];
			output.getBytes(0, write);
		
			control.sendToAllServers(write, write.length);
			}
		//	toSend.clear();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
