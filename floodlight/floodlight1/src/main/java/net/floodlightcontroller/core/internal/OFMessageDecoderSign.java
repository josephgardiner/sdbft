/**
 *    Copyright 2011, Big Switch Networks, Inc.
 *    Originally created by David Erickson, Stanford University
 *
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package net.floodlightcontroller.core.internal;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMessageReader;
import org.projectfloodlight.openflow.protocol.OFVersion;

/**
 * Decode an openflow message from a channel, for use in a netty pipeline.
 *
 * @author Andreas Wundsam <andreas.wundsam@bigswitch.com>
 */
public class OFMessageDecoderSign extends ByteToMessageDecoder {

	private OFMessageReader<OFMessage> reader;
	KeyPair keyPair;
	Signature signer; 
	ByteBuf buffer;
	int sigLength;
	public OFMessageDecoderSign(KeyPair keyPair, String signAlgo, int sigLength) {
		setReader();
		this.sigLength = sigLength;
		this.keyPair = keyPair;
		
		try {
			signer = Signature.getInstance(signAlgo);
			signer.initVerify(keyPair.getPublic());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		buffer = Unpooled.buffer();

	}

	public OFMessageDecoderSign(OFVersion version) {
		setVersion(version);
		setReader();
	}

	private void setReader() {
		reader = OFFactories.getGenericReader();
	}

	public void setVersion(OFVersion version) {
		OFFactory factory = OFFactories.getFactory(version);
		this.reader = factory.getReader();
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (!ctx.channel().isActive()){
			// In testing, I see decode being called AFTER decode last.
			// This check avoids that from reading corrupted frames
			return;
		}

		// Note(andiw): netty4 adds support for more efficient handling of lists messages in the
		// pipeline itself.
		// Instead of constructing a list of messages here, we could also just add the individual
		// messages to the "out" list provided by netty. This would require changing all the handlers
		// in the pipeline to accept "OFMessage" instead of "Iterable<OFMessage>". Probably
		// a good idea, but left for a future cleanup.

		OFMessage singleMessage = null;
		List<OFMessage> list = null;
		boolean first = true;
		int prev = 0;
		prev=in.readableBytes();
		
		ByteBuf messageBuf = Unpooled.copiedBuffer(buffer, in);
		//in = Unpooled.copiedBuffer(messageBuf);
		for (;;) {

			int readable = messageBuf.readableBytes();
			if(readable == 0){
	//			System.out.println("empty buffer");
				buffer = Unpooled.buffer();
				break;
			}
			if(readable > sigLength){
				
			ByteBuf sig = Unpooled.buffer();
			prev = in.readerIndex();
			byte[] sigBytes = new byte[sigLength];
			messageBuf.readBytes(sigBytes,0, sigLength);
			
			int length = messageBuf.getUnsignedShort(messageBuf.readerIndex() + 2);
			
			if (length > messageBuf.readableBytes() ||length==0) {
				messageBuf.readerIndex(messageBuf.readerIndex() - sigLength);
			buffer = Unpooled.copiedBuffer(messageBuf);

				break;
			}
			OFMessage message = reader.readFrom(messageBuf);
			ByteBuf mByteBuf = Unpooled.buffer();
			message.writeTo(mByteBuf);
			byte[] mBytes = new byte[mByteBuf.readableBytes()];
			int mReader = mByteBuf.readerIndex();
			mByteBuf.getBytes(mReader, mBytes);
			boolean verified;

			verified = verifySig(mBytes, sigBytes);
			//System.out.println("Verified: " + message.getType() + ": "+ verified);
			if(!verified){
				System.out.println(message.getType() + " Non Verified!!");
			}
			if (message == null) {
				break;
			}
			if (first) {
				// first message read
				singleMessage = message;
				first = false;
			} else {
				// more messages read, use the list
				if (list == null) {
					list = new ArrayList<>();
					list.add(singleMessage);
					singleMessage = null;
				}
				list.add(message);
			}
			}else{
				buffer = Unpooled.copiedBuffer(messageBuf);
				break;
			}
		}
		if (list != null) {
			out.add(list);
		} else if (singleMessage != null) {
			out.add(Collections.singletonList(singleMessage));
		}
	//	System.out.println(messageBuf.readerIndex());
		in.readerIndex(in.writerIndex());
	}
	
	public boolean verifySig(byte[] data, byte[] sig) throws Exception {
		//	KeyPair keyPair = generateKeyPair(999);

		
			signer.update(data);
			return (signer.verify(sig));
		}
}
