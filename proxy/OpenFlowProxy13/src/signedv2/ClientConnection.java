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
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;

public class ClientConnection implements Runnable {
  Socket sock;
  
  Control control;
  
  InputStream streamFromClient;
  
  OutputStream streamToClient;
  
  byte[] temp;
  
  boolean waiting;
  
  ByteBuf buffer;
  
  OpenFlowPacketParser parser;
  
  OFVersion ofVersion;
  
  public ClientConnection(Socket s, Control control, OpenFlowPacketParser parser, OFVersion version) {
    this.sock = s;
    this.control = control;
    this.parser = parser;
    this.buffer = Unpooled.buffer();
    this.ofVersion = version;
    try {
      this.streamFromClient = this.sock.getInputStream();
      this.streamToClient = new BufferedOutputStream(this.sock.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public void close() {
    try {
      System.out.println("Closing client connection");
      this.sock.close();
      this.streamToClient.close();
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public void send(byte[] data, int bytesRead) {
    try {
      if (bytesRead > 0) {
        this.streamToClient.write(data, 0, bytesRead);
        this.streamToClient.flush();
      } 
      Thread.yield();
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public void run() {
    byte[] request = new byte[65535];
    try {
      int bytesRead;
      while ((bytesRead = this.streamFromClient.read(request)) != -1) {
        ByteBuf m = Unpooled.copiedBuffer(request, 0, bytesRead);
        ByteBuf toWrite = Unpooled.buffer();
        List<Packet> packets = null;
        try {
          if (this.buffer.readableBytes() > 0) {
            ByteBuf messageBuf = Unpooled.copiedBuffer(new ByteBuf[] { this.buffer, m });
            m = Unpooled.copiedBuffer(messageBuf);
          } 
          packets = this.parser.parseMessagesLooping(m, 100);
          for (Packet p : packets) {
            int nextXID;
            ByteBuf temp;
            byte[] sig;
            OFHello.Builder helloBuilder;
            OFHello hello;
            ByteBuf helloBuf;
            byte[] arrayOfByte1;
            int i;
            ByteBuf echoBuf;
            OFEchoReply.Builder echoBuilder;
            OFEchoReply echo;
            byte[] bytes1;
            int readerIndex1;
            System.out.println(p.getType());
            switch (p.getType()) {
              case PACKET_IN:
                nextXID = this.control.getNextXid();
                temp = Unpooled.buffer();
                p.getMessage().writeTo(temp);
                temp.setInt(4, nextXID);
                this.control.addSwitchEvent(Integer.valueOf(nextXID), new SwitchEvent(nextXID, (OFPacketIn)p.getMessage(), temp));
                sig = this.control.signData(temp);
                temp.resetReaderIndex();
                toWrite.writeBytes(sig);
                toWrite.writeBytes(temp);
                continue;
              case STATS_REPLY:
                this.control.processResponse(p);
                continue;
              case ROLE_REPLY:
                this.control.processResponse(p);
                continue;
              case FEATURES_REPLY:
                this.control.processResponse(p);
                continue;
              case GET_CONFIG_REPLY:
                this.control.processResponse(p);
                continue;
              case HELLO:
                helloBuilder = 
                  OFFactories.getFactory(this.ofVersion).buildHello();
                hello = helloBuilder.build();
                helloBuf = Unpooled.buffer();
                hello.writeTo(helloBuf);
                arrayOfByte1 = new byte[helloBuf.readableBytes()];
                i = helloBuf.readerIndex();
                helloBuf.getBytes(i, arrayOfByte1);
                this.streamToClient.write(arrayOfByte1);
                this.streamToClient.flush();
                continue;
              case ECHO_REQUEST:
                System.out.println("Got echo request");
                echoBuf = Unpooled.buffer();
                echoBuilder = 
                  OFFactories.getFactory(this.ofVersion).buildEchoReply();
                echo = echoBuilder.build();
                echo.writeTo(echoBuf);
                bytes1 = new byte[echoBuf.readableBytes()];
                readerIndex1 = echoBuf.readerIndex();
                echoBuf.getBytes(readerIndex1, bytes1);
                this.streamToClient.write(bytes1);
                this.streamToClient.flush();
                continue;
              case ERROR:
                this.control.processResponse(p);
                continue;
            } 
            ByteBuf defTemp = Unpooled.buffer();
            p.getMessage().writeTo(defTemp);
            byte[] defSig = this.control.signData(defTemp);
            defTemp.resetReaderIndex();
            toWrite.writeBytes(defSig);
            toWrite.writeBytes(defTemp);
          } 
          this.buffer = Unpooled.copiedBuffer(m);
        } catch (NullPointerException ex) {
          System.out.println("Switch: NullPointer");
          ex.printStackTrace();
        } 
        m.resetReaderIndex();
        byte[] bytes = new byte[toWrite.readableBytes()];
        int readerIndex = toWrite.readerIndex();
        toWrite.getBytes(readerIndex, bytes);
        this.control.sendToAllServers(bytes, bytes.length);
        Thread.yield();
      } 
    } catch (IOException e) {
      System.out.println("Client IO Exception");
      this.control.closeServerConnections();
    } 
  }
}
