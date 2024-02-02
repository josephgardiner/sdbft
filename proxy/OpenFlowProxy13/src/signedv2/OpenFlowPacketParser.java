package signedv2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMessageReader;
import org.projectfloodlight.openflow.protocol.OFPacketOut;

public class OpenFlowPacketParser {
  KeyPair keyPair;
  
  Signature signer;
  
  OFMessageReader<OFMessage> reader;
  
  int sigLength;
  
  int keyLength;
  
  Signature sign;
  
  public OpenFlowPacketParser(int sigLength, int keyLength, String sigAlgo) {
    try {
      this.sigLength = sigLength;
      this.reader = OFFactories.getGenericReader();
      this.keyPair = generateKeyPair(999L, keyLength);
      this.signer = Signature.getInstance(sigAlgo);
      this.signer.initVerify(this.keyPair.getPublic());
      this.sign = Signature.getInstance(sigAlgo);
      this.sign.initSign(this.keyPair.getPrivate());
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  public List<Packet> parseMessagesLooping(ByteBuf data, int limit) {
    List<Packet> results = new ArrayList<>();
    byte[] bytes = new byte[data.readableBytes()];
    int readerIndex = data.readerIndex();
    data.getBytes(readerIndex, bytes);
    OFMessage m = null;
    int prev = 0;
    try {
      int count = 0;
      while (data.readableBytes() > 0 && count < limit) {
        ByteBuf sig = Unpooled.buffer();
        prev = data.readerIndex();
        if (data.getShort(data.readerIndex() + 2) > data.readableBytes() || data.getShort(data.readerIndex() + 2) == 0)
          break; 
        m = (OFMessage)OFFactories.getGenericReader().readFrom(data);
        if (m != null) {
          ByteBuf mData = Unpooled.buffer();
          if (m == null)
            System.out.println("Message is null"); 
          Packet p = new Packet(m.getType(), m);
          results.add(p);
        } 
        count++;
      } 
    } catch (OFParseError e) {
      System.out.println("Parse error");
      e.printStackTrace();
    } catch (IllegalArgumentException ex) {
      System.out.println("Illegal argument!");
      data.readerIndex(prev);
    } catch (NullPointerException ex) {
      System.out.println("parser threw null pointer!!!!");
      ex.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return results;
  }
  
  public List<Packet> parseMessagesLoopingSigned(ByteBuf data, int limit, String ip) {
    List<Packet> results = new ArrayList<>();
    byte[] bytes = new byte[data.readableBytes()];
    int readerIndex = data.readerIndex();
    data.getBytes(readerIndex, bytes);
    OFMessage m = null;
    int prev = 0;
    try {
      int count = 0;
      while (data.readableBytes() > 0 && count < limit && data.readableBytes() > this.sigLength) {
        prev = data.readerIndex();
        byte[] sigBytes = new byte[this.sigLength];
        data.readBytes(sigBytes, 0, this.sigLength);
        short size = data.getShort(data.readerIndex() + 2);
        if (size > data.readableBytes() || size == 0) {
          data.readerIndex(data.readerIndex() - this.sigLength);
          break;
        } 
        m = (OFMessage)this.reader.readFrom(data);
        if (m != null) {
          ByteBuf mData = Unpooled.buffer();
          m.writeTo(mData);
          byte[] mBytes = new byte[mData.readableBytes()];
          int mReader = mData.readerIndex();
          mData.getBytes(mReader, mBytes);
  //        boolean verified = verifySig(mBytes, sigBytes);
          Packet p = new Packet(m.getType(), m, mData);
          p.setSignature(sigBytes);
          results.add(p);
        } 
        count++;
      } 
    } catch (OFParseError e) {
      System.out.println("Parse error");
      e.printStackTrace();
    } catch (IllegalArgumentException ex) {
      System.out.println("Illegal argument!");
      ex.printStackTrace();
      data.readerIndex(prev);
    } catch (NullPointerException ex) {
      System.out.println("parser threw null pointer!!!!");
      ex.printStackTrace();
    } catch (Exception e) {
      System.out.println("Error caused by: " + ip);
      e.printStackTrace();
    } 
    return results;
  }
  
  public boolean verifyPacketOut(ArrayList<Packet> packetOuts) {
    OFPacketOut first = (OFPacketOut)((Packet)packetOuts.get(0)).getMessage();
    for (int i = 1; i < packetOuts.size(); i++) {
      if (!first.equalsIgnoreXid(((Packet)packetOuts.get(i)).getMessage()))
        return false; 
    } 
    return true;
  }
  
  public boolean verifyFlowMods(ArrayList<Packet> flowMods) {
    OFMessage first = ((Packet)flowMods.get(0)).getMessage();
    for (int i = 1; i < flowMods.size(); i++) {
      if (!first.equalsIgnoreXid(((Packet)flowMods.get(i)).getMessage()))
        return false; 
    } 
    return true;
  }
  
  public int verifyPacketOutFailure(Packet packet, ArrayList<Packet> packetOuts) {
    OFPacketOut first = (OFPacketOut)packet.getMessage();
    int count = 0;
    for (int i = 0; i < packetOuts.size(); i++) {
      if (first.equalsIgnoreXid(((Packet)packetOuts.get(i)).getMessage()))
        count++; 
    } 
    return count;
  }
  
  public int verifyFlowModsFailure(Packet packet, ArrayList<Packet> flowMods) {
    OFFlowMod first = (OFFlowMod)packet.getMessage();
    int count = 0;
    for (int i = 0; i < flowMods.size(); i++) {
      OFFlowMod compare = (OFFlowMod)((Packet)flowMods.get(i)).getMessage();
      if (first.getActions().equals(compare.getActions()) && first.getMatch().equals(compare.getMatch()))
        count++; 
    } 
    return count;
  }
  
  public boolean verifySig(byte[] data, byte[] sig) throws Exception {
    this.signer.update(data);
    return this.signer.verify(sig);
  }
  
  public static KeyPair generateKeyPair(long seed, int keyLength) throws Exception {
    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
    SecureRandom rng = SecureRandom.getInstance("SHA1PRNG", "SUN");
    rng.setSeed(seed);
    keyGenerator.initialize(keyLength, rng);
    return keyGenerator.generateKeyPair();
  }
  
  public static byte[] signData(byte[] data, KeyPair keyPair, Signature signer) throws Exception {
    signer.update(data);
    return signer.sign();
  }
  
  public byte[] signData(ByteBuf data) {
    byte[] toReturn = null;
    try {
      byte[] dBytes = new byte[data.readableBytes()];
      data.getBytes(data.readerIndex(), dBytes);
      this.sign.update(dBytes);
      toReturn = this.sign.sign();
    } catch (SignatureException e) {
      e.printStackTrace();
    } 
    return toReturn;
  }
  
  public byte[] signData(byte[] data) {
    byte[] toReturn = null;
    try {
      this.sign.update(data);
      toReturn = this.sign.sign();
    } catch (SignatureException e) {
      e.printStackTrace();
    } 
    return toReturn;
  }
}
