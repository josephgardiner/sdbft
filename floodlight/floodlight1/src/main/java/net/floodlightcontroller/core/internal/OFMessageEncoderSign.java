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
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;

import org.projectfloodlight.openflow.protocol.OFMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Encode an iterable of openflow messages for output into a ByteBuf, for use in
 * a netty pipeline
 *
 * @author Andreas Wundsam <andreas.wundsam@bigswitch.com>
 */
public class OFMessageEncoderSign extends MessageToByteEncoder<Iterable<OFMessage>> {

	KeyPair keyPair;
	Signature signer; 
	
	
	public OFMessageEncoderSign(KeyPair keyPair, String signAlgo, int sigLength) {
		super();
		this.keyPair = keyPair;
		try {
			signer = Signature.getInstance(signAlgo);
		signer.initSign(keyPair.getPrivate());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Iterable<OFMessage> msgList, ByteBuf out) throws Exception {
		for (OFMessage ofm : msgList) {
			
			ByteBuf temp = Unpooled.buffer();
			ofm.writeTo(temp);
			byte[] bytes = new byte[temp.readableBytes()];
			int readerIndex = temp.readerIndex();
			temp.getBytes(readerIndex, bytes);
			byte[] sig = signData(bytes,keyPair, signer);
			
		out.writeBytes(sig);
			ofm.writeTo(out);

		}
	}

	public static byte[] signData(byte[] data, KeyPair keyPair, Signature signer) throws Exception {
	  //  KeyPair keyPair = generateKeyPair(999);

		
		signer.update(data);
		return (signer.sign());
	}
	
//	  public static KeyPair generateKeyPair(long seed) throws Exception {
//		    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
//		    SecureRandom rng = SecureRandom.getInstance("SHA1PRNG", "SUN");
//		    rng.setSeed(seed);
//		    keyGenerator.initialize(1024, rng);
//
//		    return (keyGenerator.generateKeyPair());
//		  }

}
