package signedv2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import org.projectfloodlight.openflow.protocol.OFEchoReply;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFHello;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;

public class ServerConnection implements Runnable {
  String IP;
  
  Socket sock;
  
  Control control;
  
  InputStream streamFromServer;
  
  OutputStream streamToServer;
  
  Controller controller;
  
  ByteBuf buffer;
  
  OpenFlowPacketParser parser;
  
  OFVersion ofVersion;
  
  public void close() {
    try {
      this.sock.close();
      this.streamToServer.close();
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public ServerConnection(String iP, Socket s, Control control, Controller controller, OpenFlowPacketParser parser, OFVersion ofVersion) {
    this.IP = iP;
    this.sock = s;
    this.control = control;
    this.controller = controller;
    this.parser = parser;
    this.buffer = Unpooled.buffer();
    this.ofVersion = ofVersion;
    try {
      this.streamFromServer = this.sock.getInputStream();
      this.streamToServer = new BufferedOutputStream(this.sock.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public void send(byte[] data, int bytesRead) {
    try {
      this.streamToServer.write(data, 0, bytesRead);
      this.streamToServer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public void run() {
    byte[] reply = new byte[65535];
    try {
      int bytesRead;
      while ((bytesRead = this.streamFromServer.read(reply)) != -1) {
        ByteBuf m = Unpooled.copiedBuffer(reply, 0, bytesRead);
        List<Packet> packets = null;
        try {
          ByteBuf messageBuf = Unpooled.copiedBuffer(new ByteBuf[] { this.buffer, m });
          m = Unpooled.copiedBuffer(messageBuf);
          long startTime = System.nanoTime();
          packets = this.parser.parseMessagesLoopingSigned(m, 100, this.controller.IP);
          System.out.println("Parse Time: " + (System.nanoTime()-startTime));
          this.buffer = Unpooled.copiedBuffer(m);
        } catch (NullPointerException ex) {
          System.out.println("Controller: NullPointer");
          this.buffer = Unpooled.copiedBuffer(m);
        } catch (Exception ex) {
          System.out.println("Error came from controller: " + this.controller.IP);
        } 
        m.resetReaderIndex();
        ByteBuf toWrite = Unpooled.buffer();
        ByteBuf ackBuf = Unpooled.buffer();
        if (packets != null)
          for (Packet p : packets) {
        	  System.out.println("Server: " + p.getType());
            if (p.getType() == OFType.HELLO) {
              ByteBuf helloBuf = Unpooled.buffer();
              OFHello.Builder helloBuilder = 
                OFFactories.getFactory(this.ofVersion).buildHello();
              OFHello hello = helloBuilder.build();
              hello.writeTo(helloBuf);
              byte[] bytes = new byte[helloBuf.readableBytes()];
              int readerIndex = helloBuf.readerIndex();
              helloBuf.getBytes(readerIndex, bytes);
              byte[] sig = this.parser.signData(bytes);
              byte[] comb = new byte[sig.length + bytes.length];
              System.arraycopy(sig, 0, comb, 0, sig.length);
              System.arraycopy(bytes, 0, comb, sig.length, bytes.length);
              send(comb, comb.length);
              continue;
            } 
            if (p.getType() == OFType.ECHO_REQUEST) {
              ByteBuf echoBuf = Unpooled.buffer();
              OFEchoReply.Builder echoBuilder = 
                OFFactories.getFactory(this.ofVersion).buildEchoReply();
              OFEchoReply echo = echoBuilder.build();
              echo.writeTo(echoBuf);
              byte[] bytes = new byte[echoBuf.readableBytes()];
              int readerIndex = echoBuf.readerIndex();
              echoBuf.getBytes(readerIndex, bytes);
              long startTime = System.nanoTime();
              byte[] sig = this.parser.signData(bytes);
              
              byte[] comb = new byte[sig.length + bytes.length];
              System.arraycopy(sig, 0, comb, 0, sig.length);
              System.arraycopy(bytes, 0, comb, sig.length, bytes.length);
              System.out.println("Echo Sign Time: " + (System.nanoTime() - startTime));
              this.streamToServer.write(comb);
              this.streamToServer.flush();
            } 
          }  
        if (packets.size() > 0 && packets != null)
          this.control.processResponseList(packets, this.controller.getId()); 
        Thread.yield();
      } 
    } catch (IOException e) {
      System.out.println("Server IO Exception");
    } 
  }
}
