package bftsmart.demo.counter;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;

public class CounterReplyListener implements ReplyListener{
	AsynchServiceProxy serviceProxy;
	int replies = 0;
	boolean verbose = true;
	ArrayList<TOMMessage> replyList;
	
	public CounterReplyListener(AsynchServiceProxy serviceProxy) {
		super();
		this.serviceProxy = serviceProxy;
		this.replyList = new ArrayList<TOMMessage>();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
        if (verbose) System.out.println("[RequestContext] The proxy is re-issuing the request to the replicas");
        replies = 0;
	}

	@Override
	public void replyReceived(RequestContext context, TOMMessage reply) {
		int newValue = 0;
		 try {
			 newValue = new DataInputStream(new ByteArrayInputStream(reply.getContent())).readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
}
            StringBuilder builder = new StringBuilder();
            builder.append("[RequestContext] id: " + context.getReqId() + " type: " + context.getRequestType());
            builder.append("[TOMMessage reply] sender id: " + reply.getSender() + "  Content: " + newValue);
            if (verbose) System.out.println(builder.toString());
            int sameContent = 1;
            for (TOMMessage tomMessage : replyList) {
            	if(Arrays.equals(tomMessage.getContent(),reply.getContent())) {
            		sameContent++;
            	}else {
            		System.out.println("Disagreement!!");
            	}
			} 
            
            replyList.add(reply);
            System.out.println("Current responses: " + replyList.size() + " Aggremment: " + sameContent);
            double q = Math.ceil((double) (serviceProxy.getViewManager().getCurrentViewN() + serviceProxy.getViewManager().getCurrentViewF() + 1) / 2.0);
           // double q = 1;
            if (sameContent >= q) {
            	System.out.println("I declare agreement has been reached!");
                if (verbose) System.out.println("[RequestContext] clean request context id: " + context.getReqId());
                serviceProxy.cleanAsynchRequest(context.getOperationId());
            }
        }		
	}


