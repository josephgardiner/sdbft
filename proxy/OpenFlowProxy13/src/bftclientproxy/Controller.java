package bftclientproxy;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.projectfloodlight.openflow.protocol.OFVersion;

import java.io.IOException;
import java.net.Socket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
public class Controller {
	ServerConnection connection;
	String IP;
	int port;
	int id;

	public Controller(ServerConnection connection, String iP, int port, int id) {
		super();
		this.connection = connection;
		IP = iP;
		this.port = port;
		this.id = id;
	}

	@JsonCreator
	public Controller(@JsonProperty("ip")String iP,@JsonProperty("port") int port,@JsonProperty("id")int id) {
		super();
		IP = iP;
		this.port = port;
		this.id=id;
	}

	public void setUpConnection(BFTClientControl control, OpenFlowPacketParser parser, OFVersion version, int bftClientID) {
		Socket sock = null;
		try {
			sock = new Socket(IP, port);
			sock.setTcpNoDelay(true);
		
	//		sock.shutdownOutput();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.connection = new ServerConnection(IP, sock, control, this, parser, version);
		Thread th = new Thread(this.connection);
		System.out.println("Starting server thread");
		th.start();
 
	}

	public void send(byte[] data, int size) {
		this.connection.send(data, size);
	}

	public ServerConnection getConnection() {
		return connection;
	}

	public void setConnection(ServerConnection connection) {
		this.connection = connection;
	}

	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void stop(){
		connection.close();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Controller [IP=" + IP + ", port=" + port + ", id=" + id + "]";
	}
	
	
	
	

}
