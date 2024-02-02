package proxy;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import io.netty.buffer.Unpooled;


public class SimpleProxyServer {
	static OpenFlowPacketParser parser;
  public static void main(String[] args) throws IOException {
    try {

    //	 SimpleCLI cmd = parseArgs(args);
         // int port = Integer.valueOf(cmd.getOptionValue("p"));


      String host = "192.168.1.136";
      int remoteport = 6653;
//      String host = "148.88.227.127";
//      int remoteport = 6633;
     // String host = "127.0.0.1";
//      String host = "148.88.227.205";
//      int remoteport = 6653;

//      String host = "127.0.0.1";
//      int remoteport = 6622;
      int localport = 111;
      // Print a start-up message
      System.out.println("Starting proxy for " + host + ":" + remoteport
          + " on port " + localport);
      // And start running the server
      runServer(host, remoteport, localport); // never returns
    } catch (Exception e) {
      System.err.println(e);
    }
  }

  /**
   * runs a single-threaded proxy server on
   * the specified local port. It never returns.
   */
  public static void runServer(String host, int remoteport, int localport)
      throws IOException {
	  parser = new OpenFlowPacketParser(null);
    // Create a ServerSocket to listen for connections with
    ServerSocket ss = new ServerSocket(localport);
    final byte[] request = new byte[1024];
    byte[] reply = new byte[4096];

    while (true) {
      Socket client = null, server = null;
      try {
        // Wait for a connection on the local port
        client = ss.accept();
        System.out.println("Accepting connection: " + client.getLocalAddress());
        final InputStream streamFromClient = client.getInputStream();
        final OutputStream streamToClient = client.getOutputStream();

        // Make a connection to the real server.
        // If we cannot connect to the server, send an error to the
        // client, disconnect, and continue waiting for connections.
        try {
          server = new Socket(host, remoteport);
        } catch (IOException e) {
          PrintWriter out = new PrintWriter(streamToClient);
          out.print("Proxy server cannot connect to " + host + ":"
              + remoteport + ":\n" + e + "\n");
          out.flush();
          client.close();
          continue;
        }

        // Get server streams.
        final InputStream streamFromServer = server.getInputStream();
        final OutputStream streamToServer = server.getOutputStream();

//
//        SocketChannel socketChannel = SocketChannel.open();
//        socketChannel.connect(new InetSocketAddress("127.0.0.1", 6622));

        // a thread to read the client's requests and pass them
        // to the server. A separate thread for asynchronous.
        Thread t = new Thread() {
          public void run() {
            int bytesRead;
            try {

              while ((bytesRead = streamFromClient.read(request)) != -1) {
         //        System.out.println("Message to server");
                 System.out.println("Message to server");
            //     System.out.println("Message to server");
                  System.out.println("Client:");

            	  try{
                      parser.parseMessages(Unpooled.wrappedBuffer(request), 100);
                      }catch(NullPointerException ex){
                      	System.out.println("Unable to parse");
                      }
                streamToServer.write(request, 0, bytesRead);
                streamToServer.flush();
          //      System.out.println("Client");
         //       parser.parseMessages(Unpooled.wrappedBuffer(request), 100);
                System.out.println("Client");
                parser.parseMessages(Unpooled.wrappedBuffer(request), 100);




          //      while(buffer.hasRemaining()) {
               //     socketChannel.write(buffer);
          //      }
//               streamToProxy.write(buffer.array());
//                streamToProxy.flush();
              }
            } catch (IOException e) {
            	System.out.println("IO Exception");
            }

            // the client closed the connection to us, so close our
            // connection to the server.
            try {
            	System.out.println("CLient closed");
              streamToServer.close();
            } catch (IOException e) {
            }
          }
        };

        // Start the client-to-server request thread running
        t.start();

        // Read the server's responses
        // and pass them back to the client.
        int bytesRead;
        try {
          while ((bytesRead = streamFromServer.read(reply)) != -1) {

     //         System.out.println("Message to client");

              System.out.println("Message to client");

         //     System.out.println("Message to client");

      //        System.out.println(new String(reply, 0, bytesRead));
        	  try{
                  parser.parseMessages(Unpooled.wrappedBuffer(reply), 100);
                   }catch(NullPointerException ex){
                   	System.out.println("Unable to parse");
                   }
            streamToClient.write(reply, 0, bytesRead);
            streamToClient.flush();

       //     System.out.println("Server:");
      //      parser.parseMessages(Unpooled.wrappedBuffer(reply), 100);

            System.out.println("Server:");
            parser.parseMessages(Unpooled.wrappedBuffer(reply), 100);

            System.out.println("Server:");




          }
        } catch (IOException e) {
        	System.out.println("Error");
        }

        // The server closed its connection to us, so we close our
        // connection to our client.
        streamToClient.close();
      } catch (IOException e) {
        System.err.println(e);
      } finally {
        try {
        	System.out.println("closing");
          if (server != null)
            server.close();
          if (client != null)
            client.close();
        } catch (IOException e) {
        }
      }
    }
  }
}