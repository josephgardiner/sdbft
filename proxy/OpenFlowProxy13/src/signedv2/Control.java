package signedv2;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFHelloFailedCode;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.errormsg.OFHelloFailedErrorMsg;
import org.projectfloodlight.openflow.types.OFErrorCauseData;

public class Control {
  OpenFlowPacketParser parser;
  
  List<Controller> controllers;
  
  ClientConnection client;
  
  byte[] helloMessage;
  
  ArrayList<Request> requestQueue;
  
  HashMap<Integer, SwitchEvent> switchEvents;
  
  HashMap<Integer, ServerConnection> mappedChannels;
  
  HashMap<OFType, OFType> replyRequestMapping;
  
  boolean useSigs = false;
  
  int localPort;
  
  int xidCounter = 0;
  
  SwitchEvent event;
  
  int controllerCount;
  
  KeyPair keyPair;
  
  Signature signer;
  
  SendAcks ackSender;
  
  int sigLength;
  
  String sigType;
  
  String keyType;
  
  int keyLength;
  
  OFVersion oFVersion;
  
  boolean batchAcks;
  
  public static void main(String[] args) throws IOException {
    try {
      String host = "148.88.227.205";
      int remoteport = 6633;
      int localport = 111;
      System.out.println("Starting proxy for " + host + ":" + remoteport + " on port " + localport);
      Control control = new Control(null);
      control.runServer();
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  public Control(Properties props) {
    this.sigLength = Integer.parseInt(props.getProperty("siglength"));
    this.sigType = props.getProperty("signaturetype");
    this.keyType = props.getProperty("signaturekey");
    this.keyLength = Integer.parseInt(props.getProperty("keysize"));
    this.parser = new OpenFlowPacketParser(this.sigLength, this.keyLength, this.sigType);
    this.controllers = new ArrayList<>();
    this.switchEvents = new HashMap<>();
    this.requestQueue = new ArrayList<>();
    this.mappedChannels = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();
    try {
      this.controllers = (List<Controller>)mapper.readValue(props.getProperty("controllers"), new TypeReference<List<Controller>>() {
          
          });
    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } 
    for (int i = 0; i < this.controllers.size(); i++)
      System.out.println(this.controllers.get(i)); 
    this.localPort = Integer.parseInt(props.getProperty("localport"));
    this.xidCounter = Integer.parseInt(props.getProperty("startxid"));
    this.replyRequestMapping = new HashMap<>();
    this.replyRequestMapping.put(OFType.HELLO, OFType.HELLO);
    this.replyRequestMapping.put(OFType.STATS_REPLY, OFType.STATS_REQUEST);
    this.replyRequestMapping.put(OFType.BARRIER_REPLY, OFType.BARRIER_REQUEST);
    this.replyRequestMapping.put(OFType.ROLE_REPLY, OFType.ROLE_REQUEST);
    this.replyRequestMapping.put(OFType.FEATURES_REPLY, OFType.FEATURES_REQUEST);
    this.replyRequestMapping.put(OFType.GET_CONFIG_REPLY, OFType.GET_CONFIG_REQUEST);
    this.controllerCount = this.controllers.size();
    String ofVersion = props.getProperty("ofversion");
    String str1;
    switch ((str1 = ofVersion).hashCode()) {
      case 48563:
        if (str1.equals("1.0")) {
          this.oFVersion = OFVersion.OF_10;
          break;
        } 
      case 48564:
        if (str1.equals("1.1")) {
          this.oFVersion = OFVersion.OF_11;
          break;
        } 
      case 48565:
        if (str1.equals("1.2")) {
          this.oFVersion = OFVersion.OF_12;
          break;
        } 
      case 48566:
        if (str1.equals("1.3")) {
          this.oFVersion = OFVersion.OF_13;
          break;
        } 
      case 48567:
        if (str1.equals("1.4")) {
          this.oFVersion = OFVersion.OF_14;
          break;
        } 
      case 48568:
        if (str1.equals("1.5")) {
          this.oFVersion = OFVersion.OF_15;
          break;
        } 
      default:
        this.oFVersion = OFVersion.OF_13;
        break;
    } 
    System.out.println("using openflow version: " + this.oFVersion);
    setUpSignatures(props.getProperty("signaturetype"), props.getProperty("signaturekey"), 
        Integer.parseInt(props.getProperty("keysize")));
  }
  
  public void setUpServerConnections() {
    for (int i = 0; i < this.controllers.size(); i++) {
      ((Controller)this.controllers.get(i)).setUpConnection(this, new OpenFlowPacketParser(this.sigLength, this.keyLength, this.sigType), 
          this.oFVersion);
      this.mappedChannels.put(Integer.valueOf(((Controller)this.controllers.get(i)).id), ((Controller)this.controllers.get(i)).getConnection());
    } 
  }
  
  public void sendToAllServers(byte[] data, int size) {
    for (int i = 0; i < this.controllers.size(); i++)
      ((Controller)this.controllers.get(i)).send((byte[])data.clone(), size); 
  }
  
  public void sendToClient(byte[] data, int size) {
    if (this.client != null) {
      this.client.send(data, size);
    } else {
      System.out.println("Client not connected");
    } 
  }
  
  public void sendHelloMessage() {
    if (this.client != null) {
      this.client.send(this.helloMessage, this.helloMessage.length);
    } else {
      System.out.println("Client not connected");
    } 
  }
  
  public void setUpSignatures(String sigVersion, String keyType, int keySize) {
    try {
      this.keyPair = generateKeyPair(999L, keyType, keySize);
      this.signer = Signature.getInstance(sigVersion);
      this.signer.initSign(this.keyPair.getPrivate());
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  public static KeyPair generateKeyPair(long seed, String keyType, int keySize) throws Exception {
    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(keyType);
    SecureRandom rng = SecureRandom.getInstance("SHA1PRNG", "SUN");
    rng.setSeed(seed);
    keyGenerator.initialize(keySize, rng);
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
      this.signer.update(dBytes);
      toReturn = this.signer.sign();
    } catch (SignatureException e) {
      e.printStackTrace();
    } 
    return toReturn;
  }
  
  public byte[] signData(byte[] data) {
    byte[] toReturn = null;
    try {
      this.signer.update(data);
      toReturn = this.signer.sign();
    } catch (SignatureException e) {
      e.printStackTrace();
    } 
    return toReturn;
  }
  
  public void runServer() throws IOException {
    ServerSocket ss = new ServerSocket(this.localPort);
    Socket clientSock = null;
    boolean second = false;
    while (true) {
      try {
        clientSock = ss.accept();
        clientSock.setTcpNoDelay(true);
        System.out.println("got connection from: " + clientSock.getRemoteSocketAddress());
        this.client = new ClientConnection(clientSock, this, this.parser, this.oFVersion);
        if (second)
          setUpServerConnections(); 
        second = true;
        Thread t = new Thread(this.client);
        t.start();
      } catch (IOException e) {
        e.printStackTrace();
      } 
    } 
  }
  
  public void setHelloMessage(byte[] message) {
    this.helloMessage = message;
  }
  
  public void closeServerConnections() {
    for (int i = 0; i < this.controllers.size(); i++)
      ((Controller)this.controllers.get(i)).stop(); 
  }
  
  public void addSwitchEvent(Integer key, SwitchEvent value) {
    this.switchEvents.put(key, value);
  }
  
  public void deleteSwitchEvent(Integer key) {
    this.switchEvents.remove(key);
  }
  
  public SwitchEvent getSwitchEvent(Integer key) {
    return this.switchEvents.get(key);
  }
  
  public int getNextXid() {
    this.xidCounter++;
    return this.xidCounter;
  }
  
  public void handlePacket(Packet packet, byte[] raw, int rawSize) {
    if (this.event != null) {
      switch (packet.getType()) {
        case PACKET_OUT:
          this.event.addPacketOut(packet);
          System.out.println("Packet Out");
          System.out.println(packet.getMessage());
          if (this.event.getPacketOuts().size() == this.controllerCount) {
            System.out.println("Sending packet out");
            sendToClient(raw, rawSize);
          } 
          break;
        case FLOW_MOD:
          this.event.addFlowMod(packet);
          System.out.println("Flow Mod");
          System.out.println(packet.getMessage());
          if (this.event.getFlowMods().size() == this.controllerCount && 
            this.parser.verifyFlowMods(this.event.getFlowMods())) {
            System.out.println("sending flow mod");
            sendToClient(raw, rawSize);
          } 
          break;
      } 
    } else {
      sendToClient(raw, rawSize);
    } 
  }
  
  public int getControllerCount() {
    return this.controllers.size();
  }
  
  public void handlePacketIn(Packet packet) {}
  
  public void queueRequest(Request r) {
    this.requestQueue.add(r);
  }
  
  public synchronized void processResponseList(List<Packet> packets, int id) {
    ByteBuf toWrite = Unpooled.buffer();
    String ackMessage = null;
    for (Packet packet : packets) {
      System.out.println("Response: " + packet.getType());
      if (getSwitchEvent(Integer.valueOf((int)packet.getMessage().getXid())) != null) {
        System.out.println("Found event");
        SwitchEvent event = getSwitchEvent(Integer.valueOf((int)packet.getMessage().getXid()));
        switch (packet.getType()) {
          case PACKET_OUT:
            event.addPacketOut(packet);
            if (event.getPacketOuts().size() == getControllerCount()) {
              if (this.parser.verifyPacketOut(event.getPacketOuts())) {
                System.out.println("Got packet out group");
                packet.getMessage().writeTo(toWrite);
              } else {
                System.out.println("Packet outs not verified!!");
              } 
              ackMessage = "##ACK##PACKETOUT##";
            } 
            continue;
          case FLOW_MOD:
            event.addFlowMod(packet);
            if (event.getFlowMods().size() == getControllerCount()) {
              if (this.parser.verifyFlowMods(event.getFlowMods())) {
                System.out.println("Got flow mod group");
                packet.getMessage().writeTo(toWrite);
              } 
              ackMessage = "##ACK##FLOWMOD##";
            } 
            continue;
        } 
        continue;
      } 
      ByteBuf t = Unpooled.buffer();
      switch (packet.getType()) {
        case FEATURES_REQUEST:
          queueRequest(new Request(id, packet.getMessage().getType(), packet.getMessage().getXid(), 
                packet.getMessage()));
          packet.getMessage().writeTo(t);
          t.setInt(4, id);
          toWrite.writeBytes(t);
          continue;
        case STATS_REQUEST:
          queueRequest(new Request(id, packet.getMessage().getType(), packet.getMessage().getXid(), 
                packet.getMessage()));
          packet.getMessage().writeTo(t);
          t.setInt(4, id);
          toWrite.writeBytes(t);
          continue;
        case BARRIER_REQUEST:
          queueRequest(new Request(id, packet.getMessage().getType(), packet.getMessage().getXid(), 
                packet.getMessage()));
          packet.getMessage().writeTo(t);
          t.setInt(4, id);
          toWrite.writeBytes(t);
          continue;
        case ROLE_REQUEST:
          queueRequest(new Request(id, packet.getMessage().getType(), packet.getMessage().getXid(), 
                packet.getMessage()));
          packet.getMessage().writeTo(t);
          t.setInt(4, id);
          toWrite.writeBytes(t);
          continue;
        case GET_CONFIG_REQUEST:
          queueRequest(new Request(id, packet.getMessage().getType(), packet.getMessage().getXid(), 
                packet.getMessage()));
          packet.getMessage().writeTo(t);
          t.setInt(4, id);
          toWrite.writeBytes(t);
          continue;
        case PACKET_OUT:
          packet.getMessage().writeTo(t);
          toWrite.writeBytes(t);
          continue;
        case ECHO_REQUEST:
        case HELLO:
          continue;
      } 
      packet.getMessage().writeTo(toWrite);
    } 
    byte[] bytes = new byte[toWrite.readableBytes()];
    int readerIndex = toWrite.readerIndex();
    toWrite.getBytes(readerIndex, bytes);
    byte[] copyTo = new byte[65535];
    System.arraycopy(bytes, 0, copyTo, 0, bytes.length);
    sendToClient(copyTo, bytes.length);
  }
  
  public void sendAckBroadcast(String message, OFVersion version) {
    ByteBuf ackBuf = Unpooled.buffer();
    OFHelloFailedErrorMsg.Builder replyBuilder = OFFactories.getFactory(version).errorMsgs().buildHelloFailedErrorMsg();
    OFErrorCauseData data = new OFErrorCauseData(message.getBytes(), version);
    replyBuilder.setData(data);
    replyBuilder.setCode(OFHelloFailedCode.EPERM);
    OFHelloFailedErrorMsg oFHelloFailedErrorMsg = replyBuilder.build();
    oFHelloFailedErrorMsg.writeTo(ackBuf);
    byte[] bytes = new byte[ackBuf.readableBytes()];
    int readerIndex = ackBuf.readerIndex();
    ackBuf.getBytes(readerIndex, bytes);
    byte[] sig = signData(bytes);
    ackBuf.resetReaderIndex();
    byte[] comb = new byte[sig.length + bytes.length];
    System.arraycopy(sig, 0, comb, 0, sig.length);
    System.arraycopy(bytes, 0, comb, sig.length, bytes.length);
    byte[] copyTo = new byte[65535];
    System.arraycopy(comb, 0, copyTo, 0, comb.length);
    if (this.batchAcks) {
      this.ackSender.addMessage(comb);
    } else {
      sendToAllServers(copyTo, comb.length);
    } 
  }
  
  public void processResponse(Packet p) {
    OFType expected = this.replyRequestMapping.get(p.getType());
    boolean found = false;
    int i;
    for (i = 0; i < this.requestQueue.size(); i++) {
      if (((Request)this.requestQueue.get(i)).getType().equals(expected) && 
        p.getMessage().getXid() == ((Request)this.requestQueue.get(i)).getController()) {
        ByteBuf toWrite = Unpooled.buffer();
        p.getMessage().writeTo(toWrite);
        toWrite.setInt(4, (int)((Request)this.requestQueue.get(i)).getXid());
        toWrite.writeBytes(toWrite);
        byte[] bytes = new byte[toWrite.readableBytes()];
        int readerIndex = toWrite.readerIndex();
        toWrite.getBytes(readerIndex, bytes);
        byte[] sig = signData(bytes);
        byte[] comb = new byte[sig.length + bytes.length];
        System.arraycopy(sig, 0, comb, 0, sig.length);
        System.arraycopy(bytes, 0, comb, sig.length, bytes.length);
        ((ServerConnection)this.mappedChannels.get(Integer.valueOf(((Request)this.requestQueue.get(i)).getController()))).send(comb, comb.length);
        if (p.getType() != OFType.STATS_REPLY)
          this.requestQueue.remove(i); 
        found = true;
        break;
      } 
    } 
    if (p.getType() == OFType.ERROR)
      for (i = 0; i < this.requestQueue.size(); i++) {
        if (p.getMessage().getXid() == ((Request)this.requestQueue.get(i)).getController()) {
          ByteBuf toWrite = Unpooled.buffer();
          p.getMessage().writeTo(toWrite);
          toWrite.setInt(4, (int)((Request)this.requestQueue.get(i)).getXid());
          toWrite.writeBytes(toWrite);
          byte[] bytes = new byte[toWrite.readableBytes()];
          int readerIndex = toWrite.readerIndex();
          toWrite.getBytes(readerIndex, bytes);
          ((ServerConnection)this.mappedChannels.get(Integer.valueOf(((Request)this.requestQueue.get(i)).getController()))).send(bytes, bytes.length);
          if (p.getType() != OFType.STATS_REPLY)
            this.requestQueue.remove(i); 
          found = true;
          break;
        } 
      }  
    if (!found) {
      System.out.println("not found: " + p.getType());
      ByteBuf toWrite = Unpooled.buffer();
      p.getMessage().writeTo(toWrite);
      byte[] bytes = new byte[toWrite.readableBytes()];
      int readerIndex = toWrite.readerIndex();
      toWrite.getBytes(readerIndex, bytes);
      byte[] sig = signData(bytes);
      byte[] comb = new byte[65535];
      System.arraycopy(sig, 0, comb, 0, sig.length);
      System.arraycopy(bytes, 0, comb, sig.length, bytes.length);
      sendToAllServers(comb, comb.length);
    } 
  }
  
  public OFVersion getoFVersion() {
    return this.oFVersion;
  }
  
  public void setoFVersion(OFVersion oFVersion) {
    this.oFVersion = oFVersion;
  }
  
  public void pickNewQuorum() {}
  
  public void handleControllerFault() {}
}
