package bftclientproxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class BFTReplyListener implements ReplyListener {

	ArrayList<TOMMessage> responses;
	BFTClientControl control;
	AsynchServiceProxy serviceProxy;
	double q;
	OpenFlowPacketParser parser;

	public BFTReplyListener(AsynchServiceProxy serviceProxy, BFTClientControl control, OpenFlowPacketParser parser) {
		this.serviceProxy = serviceProxy;
		this.control = control;
		q = Math.ceil((double) (serviceProxy.getViewManager().getCurrentViewN()
				+ serviceProxy.getViewManager().getCurrentViewF() + 1) / 2.0);
		responses = new ArrayList<TOMMessage>();
		this.parser = parser;

	}

	@Override
	public void reset() {
		System.out.println("[RequestContext] The proxy is re-issuing the request to the replicas");
		responses.clear();
	}

	@Override
	public void replyReceived(RequestContext context, TOMMessage reply) {
		// TODO Auto-generated method stub

		// byte[] message = reply.getContent();
	//	 System.out.println("Got a response from " + reply.getSender() + " of length "
//		 + reply.getContent().length);
		int sameContent = 1;
		for (TOMMessage tomMessage : responses) {

			if (reply.getContent() == null) {
				System.out.println("null content");
			}
			if (Arrays.equals(tomMessage.getContent(), reply.getContent())) {
				sameContent++;
				// for (TOMMessage m : responses) {
				// System.out.println("old message");
				// List<Packet> parsed =
				// parser.parseMessagesLooping(Unpooled.copiedBuffer(m.getContent()), 1000000);
				// for(Packet p : parsed){
				// System.out.println(m.getSender() + ": " + p.getMessage().toString());
				// }
				// }
				// System.out.println("new message");
				// List<Packet> parsed =
				// parser.parseMessagesLooping(Unpooled.copiedBuffer(reply.getContent()),
				// 1000000);
				// for(Packet p : parsed){
				// System.out.println(reply.getSender() + ": " + p.getMessage().toString());
				// }
			} else {
				// sameContent++;
				// System.out.println("Disagreement!! Node responsible: " + reply.getSender());
				// for (TOMMessage m : responses) {
				// System.out.println("old message");
				// List<Packet> parsed =
				// parser.parseMessagesLooping(Unpooled.copiedBuffer(m.getContent()), 1000000);
				// for(Packet p : parsed){
				// System.out.println(m.getSender() + ": " + p.getMessage().toString());
				// }
				// }
				// System.out.println("new message");
				// List<Packet> parsed =
				// parser.parseMessagesLooping(Unpooled.copiedBuffer(reply.getContent()),
				// 1000000);
				// for(Packet p : parsed){
				// System.out.println(reply.getSender() + ": " + p.getMessage().toString());
				// }
			}
		}

		responses.add(reply);

		if (sameContent >= q) {
			System.out.println("Got consensus! Q:" + q);
			control.sendToClient(reply.getContent(), reply.getContent().length);
			serviceProxy.cleanAsynchRequest(context.getOperationId());
		}
	}

}
